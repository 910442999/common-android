package com.yuanquan.common.utils

import android.text.TextUtils
import android.webkit.MimeTypeMap
import java.io.File
import java.io.InputStream
import java.net.URLConnection
import java.security.MessageDigest
import java.util.Locale

object FileUtils {

    @JvmStatic
    fun getFileExtension(fileName: String): String {
        return if (fileName != null && fileName.lastIndexOf(".") != -1) {
            fileName.substring(fileName.lastIndexOf(".") + 1)
        } else ""
    }

    @JvmStatic
    fun removeFileExtension(filename: String): String {
        if (filename != null && filename.lastIndexOf(".") != -1) {
            return filename.substring(0, filename.lastIndexOf("."))
        } else {
            return filename
        }
    }

    @JvmStatic
    fun getParseName(url: String): String? {
        var fileName: String? = null
        try {
            fileName = url.substring(
                url.lastIndexOf("/") + 1,
                url.lastIndexOf(".")
            ) + System.currentTimeMillis()
        } finally {
            if (TextUtils.isEmpty(fileName)) {
                fileName = System.currentTimeMillis().toString()
            }
        }
        return fileName
    }

    fun calculateMD5(inputStream: InputStream): String {
        val md5Digest = MessageDigest.getInstance("MD5")

        // 使用缓冲区读取文件内容
        val buffer = ByteArray(8192)
        var bytesRead: Int

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            if (bytesRead > 0) {
                md5Digest.update(buffer, 0, bytesRead)
            }
        }

        inputStream.close()

        // 获取计算得到的MD5值
        val md5Bytes = md5Digest.digest()

        // 将字节数组转换为十六进制字符串表示
        val md5String = StringBuilder()
        for (i in md5Bytes.indices) {
            md5String.append(
                Integer.toString((md5Bytes[i].toInt() and 0xff) + 0x100, 16).substring(1)
            )
        }

        return md5String.toString()
    }

    fun getFileType(file: File?): String {
        if (file != null) {
            var mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
            if (TextUtils.isEmpty(mimeType)) {
                mimeType = getMimeType(file)
            }
            return mimeType ?: ""
        }
        return ""
    }

    /**
     * 获取mimeType
     *
     * @param file
     * @return
     */
    private fun getMimeType(file: File): String? {
        val fileNameMap = URLConnection.getFileNameMap()
        return fileNameMap.getContentTypeFor(file.name)
    }

    fun getMimeTypeFromMediaHttpUrl(url: String): String? {
        if (TextUtils.isEmpty(url)) {
            return null
        }
        if (url.lowercase(Locale.getDefault())
                .endsWith(".jpg") || url.lowercase(Locale.getDefault()).endsWith(".jpeg")
        ) {
            return "image/jpeg"
        } else if (url.lowercase(Locale.getDefault()).endsWith(".png")) {
            return "image/png"
        } else if (url.lowercase(Locale.getDefault()).endsWith(".gif")) {
            return "image/gif"
        } else if (url.lowercase(Locale.getDefault()).endsWith(".webp")) {
            return "image/webp"
        } else if (url.lowercase(Locale.getDefault()).endsWith(".bmp")) {
            return "image/bmp"
        } else if (url.lowercase(Locale.getDefault()).endsWith(".mp4")) {
            return "video/mp4"
        } else if (url.lowercase(Locale.getDefault()).endsWith(".avi")) {
            return "video/avi"
        } else if (url.lowercase(Locale.getDefault()).endsWith(".mp3")) {
            return "audio/mpeg"
        } else if (url.lowercase(Locale.getDefault()).endsWith(".amr")) {
            return "audio/amr"
        } else if (url.lowercase(Locale.getDefault()).endsWith(".m4a")) {
            return "audio/mpeg"
        }
        return null
    }
    @JvmStatic
    fun addWavHeaderToByteArray(
        audioData: ByteArray, sampleRate: Int, channelCount: Int, bitsPerSample: Int
    ): ByteArray {
        val totalAudioLen = audioData.size
        val totalDataLen = totalAudioLen + 44

        val header = ByteArray(44)
        val byteRate = sampleRate * channelCount * bitsPerSample / 8

        // 添加RIFF头
        header[0] = 'R'.toByte()
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xFF).toByte()
        header[5] = (totalDataLen shr 8 and 0xFF).toByte()
        header[6] = (totalDataLen shr 16 and 0xFF).toByte()
        header[7] = (totalDataLen shr 24 and 0xFF).toByte()
        header[8] = 'W'.toByte()
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()

        // 添加格式子块
        header[12] = 'f'.toByte()
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        header[16] = 16  // 格式子块大小（固定为16）
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1  // 音频格式（1表示PCM）
        header[21] = 0
        header[22] = channelCount.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xFF).toByte()
        header[25] = (sampleRate shr 8 and 0xFF).toByte()
        header[26] = (sampleRate shr 16 and 0xFF).toByte()
        header[27] = (sampleRate shr 24 and 0xFF).toByte()
        header[28] = (byteRate and 0xFF).toByte()
        header[29] = (byteRate shr 8 and 0xFF).toByte()
        header[30] = (byteRate shr 16 and 0xFF).toByte()
        header[31] = (byteRate shr 24 and 0xFF).toByte()
        header[32] = (channelCount * bitsPerSample / 8).toByte()
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = 0

        // 添加数据子块
        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xFF).toByte()
        header[41] = (totalAudioLen shr 8 and 0xFF).toByte()
        header[42] = (totalAudioLen shr 16 and 0xFF).toByte()
        header[43] = (totalAudioLen shr 24 and 0xFF).toByte()

        val headerAndAudioData = ByteArray(totalDataLen)
        System.arraycopy(header, 0, headerAndAudioData, 0, header.size)
        System.arraycopy(audioData, 0, headerAndAudioData, header.size, audioData.size)

        return header
    }
}