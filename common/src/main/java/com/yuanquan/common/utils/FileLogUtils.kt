package com.yuanquan.common.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.RandomAccessFile
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedList
import java.util.Locale
import kotlin.Boolean
import kotlin.math.ceil

/**
 * // 读取最新一页，每页20行
 * val latestLogs = FileLogUtils.readLatestPage(20)
 * println("总行数: ${latestLogs.totalLines}, 总页数: ${latestLogs.totalPages}")
 * println("当前页: ${latestLogs.currentPage}")
 * latestLogs.lines.forEach { line ->
 *     println(line)
 * }
 *
 * // 读取指定页码
 * val page2Logs = FileLogUtils.readFromBottomWithPagination(logFile, page = 2, pageSize = 10)
 *
 * // 读取特定日期的日志
 * val specificDateLogs = FileLogUtils.readLogByDate("2025-12-08", page = 1, pageSize = 15)
 *
 * // 分页导航
 * if (latestLogs.hasPrevious) {
 *     // 显示"上一页"按钮
 *     val prevPageLogs = FileLogUtils.readFromBottomWithPagination(logFile, latestLogs.currentPage - 1)
 * }
 *
 * if (latestLogs.hasNext) {
 *     // 显示"下一页"按钮
 *     val nextPageLogs = FileLogUtils.readFromBottomWithPagination(logFile, latestLogs.currentPage + 1)
 * }
 */
