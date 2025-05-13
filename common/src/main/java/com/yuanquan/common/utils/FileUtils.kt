package com.yuanquan.common.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URLConnection
import java.security.MessageDigest
import java.util.Locale

/**
 * 1、根据指定类型打开文件管理
 *  val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
 *     type = "*\/" // 必须设置 type 才能应用 EXTRA_MIME_TYPES
 *      putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
 *      putExtra(
 *          Intent.EXTRA_MIME_TYPES,
 *          arrayOf(
 *              "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // XLSX
 *              "application/vnd.ms-excel" // XLS
 *          )
 *      )
 *      addCategory(Intent.CATEGORY_OPENABLE)
 *  }
 *  startActivityForResult(intent, FILE_REQUEST_CODE)
 *
 */
object FileUtils {
    val ILLEGAL_CHARS = arrayOf("\\", "/") // 可根据需要扩展

    // 获取文件扩展名
    @JvmStatic
    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }

    @JvmStatic
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

    @JvmStatic
    // 校验非法字符
    fun hasIllegalCharacters(fileName: String): Boolean {
        return ILLEGAL_CHARS.any { fileName.contains(it) }
    }

    @JvmStatic
    // 获取文件名
    fun getFileName(context: Context, uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else null
        }
    }

    @JvmStatic
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

    @JvmStatic
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

    @JvmStatic
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

    @JvmStatic
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

    @JvmStatic
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

    @JvmStatic
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
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx"
            "application/vnd.ms-excel" -> "xls"
            else -> null
        }
    }

    @JvmStatic
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
    @JvmStatic
    private fun getMimeType(file: File): String? {
        val fileNameMap = URLConnection.getFileNameMap()
        return fileNameMap.getContentTypeFor(file.name)
    }

    @JvmStatic
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

    /**
     * 获取存储的根目录
     *
     * @return
     */
    @JvmStatic
    fun getExternalCacheDir(context: Context): File? {
        return context.externalCacheDir
    }

    @JvmStatic
    fun getExternalFilesDir(context: Context, type: String): File? {
        return context.getExternalFilesDir(type)
    }

    @JvmStatic
    val imageExts: ArrayList<String>
        get() {
            val imageTypes = arrayOf("png", "jpg", "jpeg", "bmp", "gif")
            val imageExts = imageTypes.indices.mapTo(ArrayList()) { imageTypes[it] }

            return imageExts
        }

    @JvmStatic
    val videoExts: ArrayList<String>
        get() {
            val videoTypes = arrayOf("mpeg", "mp4", "gif", "wmv", "mov", "mpg", "3gp", "flv")
            val videoExts = videoTypes.indices.mapTo(ArrayList()) { videoTypes[it] }
            return videoExts
        }

    @JvmStatic
    val docExts: ArrayList<String>
        get() {
            val docTypes = arrayOf("doc", "docx", "pdf", "txt")
            val docExts = docTypes.indices.mapTo(ArrayList()) { docTypes[it] }
            return docExts
        }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @JvmStatic
    fun getPath(context: Context, uri: Uri): String? {

        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {

                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {

                val decodedURI = Uri.decode(uri.toString())

                if (decodedURI.contains("raw:")) {
                    return decodedURI.substring(decodedURI.indexOf("raw:") + 4)
                }

                val id = DocumentsContract.getDocumentId(Uri.parse(decodedURI))

                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)!!
                )

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {

                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                } else if ("document" == type) {
                    contentUri = MediaStore.Files.getContentUri("external")
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    @JvmStatic
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor =
                context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            if (cursor != null)
                cursor.close()
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    @JvmStatic
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    @JvmStatic
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    @JvmStatic
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }


    const val FILE_TYPE_IMAGE = 1
    const val FILE_TYPE_AUDIO = 2
    const val FILE_TYPE_VIDEO = 3
    const val FILE_TYPE_UNKNOWN = 4

    /**
     * String filePath = "your_file_path";
     * int fileType = FileTypeUtils.getFileType(filePath);
     *
     * switch (fileType) {
     *     case FileTypeUtils.FILE_TYPE_IMAGE:
     *         // 是图片类型
     *         break;
     *     case FileTypeUtils.FILE_TYPE_AUDIO:
     *         // 是音频类型
     *         break;
     *     case FileTypeUtils.FILE_TYPE_VIDEO:
     *         // 是视频类型
     *         break;
     *     case FileTypeUtils.FILE_TYPE_UNKNOWN:
     *         // 文件类型未知
     *         break;
     * }
     *
     */
    @JvmStatic
    fun getFileType(file: File?): Int {
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file?.extension)
        if (mimeType != null) {
            if (mimeType.startsWith("image")) {
                return FILE_TYPE_IMAGE
            } else if (mimeType.startsWith("audio")) {
                return FILE_TYPE_AUDIO
            } else if (mimeType.startsWith("video")) {
                return FILE_TYPE_VIDEO
            }
        }
        return FILE_TYPE_UNKNOWN
    }

    /**
     * 删除文件
     *
     * @param filePath
     */
    @JvmStatic
    fun deleteFile(filePath: String?) {
        if (filePath == null) {
            return
        }
        val file = File(filePath)
        try {
            if ((file.isFile)) {
                file.delete()
            }
        } catch (e: java.lang.Exception) {
        }
    }

    /**
     * 删除文件夹的所有文件
     *
     * @param file
     * @return
     */
    @JvmStatic
    fun delAllFile(file: File?): Boolean {
        if (file == null || !file.exists()) {
            return false
        }

        if (file.isDirectory) {
            val files = file.listFiles()
            if (files != null) for (f in files) {
                delAllFile(f)
            }
        }
        return file.delete()
    }

    /**
     * 将应用里的文件复制到指定Download路径
     */
    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveAssetToDownloads(context: Context, assetFileName: String, targetFileName: String) {
        try {
            // 1. 打开Asset文件流
            val assetManager = context.assets
            BufferedInputStream(assetManager.open(assetFileName)).use { inputStream ->

                // 2. 创建目标文件元数据
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, targetFileName)
//                    put(MediaStore.Downloads.MIME_TYPE, getMimeType(targetFileName))
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                // 3. 通过MediaStore插入文件
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw Exception("创建文件失败")

                // 4. 写入文件内容
                BufferedOutputStream(resolver.openOutputStream(uri)).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    outputStream.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 处理错误
        }
    }
}