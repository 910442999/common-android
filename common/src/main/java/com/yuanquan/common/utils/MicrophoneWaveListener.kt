package com.yuanquan.common.utils

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlin.math.abs
import kotlin.math.sqrt

class MicrophoneWaveListener(
    private val sampleRate: Int = 16000,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
) {
    private var audioRecord: AudioRecord? = null
    private var isListening = false
    private var listeningThread: Thread? = null

    // 监听回调接口
    interface WaveDetectionListener {
        fun onReadBuffer(buffer: ShortArray)
        fun onWaveDetected(amplitude: Double, isActive: Boolean)
        fun onError(message: String)
    }

    private var listener: WaveDetectionListener? = null

    fun setWaveDetectionListener(listener: WaveDetectionListener) {
        this.listener = listener
    }

    /**
     * 开始监听麦克风波动
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startListening() {
        if (isListening) return

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            channelConfig,
            audioFormat
        )

        if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            listener?.onError("无效的缓冲区大小")
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                listener?.onError("无法初始化AudioRecord")
                return
            }

            audioRecord?.startRecording()
            isListening = true

            listeningThread = Thread {
                listenForWaves(minBufferSize)
            }.apply { start() }

        } catch (e: SecurityException) {
            listener?.onError("缺少录音权限: ${e.message}")
        } catch (e: Exception) {
            listener?.onError("启动监听失败: ${e.message}")
        }
    }

    /**
     * 停止监听麦克风波动
     */
    fun stopListening() {
        if (!isListening) return

        isListening = false
        listeningThread?.interrupt()
        listeningThread = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e("MicrophoneListener", "停止监听失败", e)
        } finally {
            audioRecord = null
        }
    }

    /**
     * 监听波动核心逻辑
     */
    private fun listenForWaves(bufferSize: Int) {
        val buffer = ShortArray(bufferSize)

        while (isListening && !Thread.currentThread().isInterrupted) {
            try {
                val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                listener?.onReadBuffer(buffer)
                if (read > 0) {
                    // 计算振幅
                    val amplitude = calculateAmplitude(buffer, read)
                    // 判断是否有波动
                    val isActive = amplitude > AMPLITUDE_THRESHOLD

                    // 回调结果
                    listener?.onWaveDetected(amplitude, isActive)
                }

                // 短暂休眠以减少CPU使用
                Thread.sleep(LISTENING_INTERVAL)

            } catch (e: Exception) {
                listener?.onError("监听过程中出错: ${e.message}")
                stopListening()
            }
        }
    }

    /**
     * 计算音频振幅（RMS）
     */
    private fun calculateAmplitude(buffer: ShortArray, length: Int): Double {
        var sum = 0.0

        for (i in 0 until length) {
            sum += buffer[i] * buffer[i]
        }

        return if (length > 0) {
            sqrt(sum / length)
        } else {
            0.0
        }
    }

    companion object {
        // 振幅阈值（根据实际环境调整）
        private const val AMPLITUDE_THRESHOLD = 100.0

        // 监听间隔（毫秒）
        private const val LISTENING_INTERVAL = 100L
    }
}