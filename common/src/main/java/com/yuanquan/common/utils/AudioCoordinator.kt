package com.yuanquan.common.utils


import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import com.yuanquan.common.interfaces.AudioDeviceChangeListener

/**
 * 协调管理类 AudioCoordinator
 *
 * 使用示例（音乐播放）
 * class MusicPlayerActivity : AppCompatActivity() {
 *     private lateinit var audioCoordinator: AudioCoordinator
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_music_player)
 *
 *         // 初始化音频协调器
 *         audioCoordinator = AudioCoordinator(this)
 *         // 配置为音乐模式
 *         audioCoordinator.configureAudio(true)
 *
 *         // 准备播放
 *         audioCoordinator.prepare()
 *
 *         // 开始播放音乐
 *         startMusicStreaming()
 *     }
 *
 *     private fun startMusicStreaming() {
 *         thread {
 *             while (true) {
 *                 // 从网络获取音频数据（示例）
 *                 val audioData = receiveMusicDataFromNetwork()
 *                 audioCoordinator.addAudioData(audioData)
 *             }
 *         }
 *     }
 *
 *     override fun onDestroy() {
 *         super.onDestroy()
 *         // 释放音频资源
 *         audioCoordinator.release()
 *     }
 * }
 *
 * 使用示例（语音通话）
 *
 * class VoiceCallActivity : AppCompatActivity() {
 *     private lateinit var audioCoordinator: AudioCoordinator
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_voice_call)
 *
 *         // 初始化音频协调器
 *         audioCoordinator = AudioCoordinator(this)
 *         // 配置为语音模式（默认就是，可以不调用）
 *         audioCoordinator.configureAudio(false)
 *
 *         // 请求音频焦点
 *         requestAudioFocus()
 *
 *         // 开始接收音频数据（示例）
 *         startAudioStreaming()
 *     }
 *
 *     private fun requestAudioFocus() {
 *         val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
 *         val result = audioManager.requestAudioFocus(
 *             { focusChange -> audioCoordinator.handleAudioFocusChange(focusChange) },
 *             AudioManager.STREAM_VOICE_CALL,
 *             AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
 *         )
 *
 *         if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
 *             audioCoordinator.prepare()
 *         }
 *     }
 *
 *     override fun onDestroy() {
 *         super.onDestroy()
 *         // 释放音频资源
 *         audioCoordinator.release()
 *     }
 * }
 */
class AudioCoordinator(context: Context) : AudioDeviceChangeListener {
    private val mediaManager: MediaManager
    private val audioPlayer: AudioPlayerUtils
    private var isPrepared = false

    // 新增：标记当前是否是音乐模式
    private var isMusicMode = false
    var audioDeviceChangeListener: AudioDeviceChangeListener? = null

    init {
        // 初始化媒体管理器
        mediaManager = MediaManager(context)
        // 注意：这里我们暂时不调用configureAudio，因为模式由调用方决定
        mediaManager.setAudioDeviceChangeListener(this) // 设置监听器
        // 初始化音频播放器
        audioPlayer = AudioPlayerUtils().apply {
            setMediaManager(mediaManager)
            // 设置默认值（语音模式）
            configureForVoice()
        }
    }

    /**
     * 配置音频模式（语音或音乐）
     * @param isMusic true为音乐，false为语音
     */
    fun configureAudio(isMusic: Boolean) {
        isMusicMode = isMusic
        mediaManager.configureAudio(!isMusic)

        if (isMusic) {
            audioPlayer.configureForMusic()
        } else {
            audioPlayer.configureForVoice()
        }
    }

    fun setSkipWavHeader(skip: Boolean) {
        audioPlayer.setSkipWavHeader(skip)
    }

    /**
     * 准备音频系统
     */
    fun prepare() {
        if (!isPrepared) {
            audioPlayer.prepare()
            // 然后设置媒体管理器的音频会话ID
            audioPlayer.audioSession?.let { sessionId ->
                if (sessionId != AudioManager.ERROR) {
                    mediaManager.setAudioSessionId(sessionId)
                }
            }

            // 设置初始音频模式
//            applyInitialAudioMode()

            isPrepared = true
            LogUtil.i("AudioCoordinator", "音频系统准备就绪")
        }
    }