object FileLogUtils {
    private var TAG = "FileLog"
    private var logFile: File? = null
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
        // 初始化时尝试创建日志目录和当天的日志文件
        try {
            this.context?.let { ctx ->
                logFile = getLogFile(ctx)
            }
        } catch (e: Exception) {
            LogUtil.e("写入日志初始化文件异常")
        }
    }

    fun init(context: Context, tag: String) {
        this.setTagName(tag)
        this.init(context)
    }

    fun setTagName(tag: String) {
        TAG = tag
    }

    fun d(message: String) {
        if (context != null) {
            writeLog("DEBUG", message)
        } else {
            LogUtil.e("写入日志未初始化")
        }
    }

    fun i(message: String) {
        if (context != null) {
            writeLog("INFO", message)
        } else {
            LogUtil.e("写入日志未初始化")
        }
    }

    fun i(level: String, message: String) {
        if (context != null) {
            writeLog(level, message)
        } else {
            LogUtil.e("写入日志未初始化")
        }
    }

    fun w(message: String) {
        if (context != null) {
            writeLog("WARN", message)
        } else {
            LogUtil.e("写入日志未初始化")
        }
    }

    fun e(message: String) {
        if (context != null) {
            writeLog("ERROR", message)
        } else {
            LogUtil.e("写入日志未初始化")
        }
    }

    fun writeLog(level: String, message: String) {
        //部分PAD（联想小新）获取权限为拒绝但实际有权限
        // 建议在实际使用中将文件操作移至后台线程
        if (context != null) {
            writeFile(level, message)
        } else {
            LogUtil.e("写入日志未初始化")
        }
    }

    fun writeFile(level: String, message: String) {
        try {
            if (context == null) return
            if (logFile == null) {
                logFile = getLogFile(context!!)
            }
            if (logFile == null) {
                LogUtil.e("写入日志未初始化失败")
                return
            }
            val timestamp =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val log = "[$timestamp][$TAG][$level] $message"

            // 替换这部分代码，明确指定UTF-8编码
            FileOutputStream(logFile, true).use { fos ->
                // 如果是新文件，先写入UTF-8 BOM
//                if (logFile?.length() == 0L) {
//                    val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
//                    fos.write(bom)
//                }
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { osw ->
                    BufferedWriter(osw).use { writer ->
                        writer.append(log)
                        writer.flush()
                    }
                }
            }
        } catch (e: Exception) {
            LogUtil.e("写入日志失败: ${e.message}")
        }
    }

    fun writeMemory(level: String, message: String) {
        try {
            val timestamp =
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss.SSS",
                    Locale.getDefault()
                ).format(Date())
            val log = "[$timestamp][$TAG][$level] $message"
            var string = SPUtils.getInstance().getString(TAG)
            var s = string + log
            SPUtils.getInstance().put(TAG, s)
        } catch (e: Exception) {
            LogUtil.e(message)
        }
    }

    fun getMemory(): String? {
        var string = SPUtils.getInstance().getString(TAG)
        return string
    }

    fun cleanMemory() {
        SPUtils.getInstance().remove(TAG)
    }

    // 获取日志文件路径（兼容 Android 4.4 以下版本）
    fun getLogFile(context: Context): File? {
        val logDir: File? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // API 19+ 使用系统标准文档目录
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        } else {
            // API 18- 使用外部存储根目录下的自定义目录
            val externalDir = context.getExternalFilesDir(null)
            if (externalDir != null) {
                File(externalDir, "documents")
            } else {
                // 回退到内部存储
                context.filesDir
            }
        }?.let { baseDir ->
            File(baseDir, TAG).apply { mkdirs() } // 确保目录存在
        }

        if (logDir == null || !logDir.exists()) {
            LogUtil.e(TAG, "无法创建日志目录")
            return null
        }

        val timestamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fileName = "$TAG-$timestamp.log"
        return File(logDir, fileName).also { logFile = it }
    }

    /**
     * 获取日志目录
     */
    private fun getLogDirectory(): File? {
        if (context == null) return null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            context?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        } else {
            val externalDir = context?.getExternalFilesDir(null)
            externalDir ?: context?.filesDir
        }?.let { baseDir ->
            File(baseDir, TAG).apply {
                if (!exists()) {
                    mkdirs()
                }
            }
        }
    }

    /**
     * 获取所有日志文件列表
     * @return 按修改时间倒序排列的日志文件数组（最新的在前）
     */
    fun getLogFiles(): Array<File> {
        val logDir = getLogDirectory() ?: return emptyArray()

        return if (logDir.exists() && logDir.isDirectory) {
            // 过滤出以TAG开头且以.log结尾的文件，并按修改时间倒序排列
            logDir.listFiles { file ->
                file.isFile && file.name.startsWith(TAG) && file.name.endsWith(".log")
            }?.sortedByDescending { it.lastModified() }?.toTypedArray() ?: emptyArray()
        } else {
            emptyArray()
        }
    }

    /**
     * 删除指定的日志文件
     * @param file 要删除的日志文件
     * @return Boolean 删除是否成功
     */
    fun deleteLogFile(file: File): Boolean {
        return try {
            if (file.exists() && file.isFile) {
                file.delete()
            } else {
                false
            }
        } catch (e: SecurityException) {
            LogUtil.e(TAG, "删除文件权限不足: ${e.message}")
            false
        } catch (e: Exception) {
            LogUtil.e(TAG, "删除文件失败: ${e.message}")
            false
        }
    }

    /**
     * 删除所有日志文件
     * @return Boolean 是否全部删除成功
     */
    fun deleteAllLogFiles(): Boolean {
        val logFiles = getLogFiles()
        var allSuccess = true

        logFiles.forEach { file ->
            if (!deleteLogFile(file)) {
                allSuccess = false
            }
        }

        return allSuccess
    }

    /**
     * 根据文件名模式删除日志文件（支持通配符）
     * @param pattern 文件名模式，如 "FileLog-2025-*.log"
     * @return Boolean 是否删除成功
     */
    fun deleteLogFilesByPattern(pattern: String): Boolean {
        val logFiles = getLogFiles()
        var deletedCount = 0

        logFiles.forEach { file ->
            if (file.name.matches(Regex(pattern.replace("*", ".*")))) {
                if (deleteLogFile(file)) {
                    deletedCount++
                }
            }
        }

        return deletedCount > 0
    }

    fun clearFileContents(): Boolean {
        try {
            logFile?.let { file ->
                FileOutputStream(file).use { fos ->
                    fos.write(ByteArray(0)) // 清空文件内容
                }
                return true
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "清空日志文件失败: ${e.message}")
        }
        return false
    }

    fun clearFileContents(file: File): Boolean {
        try {
            FileOutputStream(file).use { fos ->
                fos.write(ByteArray(0)) // 清空文件内容
            }
            return true
        } catch (e: Exception) {
            LogUtil.e(TAG, "清空日志文件失败: ${e.message}")
        }
        return false
    }

    fun readFileFromInternalStorage(file: File?, lineBreak: Boolean = false): String {
        if (file == null || !file.exists()) return ""
        val stringBuilder = StringBuilder()
        try {
            FileInputStream(file).use { fis ->
                // 明确指定UTF-8编码
                InputStreamReader(fis, StandardCharsets.UTF_8).use { isr ->
                    BufferedReader(isr).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            if (lineBreak) {
                                stringBuilder.append(line).append("\n")
                            } else {
                                stringBuilder.append(line)
                            }
                        }
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            LogUtil.e(TAG, "文件未找到: ${e.message}")
        } catch (e: IOException) {
            LogUtil.e(TAG, "读取文件失败: ${e.message}")
        }
        return stringBuilder.toString()
    }

    fun readFileWithAutoDetection(file: File?): String {
        if (file == null || !file.exists()) return ""

        return try {
            FileInputStream(file).use { fis ->
                val charset = detectCharset(fis) ?: StandardCharsets.UTF_8
                InputStreamReader(fis, charset).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "读取文件失败: ${e.message}")
            ""
        }
    }

    //自动检测文件编码
    private fun detectCharset(inputStream: InputStream): Charset? {
        return try {
            inputStream.mark(4)
            val buffer = ByteArray(3)
            val bytesRead = inputStream.read(buffer)
            inputStream.reset()

            if (bytesRead >= 3) {
                when {
                    // UTF-8 BOM
                    buffer[0] == 0xEF.toByte() && buffer[1] == 0xBB.toByte() && buffer[2] == 0xBF.toByte() ->
                        return Charset.forName("UTF-8")
                    // UTF-16 Little Endian BOM
                    buffer[0] == 0xFF.toByte() && buffer[1] == 0xFE.toByte() ->
                        return Charset.forName("UTF-16LE")
                    // UTF-16 Big Endian BOM
                    buffer[0] == 0xFE.toByte() && buffer[1] == 0xFF.toByte() ->
                        return Charset.forName("UTF-16BE")
                }
            }

            // 默认使用UTF-8
            StandardCharsets.UTF_8
        } catch (e: Exception) {
            StandardCharsets.UTF_8
        }
    }

    // 从文件末尾读取内容（示例：读取最后一段日志）
    fun readFromBottom(
        file: File?,
        maxLines: Int = 10,
        reversed: Boolean = false,
        lineBreak: Boolean = false
    ): String {
        if (file == null || !file.exists()) return ""
        val lines = mutableListOf<String>()
        try {
            RandomAccessFile(file, "r").use { raf ->
                val charset = StandardCharsets.UTF_8 // 明确指定编码
                var fileLength = raf.length()
                val buffer = StringBuilder()

                for (i in 1..fileLength) {
                    raf.seek(fileLength - i)
                    val byteRead = raf.readByte().toInt().toChar()
                    if (byteRead == '\n') {
                        if (buffer.isNotEmpty()) {
                            lines.add(buffer.reverse().toString())
                            buffer.setLength(0)
                        }
                        if (lines.size >= maxLines) break
                    } else {
                        buffer.append(byteRead)
                    }
                }
                if (buffer.isNotEmpty()) {
                    lines.add(buffer.reverse().toString())
                }
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "从文件末尾读取失败: ${e.message}")
        }
        if (reversed) {
            if (lineBreak) {
                return lines.reversed().joinToString("\n")
            } else {
                return lines.reversed().joinToString()
            }
        } else {
            if (lineBreak) {
                return lines.joinToString("\n")
            } else {
                return lines.joinToString()
            }
        }
    }

    /**
     * 从文件末尾分页读取内容 (修复版)
     */
    fun readFromBottomWithPagination(
        file: File?,
        page: Int = 1,
        pageSize: Int = 20,
        reversed: Boolean = false,
        lineBreak: Boolean = false
    ): PagedReadResult {
        if (file == null || !file.exists()) {
            return PagedReadResult(emptyList(), 0, page, pageSize, 0)
        }

        val allLines = mutableListOf<String>()
        try {
            // 使用明确指定了UTF-8编码的方式读取所有行
            FileInputStream(file).use { fis ->
                InputStreamReader(fis, StandardCharsets.UTF_8).use { isr ->
                    BufferedReader(isr).use { reader ->
                        var line: String?
                        // readLine() 会自动去除行尾的换行符
                        while (reader.readLine().also { line = it } != null) {
                            // 直接将读取到的行内容（不含换行符）加入列表
                            line?.let {
                                if (lineBreak) {
                                    allLines.add("$it\n")
                                } else {
                                    allLines.add(it)
                                }
                            }
                        }
                    }
                }
            }

            val totalLines = allLines.size
            if (totalLines == 0) {
                return PagedReadResult(emptyList(), 0, page, pageSize, 0)
            }

            val totalPages = ceil(totalLines.toDouble() / pageSize).toInt()
            val currentPage = page.coerceIn(1, totalPages)

            // **修复点1：正确的分页计算逻辑**
            // 计算当前页数据在 allLines 列表中的起始和结束索引
            val startIndex = (totalLines - currentPage * pageSize).coerceAtLeast(0)
            val endIndex = totalLines - (currentPage - 1) * pageSize // 保证不越界

            // 获取当前页的数据子列表（由于日志是追加的，最新的在最后，所以这里取最后几行）
            if (reversed) {
                // 反转顺序，让最新的在最前面
                val pageLines = allLines.subList(startIndex, endIndex).reversed()
                return PagedReadResult(
                    lines = pageLines, // 此时 pageLines 中的每个字符串都是不包含换行符的纯文本行
                    totalLines = totalLines,
                    currentPage = currentPage,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            } else {
                val pageLines = allLines.subList(startIndex, endIndex)
                return PagedReadResult(
                    lines = pageLines, // 此时 pageLines 中的每个字符串都是不包含换行符的纯文本行
                    totalLines = totalLines,
                    currentPage = currentPage,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            }
        } catch (e: Exception) {
            LogUtil.e("$TAG: 分页读取失败: ${e.message}")
            return PagedReadResult(emptyList(), 0, page, pageSize, 0)
        }
    }

    /**
     * 读取最新一页的日志（便捷方法）
     * @param pageSize 每页行数
     */
    fun readLatestPage(pageSize: Int = 20): PagedReadResult {
        val logDir = getLogDirectory() ?: return PagedReadResult(emptyList(), 0, 1, pageSize, 0)
        val latestLogFile = getLogFiles().firstOrNull()
        return readFromBottomWithPagination(latestLogFile, 1, pageSize)
    }

    /**
     * 读取指定日期的日志文件分页内容
     * @param date 日期字符串，格式 yyyy-MM-dd
     * @param page 页码
     * @param pageSize 每页行数
     */
    fun readLogByDate(date: String, page: Int = 1, pageSize: Int = 20): PagedReadResult {
        val logDir = getLogDirectory() ?: return PagedReadResult(emptyList(), 0, page, pageSize, 0)
        val fileName = "$TAG-$date.log"
        val targetFile = File(logDir, fileName)

        return if (targetFile.exists()) {
            readFromBottomWithPagination(targetFile, page, pageSize)
        } else {
            PagedReadResult(emptyList(), 0, page, pageSize, 0)
        }
    }

    data class PagedReadResult(
        val lines: List<String>,
        val totalLines: Int,
        val currentPage: Int,
        val pageSize: Int,
        val totalPages: Int
    ) {
        /**
         * 是否有上一页
         */
        val hasPrevious: Boolean
            get() = currentPage > 1

        /**
         * 是否有下一页
         */
        val hasNext: Boolean
            get() = currentPage < totalPages
    }
}