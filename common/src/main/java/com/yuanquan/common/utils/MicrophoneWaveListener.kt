package com.yuanquan.common.utils

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import android.util.Log
import androidx.annotation.RequiresPermission
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.sqrt

class MicrophoneWaveListener(
    private val sampleRate: Int = 16000,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
) {
    private var audioRecord: AudioRecord? = null
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
            val record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufferSize * AUDIO_RECORD_BUFFER_MULTIPLIER
            )

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                isListening.set(false)
                record.release()
                listener?.onError("无法初始化AudioRecord")
                return
            }

            audioRecord = record
            record.startRecording()

            listeningThread = Thread({
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
                listenForWaves(record, minBufferSize)
            }, "MicrophoneWaveListener").apply {
                start()
            }
        } catch (e: SecurityException) {
            isListening.set(false)
            listener?.onError("缺少录音权限: ${e.message}")
        } catch (e: Exception) {
            isListening.set(false)
            listener?.onError("启动监听失败: ${e.message}")
            releaseAudioRecord()
        }
    }

    fun stopListening() {
        if (!isListening.compareAndSet(true, false)) {
            return
        }

        listeningThread?.interrupt()
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
        val record = audioRecord ?: return
        audioRecord = null
        try {
            if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                record.stop()
            }
        } catch (_: Exception) {
        } finally {
            record.release()
        }
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
    }
}
