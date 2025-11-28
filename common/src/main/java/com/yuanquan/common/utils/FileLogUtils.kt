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
import java.io.InputStreamReader
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogUtils {
    private const val TAG = "FileLog"
    private var logFile: File? = null
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext // 使用 ApplicationContext 避免内存泄漏
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
            val log = "[$timestamp][$TAG][$level] $message\n"
            // 使用 BufferedWriter 追加写入，确保资源正确关闭
            BufferedWriter(FileWriter(logFile, true)).use { writer ->
                writer.append(log)
                writer.flush() // 确保数据写入磁盘
            }
        } catch (e: Exception) {
            LogUtil.e(message)
        }
    }

    fun writeMemory(level: String, message: String) {
        try {
            val timestamp =
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss.SSS",
                    Locale.getDefault()
                ).format(Date())
            val log = "[$timestamp][$TAG][$level] $message\n"
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

    fun clearFileContents() {
        try {
            logFile?.let { file ->
                FileOutputStream(file).use { fos ->
                    fos.write(ByteArray(0)) // 清空文件内容
                }
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "清空日志文件失败: ${e.message}")
        }
    }

    fun readFileFromInternalStorage(file: File?): String {
        if (file == null || !file.exists()) return ""
        val stringBuilder = StringBuilder()
        try {
            FileInputStream(file).use { fis ->
                InputStreamReader(fis).use { isr ->
                    BufferedReader(isr).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            stringBuilder.append(line).append("\n")
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

    // 从文件末尾读取内容（示例：读取最后一段日志）
    fun readFromBottom(file: File?, maxLines: Int = 10): String {
        if (file == null || !file.exists()) return ""
        val lines = mutableListOf<String>()
        try {
            RandomAccessFile(file, "r").use { raf ->
                var fileLength = raf.length()
                val buffer = StringBuilder()
                // 从文件末尾开始读取
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
        return lines.reversed().joinToString("\n")
    }
}

// 示例用法（在 Activity 或 Fragment 中）：
// class MainActivity : AppCompatActivity() {
//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         FileLogUtils.init(applicationContext)
//         FileLogUtils.d("Application started")
//         // 测试读取日志
//         val logContent = FileLogUtils.readFileFromInternalStorage(FileLogUtils.getLogFile(this))
//         LogUtil.d("MainActivity", "Log content: $logContent")
//     }
// }
