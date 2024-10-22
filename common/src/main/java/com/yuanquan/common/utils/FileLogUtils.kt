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
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object FileLogUtils {
    private const val TAG = "FileLog"
    private var logFile: File? = null
    private var context: Context? = null
    private val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    fun init(context: Context) {
        this.context = context
        logFile = this.getLogFile(context)
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

    private fun writeLog(level: String, message: String) {
        //部分PAD（联想小新）获取权限为拒绝但实际有权限
        if (context != null) {
            writeFile(level, message)
        } else {
            LogUtil.e("写入日志未初始化")
        }
    }

    private fun writeFile(level: String, message: String) {
        try {
            if (logFile == null) {
                LogUtil.e(message)
                return
            }
            val timestamp =
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss.SSS",
                    Locale.getDefault()
                ).format(Date())
            val log = "[$timestamp][$level][$TAG] $message\n"
            BufferedWriter(FileWriter(logFile, true)).use { writer ->
                writer.append(log)
            }
            LogUtil.e("写入文件日志：" + message)
        } catch (e: Exception) {
            LogUtil.e(message)
        }
    }

    private fun writeMemory(level: String, message: String) {
        try {
            val timestamp =
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss.SSS",
                    Locale.getDefault()
                ).format(Date())
            val log = "[$timestamp][$level][$TAG] $message\n"
            var string = SPUtils.getInstance().getString(TAG)
            var s = string + log
            SPUtils.getInstance().put(TAG, s)
            LogUtil.e("写入内存日志：" + message)
        } catch (e: Exception) {
            LogUtil.e(message)
        }
    }

    fun getLogFile(context: Context): File? {
        val picturesDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        } else {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        }
        if (picturesDir == null) return null
        val path =
            picturesDir.path + File.separator + TAG + File.separator
        val logDir = File(path)
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        val time = formatter.format(Date())
        val fileName = "$TAG-$time.log"
        logFile = File(logDir, fileName)
        return logFile
    }

    fun clearFileContents() {
        try {
            if (logFile == null) {
                return
            }
            FileOutputStream(logFile).use { fos ->
                // 将文件清空，即写入0字节
                fos.write(ByteArray(0))
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    fun readFileFromInternalStorage(file: File?): String {
        val stringBuilder = StringBuilder()
        try {
            val fis = FileInputStream(file)
            val isr = InputStreamReader(fis)
            val bufferedReader = BufferedReader(isr)
            var line: String?
            while ((bufferedReader.readLine().also { line = it }) != null) {
                stringBuilder.append(line).append("\n")
            }
            bufferedReader.close()
            // 使用sb.toString()获取文件内容
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }

    fun readFromBottom(file: File?): String {
        try {
            val randomAccessFile = RandomAccessFile(file, "r")
            var fileLength = randomAccessFile.length()
            // 移动到文件末尾
            randomAccessFile.seek(fileLength)
            val content = java.lang.StringBuilder()
            // 从文件末尾开始读取每一行
            while (fileLength > 0 && randomAccessFile.filePointer < fileLength) {
                randomAccessFile.seek(--fileLength)
                if (randomAccessFile.readByte() == '\n'.code.toByte()) {
                    randomAccessFile.readLine() // Skip the current empty line
                    val line = randomAccessFile.readLine()
                    if (line != null) {
                        content.insert(0, line + "\n")
                    }
                    break
                }
            }
            randomAccessFile.close()
            return content.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}