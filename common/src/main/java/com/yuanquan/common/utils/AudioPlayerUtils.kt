package com.yuanquan.common.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.audiofx.AcousticEchoCanceler
import android.os.Build
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class AudioPlayerUtils {
    // 使用原子引用确保线程安全
    public val audioTrackRef = AtomicReference<AudioTrack?>(null)
    private val audioQueue = LinkedBlockingQueue<ByteArray>(calculateOptimalQueueSize())
    private val audioExecutor = Executors.newSingleThreadExecutor()

    // 使用原子布尔变量避免锁竞争
    private val isProcessing = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)
    private val isReleased = AtomicBoolean(false)
    private val isPrepared = AtomicBoolean(false) // 新增：准备状态标志
    private val enableAcousticEchoCanceler = AtomicBoolean(false)
    private var mediaManager: MediaManager? = null // 新增：与MediaManager关联
    public var audioSession: Int? = null
    private var contentType: Int = AudioAttributes.CONTENT_TYPE_SPEECH
    private var usage: Int = AudioAttributes.USAGE_VOICE_COMMUNICATION
    private var channelConfig = AudioFormat.CHANNEL_OUT_MONO
    private var audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private var sampleRate = 16000
    private var skipWavHeader = false // 新增：控制是否跳过WAV头

    // 强制退出标志
    private val shouldExit = AtomicBoolean(false)

    // 新增：设置MediaManager
    fun setMediaManager(manager: MediaManager) {
        this.mediaManager = manager
    }

    // 新增：设置是否跳过WAV头
    fun setSkipWavHeader(skip: Boolean) {
        this.skipWavHeader = skip
    }

    // 配置为语音模式
    fun configureForVoice() {
        contentType = AudioAttributes.CONTENT_TYPE_SPEECH
        usage = AudioAttributes.USAGE_VOICE_COMMUNICATION
        enableAcousticEchoCanceler.set(true)
        sampleRate = 16000
        channelConfig = AudioFormat.CHANNEL_OUT_MONO
        skipWavHeader = false
    }

    // 配置为音乐模式
    fun configureForMusic() {
        contentType = AudioAttributes.CONTENT_TYPE_MUSIC
        usage = AudioAttributes.USAGE_MEDIA
        enableAcousticEchoCanceler.set(false)
        sampleRate = 44100
        channelConfig = AudioFormat.CHANNEL_OUT_STEREO
        skipWavHeader = true
    }

    // 根据系统性能动态调整队列大小
    private fun calculateOptimalQueueSize(): Int {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.O -> 50
            Runtime.getRuntime().availableProcessors() > 6 -> 200
            else -> 100
        }
    }

    fun enableAcousticEchoCanceler(enable: Boolean) {
        enableAcousticEchoCanceler.set(enable)
    }

    fun setAudioSessionId(audioSession: Int) {
        this.audioSession = audioSession
        // 同时更新MediaManager的音频会话ID
        mediaManager?.setAudioSessionId(audioSession)
    }

    fun setUsage(usage: Int) {
        this.usage = usage
    }

    fun setContentType(contentType: Int) {
        this.contentType = contentType
    }

    fun setChannelConfig(channelConfig: Int) {
        this.channelConfig = channelConfig
    }

    fun setAudioFormat(audioFormat: Int) {
        this.audioFormat = audioFormat
    }

    fun setSampleRate(sampleRate: Int) {
        this.sampleRate = sampleRate
    }

    fun prepare() {
        // 如果已经释放，不再重新准备
        // 如果已经释放或已准备，不再重新准备
        if (isReleased.get() || isPrepared.get()) return

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate, channelConfig, audioFormat
        )
        // 创建新的AudioTrack并立即设置引用
        val newAudioTrack = AudioTrack.Builder().setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(usage)
                .setContentType(contentType)
                .build()
        ).setAudioFormat(
            AudioFormat.Builder().setEncoding(audioFormat).setSampleRate(sampleRate)
                .setChannelMask(channelConfig).build()
        ).setBufferSizeInBytes(bufferSize * 2)
        // 使用现有会话ID或生成新的
        val sessionId = audioSession?.takeIf { it != AudioManager.ERROR } ?: 0
        newAudioTrack.setSessionId(sessionId)
        var audioTrackBuild = newAudioTrack.build()
        audioTrackBuild.apply {
            play()
        }
        // 保存实际的音频会话ID
        this.audioSession = audioTrackBuild.audioSessionId
        // 启用AEC（仅语音模式）
        if (enableAcousticEchoCanceler.get() && AcousticEchoCanceler.isAvailable()) {
            try {
                AcousticEchoCanceler.create(audioSession ?: 0)?.enabled = true
                LogUtil.e("启用回声消除")
            } catch (e: Exception) {
                LogUtil.e("AudioPlayerUtils", "启用回声消除失败: ${e.message}")
            }
        }

        // 原子操作设置audioTrack
        audioTrackRef.set(audioTrackBuild)
        // 标记为已准备
        isPrepared.set(true)
        // 启动处理线程（如果尚未启动）
        startProcessingThread()
    }

    private fun startProcessingThread() {
        if (isProcessing.getAndSet(true)) return

        audioExecutor.execute {
            try {
                while (!Thread.interrupted() && !shouldExit.get()) {
                    // 检查释放状态
                    if (isReleased.get()) {
                        LogUtil.e("音频播放器已释放，处理线程退出")
                        break
                    }

                    // 检查暂停状态
                    if (isPaused.get()) {
                        Thread.sleep(50)
                        continue
                    }

                    // 从队列获取数据（避免阻塞太久）
                    val data = audioQueue.poll(50, TimeUnit.MILLISECONDS)
                    if (data != null) {
                        // 处理音频数据
                        val track = audioTrackRef.get()
                        if (track != null && track.state == AudioTrack.STATE_INITIALIZED) {
                            processAudioData(data, track)
                        }
                    }
                }
            } catch (e: InterruptedException) {
                // 正常退出
            } catch (e: Exception) {
                LogUtil.e("音频处理错误: ${e.message}")
            } finally {
                isProcessing.set(false)
                LogUtil.e("音频处理线程已停止")
            }
        }
    }

    fun addAudioData(data: ByteArray) {
        if (isReleased.get()) {
            LogUtil.e("音频播放器已释放，丢弃数据")
            return
        }

        if (isPaused.get()) {
            LogUtil.e("音频已暂停，丢弃数据")
            return
        }
        if (!audioQueue.offer(data)) {
            LogUtil.e("音频队列已满，丢弃数据包")
        }
    }

    private fun processAudioData(data: ByteArray, track: AudioTrack) {
        try {
            var offset = 0
            var length = data.size

            // 如果需要跳过WAV文件头
            if (skipWavHeader && data.size > 44) {
                offset = 44
                length = data.size - 44
            }

            // 直接写入音频轨道
            track.write(data, offset, length)
        } catch (e: Exception) {
            LogUtil.e("音频写入错误: ${e.message}")

            // 尝试重置音频轨道
            resetAudioTrack()
        }
    }

    /**
     * 暂停播放
     */
    fun pause() {
        if (isPaused.getAndSet(true)) return

        LogUtil.e("音频播放已暂停")

        // 清空队列
        audioQueue.clear()

        // 直接停止并刷新音频轨道
        val track = audioTrackRef.getAndSet(null)
        if (track != null) {
            try {
                track.pause()
                track.flush()
                track.release()
            } catch (e: Exception) {
                LogUtil.e("暂停音频轨道时出错: ${e.message}")
            }
        }
        // 重置准备状态
        isPrepared.set(false)
        isPaused.set(false)
    }

    /**
     * 恢复播放
     */
    fun resume() {
        if (!isPaused.getAndSet(false)) return

        LogUtil.e("音频播放已恢复")
        prepare()
    }

    /**
     * 立即重置音频轨道（无锁版本）
     */
    fun resetAudioTrack() {
        LogUtil.e("立即重置音频轨道")

        // 直接获取当前音频轨道并置空
        val oldTrack = audioTrackRef.getAndSet(null)

        // 立即停止和释放旧轨道
        oldTrack?.let { track ->
            try {
                track.pause()
                track.flush()
                track.release()
                LogUtil.e("旧音频轨道已释放")
            } catch (e: Exception) {
                LogUtil.e("释放旧音频轨道时出错: ${e.message}")
            }
        }
        //立即准备新轨道
        prepare()
    }

    /**
     * 立即释放所有资源（无锁版本）
     */
    fun release() {
        if (isReleased.getAndSet(true)) return

        LogUtil.e("立即释放音频播放器资源")

        // 1. 设置强制退出标志
        shouldExit.set(true)

        // 2. 立即停止并释放音频轨道
        val track = audioTrackRef.getAndSet(null)
        track?.let {
            try {
                it.pause()
                it.flush()
                it.release()
                LogUtil.e("音频轨道已释放")
            } catch (e: Exception) {
                LogUtil.e("释放音频轨道时出错: ${e.message}")
            }
        }

        // 3. 清空音频队列
        audioQueue.clear()
        LogUtil.e("音频队列已清空")

        // 4. 关闭线程池（立即中断所有线程）
        try {
            audioExecutor.shutdownNow()
            // 不等待线程结束，直接返回
            LogUtil.e("音频处理线程池已关闭")
        } catch (e: Exception) {
            LogUtil.e("关闭线程池时出错: ${e.message}")
        }
        // 重置所有状态
        isPrepared.set(false)
        isPaused.set(false)
        audioSession = null
    }

    /**
     * 获取当前队列大小
     */
    fun getQueueSize(): Int = audioQueue.size

    /**
     * 检查是否已释放
     */
    fun isReleased(): Boolean = isReleased.get()

    /**
     * 检查是否已暂停
     */
    fun isPaused(): Boolean = isPaused.get()
}