    fun applyInitialAudioMode() {
        mediaManager.applyOptimalAudioMode()
    }

    /*
    * 设置音频模式
    */
    fun setAudioMode(mode: MediaManager.AudioMode) {
        if (isMusicMode && mode == MediaManager.AudioMode.EARPIECE) {
            LogUtil.w("AudioCoordinator", "音乐播放不支持听筒模式，使用扬声器")
            mediaManager.setAudioMode(MediaManager.AudioMode.SPEAKER)
        } else {
            mediaManager.setAudioMode(mode)
        }
        syncAudioDevice()
    }

    private fun syncAudioDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val audioTrack = audioPlayer.audioTrackRef.get()
            audioTrack?.let {
                val currentMode = mediaManager.currentMode
                val devices = mediaManager.getAudioManager().availableCommunicationDevices

                when (currentMode) {
                    MediaManager.AudioMode.EARPIECE -> {
                        devices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE }
                            ?.let { device ->
                                it.preferredDevice = device
                            }
                    }

                    MediaManager.AudioMode.SPEAKER -> {
                        devices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                            ?.let { device ->
                                it.preferredDevice = device
                            }
                    }

                    MediaManager.AudioMode.BLUETOOTH -> {
                        devices.firstOrNull {
                            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                        }?.let { device ->
                            it.preferredDevice = device
                        }
                    }

                    MediaManager.AudioMode.HEADPHONES -> {
                        devices.firstOrNull {
                            it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
                        }?.let { device ->
                            it.preferredDevice = device
                        }
                    }

                    else -> it.preferredDevice = null
                }
            }
        }
    }

    /**
     * 添加音频数据
     */
    fun addAudioData(data: ByteArray) {
        if (!isPrepared) {
            prepare()
        }
        audioPlayer.addAudioData(data)
    }

    /**
     * 暂停播放
     */
    fun pause() {
        audioPlayer.pause()
        // 注意：我们不在这里释放mediaManager，因为暂停后可能很快恢复
        isPrepared = false
    }

    /**
     * 恢复播放
     */
    fun resume() {
        // 确保会话ID同步
//        audioPlayer.audioSession?.let { sessionId ->
//            if (sessionId != AudioManager.ERROR) {
//                mediaManager.setAudioSessionId(sessionId)
//            }
//        }
        audioPlayer.resume()
    }

    /**
     * 释放所有资源
     */
    fun release() {
        audioPlayer.release()
        mediaManager.unregisterAudioDeviceCallback()
        mediaManager.release()
        isPrepared = false
        LogUtil.i("AudioCoordinator", "音频资源已释放")
    }

    /**
     * 切换扬声器状态（仅对语音通话有效）
     */
    fun toggleSpeaker() {
        if (isMusicMode) {
            // 音乐模式下，我们切换扬声器开关（其实就是外放和耳机/蓝牙之间的切换由系统管理，我们只控制音量等）
            val currentSpeakerOn = mediaManager.isSpeakerphoneOn()
            mediaManager.setSpeakerMode(!currentSpeakerOn)
        } else {
            val currentMode = mediaManager.currentMode
            setAudioMode(
                if (currentMode == MediaManager.AudioMode.SPEAKER) {
                    MediaManager.AudioMode.EARPIECE
                } else {
                    MediaManager.AudioMode.SPEAKER
                }
            )
        }
    }

    // 新增：切换到蓝牙设备
    fun switchToBluetooth() {
        if (mediaManager.isBluetoothHeadsetConnected()) {
            setAudioMode(MediaManager.AudioMode.BLUETOOTH)
        }
    }

    // 新增：切换到有线耳机
    fun switchToHeadphones() {
        if (mediaManager.isHeadphonesPlugged()) {
            setAudioMode(MediaManager.AudioMode.HEADPHONES)
        }
    }

    /**
     * 处理音频焦点变化
     */
    fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pause()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pause()
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
                resume()
            }
        }
    }

    override fun onAudioMode(audioMode: MediaManager.AudioMode) {
        audioDeviceChangeListener?.onAudioMode(audioMode)
    }

    override fun onAudioDeviceChanged(deviceName: String?) {
        audioDeviceChangeListener?.onAudioDeviceChanged(deviceName)
    }
}