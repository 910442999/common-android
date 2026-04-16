package com.yuanquan.common.utils

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.os.Process
import androidx.annotation.RequiresPermission
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt

class MicrophoneWaveListener(
    private val sampleRate: Int = 16000,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
    private val audioSource: Int = MediaRecorder.AudioSource.MIC,
    private val enableAcousticEchoCanceler: Boolean = false,
    private val enableNoiseSuppressor: Boolean = false,
    private val enableAutomaticGainControl: Boolean = false
) {
    private val audioRecordLock = Any()
    private var audioRecord: AudioRecord? = null
    private var acousticEchoCanceler: AcousticEchoCanceler? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var automaticGainControl: AutomaticGainControl? = null
    private val isListening = AtomicBoolean(false)
    private var listeningThread: Thread? = null

    interface WaveDetectionListener {
        fun onReadBuffer(buffer: ShortArray, readSize: Int)
        fun onWaveDetected(amplitude: Double, isActive: Boolean)
        fun onError(message: String)
    }

    private var listener: WaveDetectionListener? = null

    fun setWaveDetectionListener(listener: WaveDetectionListener) {
        this.listener = listener
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startListening() {
        if (!isListening.compareAndSet(false, true)) {
            return
        }

        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (minBufferSize <= 0) {
            isListening.set(false)
            listener?.onError("无效的缓冲区大小: $minBufferSize")
            return
        }

        try {
            LogUtil.i(
                "MicrophoneWaveListener startListening: sampleRate=$sampleRate channelConfig=$channelConfig audioFormat=$audioFormat audioSource=$audioSource"
            )
            val record = AudioRecord.Builder()
                .setAudioSource(audioSource)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelConfig)
                        .setEncoding(audioFormat)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize * AUDIO_RECORD_BUFFER_MULTIPLIER)
                .build()

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                isListening.set(false)
                record.release()
                LogUtil.e("MicrophoneWaveListener: AudioRecord init failed, source=$audioSource")
                listener?.onError("无法初始化AudioRecord")
                return
            }

            synchronized(audioRecordLock) {
                audioRecord = record
            }
            initAudioEffects(record)
            record.startRecording()

            listeningThread = Thread({
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
                listenForWaves(record, minBufferSize)
            }, "MicrophoneWaveListener").apply {
                start()
            }
        } catch (e: SecurityException) {
            isListening.set(false)
            LogUtil.e("MicrophoneWaveListener: missing permission ${e.message}")
            listener?.onError("缺少录音权限: ${e.message}")
        } catch (e: Exception) {
            isListening.set(false)
            LogUtil.e("MicrophoneWaveListener: start failed ${e.message}")
            listener?.onError("启动监听失败: ${e.message}")
            releaseAudioRecord()
        }
    }

    fun stopListening() {
        if (!isListening.compareAndSet(true, false)) {
            return
        }

        val thread = listeningThread
        thread?.interrupt()
        try {
            thread?.join(STOP_LISTENING_JOIN_TIMEOUT_MS)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        listeningThread = null
        releaseAudioRecord()
    }

    private fun listenForWaves(record: AudioRecord, bufferSize: Int) {
        val buffer = ShortArray(bufferSize)

        while (isListening.get() && !Thread.currentThread().isInterrupted) {
            val read = try {
                record.read(buffer, 0, buffer.size)
            } catch (e: Exception) {
                listener?.onError("读取麦克风数据失败: ${e.message}")
                break
            }

            when {
                read > 0 -> {
                    listener?.onReadBuffer(buffer, read)
                    val amplitude = calculateAmplitude(buffer, read)
                    listener?.onWaveDetected(amplitude, amplitude > AMPLITUDE_THRESHOLD)
                }

                read == 0 -> {
                    continue
                }

                else -> {
                    listener?.onError("读取麦克风数据异常: $read")
                    break
                }
            }
        }

        isListening.set(false)
        releaseAudioRecord()
    }

    private fun releaseAudioRecord() {
        val record = synchronized(audioRecordLock) {
            val current = audioRecord ?: return
            audioRecord = null
            current
        }
        try {
            if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                record.stop()
            }
        } catch (_: Exception) {
        } finally {
            releaseAudioEffects()
            record.release()
        }
    }

    private fun initAudioEffects(record: AudioRecord) {
        releaseAudioEffects()
        val audioSessionId = record.audioSessionId
        LogUtil.i(
            "MicrophoneWaveListener audio effects: session=$audioSessionId aecAvailable=${AcousticEchoCanceler.isAvailable()} nsAvailable=${NoiseSuppressor.isAvailable()} agcAvailable=${AutomaticGainControl.isAvailable()}"
        )

        if (enableAcousticEchoCanceler && AcousticEchoCanceler.isAvailable()) {
            acousticEchoCanceler = AcousticEchoCanceler.create(audioSessionId)?.apply {
                enabled = true
            }
            LogUtil.i(
                "MicrophoneWaveListener AEC enabled=${acousticEchoCanceler?.enabled} created=${acousticEchoCanceler != null}"
            )
            if (acousticEchoCanceler == null) {
                LogUtil.e("MicrophoneWaveListener AEC create returned null")
            } else if (acousticEchoCanceler?.enabled != true) {
                LogUtil.e("MicrophoneWaveListener AEC enable failed")
            }
        } else if (enableAcousticEchoCanceler) {
            LogUtil.e("MicrophoneWaveListener AEC unavailable")
        }

        if (enableNoiseSuppressor && NoiseSuppressor.isAvailable()) {
            noiseSuppressor = NoiseSuppressor.create(audioSessionId)?.apply {
                enabled = true
            }
            LogUtil.i(
                "MicrophoneWaveListener NS enabled=${noiseSuppressor?.enabled} created=${noiseSuppressor != null}"
            )
            if (noiseSuppressor == null) {
                LogUtil.e("MicrophoneWaveListener NS create returned null")
            } else if (noiseSuppressor?.enabled != true) {
                LogUtil.e("MicrophoneWaveListener NS enable failed")
            }
        } else if (enableNoiseSuppressor) {
            LogUtil.e("MicrophoneWaveListener NS unavailable")
        }

        if (enableAutomaticGainControl && AutomaticGainControl.isAvailable()) {
            automaticGainControl = AutomaticGainControl.create(audioSessionId)?.apply {
                enabled = true
            }
            LogUtil.i(
                "MicrophoneWaveListener AGC enabled=${automaticGainControl?.enabled} created=${automaticGainControl != null}"
            )
            if (automaticGainControl == null) {
                LogUtil.e("MicrophoneWaveListener AGC create returned null")
            } else if (automaticGainControl?.enabled != true) {
                LogUtil.e("MicrophoneWaveListener AGC enable failed")
            }
        } else if (enableAutomaticGainControl) {
            LogUtil.e("MicrophoneWaveListener AGC unavailable")
        }
    }

    private fun releaseAudioEffects() {
        acousticEchoCanceler?.release()
        acousticEchoCanceler = null

        noiseSuppressor?.release()
        noiseSuppressor = null

        automaticGainControl?.release()
        automaticGainControl = null
    }

    private fun calculateAmplitude(buffer: ShortArray, length: Int): Double {
        var sum = 0.0
        for (i in 0 until length) {
            val sample = buffer[i].toDouble()
            sum += sample * sample
        }
        return if (length > 0) sqrt(sum / length) else 0.0
    }

    companion object {
        private const val AMPLITUDE_THRESHOLD = 100.0
        private const val AUDIO_RECORD_BUFFER_MULTIPLIER = 2
        private const val STOP_LISTENING_JOIN_TIMEOUT_MS = 500L
    }
}
