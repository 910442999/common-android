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
    fun getFileExtension(fileName: String?): String {
        return if (fileName != null && fileName.lastIndexOf(".") != -1) {
            fileName.substring(fileName.lastIndexOf(".") + 1)
        } else ""
    }

    @JvmStatic
    fun removeFileExtension(filename: String): String {
        return filename.takeLastWhile { it != '.' }
    }

    @JvmStatic
    fun getParseFormat(fileName: String): String {
        return fileName.substring(fileName.lastIndexOf(".") + 1)
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
        val digest = MessageDigest.getInstance("MD5")
        val buffer = ByteArray(4096)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            if (read > 0) {
                digest.update(buffer, 0, read)
            }
        }
        inputStream.close()

        val md5sum = digest.digest()
        val hexString = StringBuilder()
        for (i in md5sum.indices) {
            val hex = Integer.toHexString(0xFF and md5sum[i].toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
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
}