package com.yuanquan.common.utils

import android.Manifest
import android.content.Context
import android.os.Build
import com.yuanquan.common.utils.permissions.PermissionUtils
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
    private const val TAG = "FileLogUtils"
    private const val LOG_FILE_NAME = "appLog.log" // 日志文件名
    private lateinit var logFile: File
    private var context: Context? = null
    fun init(context: Context) {
        this.context = context
        logFile = getLogFile(context)
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
            if (SysUtils.isTablet(context!!)) {
                writeFile(level, message)
            } else {
                var hasPermissions = PermissionUtils.hasPermissions(
                    context!!, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                )
                if (hasPermissions) {
                    writeFile(level, message)
                } else {
                    writeMemory(level, message)
                }
            }
        } else {
            LogUtil.e("写入日志未初始化")
        }
    }

    private fun writeFile(level: String, message: String) {
        try {
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
            LogUtil.e(e.printStackTrace())
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
            LogUtil.e(e.printStackTrace())
        }
    }

    /**
     * 内部存储目录：如果您在 getLogDirectory() 方法中选择了内部存储目录（对应 Android 10 以下版本），则日志文件将保存在应用的内部存储目录中。内部存储目录的位置通常是 /data/data/<应用包名>/files。例如，如果您的应用包名是 "com.example.myapp"，则日志文件的完整路径将是 /data/data/com.example.myapp/files/app_log.txt。
     *
     * 外部存储目录：如果您在 getLogDirectory() 方法中选择了外部存储目录（对应 Android 10 及以上版本），则日志文件将保存在应用的外部存储目录中。外部存储目录的位置通常是 /storage/emulated/0/Android/data/<应用包名>/files。例如，如果您的应用包名是 "com.example.myapp"，则日志文件的完整路径将是 /storage/emulated/0/Android/data/com.example.myapp/files/app_log.txt。
     * 如设备中无法查看，需要链接电脑查看文件
     */
    private fun getLogDirectory(context: Context): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getExternalFilesDir(null)!!
        } else {
            context.filesDir
        }
    }

    fun getLogFile(context: Context): File {
        val logDir = getLogDirectory(context)
        if (!logDir.exists()) {
            logDir.mkdir()
        }
        logFile = File(logDir, LOG_FILE_NAME)
        return logFile
    }

    fun clearFileContents() {
        try {
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