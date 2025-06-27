package com.yuanquan.common.utils


import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

class AudioToBufferUtils {
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO // 单声道
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT // 16-bit PCM

    private var audioRecord: AudioRecord? = null
    private var bufferSize: Int = 0
    private var isRecording = false

    @SuppressLint("MissingPermission")
    fun startRecording(sampleRate: Int = 44100) {
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, CHANNEL_CONFIG, AUDIO_FORMAT)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.DEFAULT, sampleRate, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize
        )

        val buffer = ByteArray(bufferSize)

        audioRecord?.startRecording()
        isRecording = true

        Thread {
            while (isRecording) {
                val bytesRead = audioRecord?.read(buffer, 0, bufferSize)
                // 在这里可以处理读取到的音频数据，将其转换为所需的格式
                // buffer 中的数据即为录音数据，bytesRead 表示实际读取的字节数
                LogUtil.e("读取到的字节：" + buffer)
            }
        }.start()
    }

    fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
    }
}