package com.yuanquan.common.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
import android.net.Uri
import android.provider.OpenableColumns
import android.text.TextUtils
import android.webkit.MimeTypeMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URLConnection
import java.security.MessageDigest
import java.util.Locale


object FileUtils {
    val ILLEGAL_CHARS = arrayOf("\\", "/") // 可根据需要扩展

    // 获取文件扩展名
    @JvmStatic
    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }

    fun removeFileExtension(filename: String): String {
        return filename.takeLastWhile { it != '.' }
    }

    @JvmStatic
    fun removeFileExtension2(filename: String): String {
        if (filename.lastIndexOf(".") != -1) {
            return filename.substring(0, filename.lastIndexOf("."))
        } else {
            return filename
        }
    }

    // 校验非法字符
    fun hasIllegalCharacters(fileName: String): Boolean {
        return ILLEGAL_CHARS.any { fileName.contains(it) }
    }

    // 获取文件名
    fun getFileName(context: Context, uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else null
        }
    }

    // 获取文件名
    fun getFileSize(context: Context, uri: Uri): Long? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
            } else null
        }
    }

    // 获取文件大小
    @JvmStatic
    fun getFileSize2(context: Context, uri: Uri): Long {
        return context.contentResolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
            parcelFileDescriptor.statSize
        } ?: 0L
    }

    fun hasMediaOrAnimationHeader(file: File): Boolean {
        return try {
            val fis = FileInputStream(file)
            val headerBytes = ByteArray(4096)
            fis.read(headerBytes)
            fis.close()
            String(headerBytes).contains("ppt/media") // 粗略判断
        } catch (e: Exception) {
            false
        }
    }

    fun hasMediaOrAnimationHeader(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val headerBytes = ByteArray(4096)
                inputStream.read(headerBytes)
                String(headerBytes).contains("ppt/media") // 关键标识检测
            } ?: false
        } catch (e: Exception) {
            false
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

    // 视频分辨率校验（异步）
    private fun checkVideoResolution(context: Context, uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)

                val width = retriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
                val height = retriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
                retriever.release()

                if (width > 1280 || height > 720) {
                    withContext(Dispatchers.Main) {
//                        showToast("视频分辨率需在720P以下")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
//                    showToast("无法读取视频信息")
                }
            }
        }
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

    // 工具方法：MIME 类型 → 具体文件类型
    fun getFileExtensionFromMime(mimeType: String?): String? {
        if (mimeType == null) return null

        return when (mimeType) {
            "audio/mpeg" -> "mp3"
            "audio/mp4" -> "m4a"
            "audio/ogg" -> "ogg"

            "video/mp4" -> "mp4"
            "video/avi" -> "avi"
            "video/x-ms-wmv" -> "wmv"
            "video/x-matroska" -> "mkv"
            "video/quicktime" -> "mov"
            "video/x-msvideo" -> "avi"
            "video/x-flv" -> "flv"
            "video/mpeg" -> "mpg"
            "video/x-ms-asf" -> "asf"

            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"

            "application/msword" -> "doc"
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
            "application/vnd.ms-powerpoint" -> "ppt"
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "pptx"
            "application/pdf" -> "pdf"
            "text/plain" -> "txt"

            else -> null
        }
    }

    fun getFileMimeType(file: File?): String {
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