package com.yuanquan.common

import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期格式化工具类
 * 格式示例：Tue Apr 1 3:11PM
 */
object DateTimeFormatter {

    /**
     * 获取当前时间的格式化字符串
     */
    fun getCurrentFormatted(): String {
        return formatDate(Date())
    }

    /**
     * 格式化指定日期对象
     * @param date 需要格式化的日期对象
     * @param timeZone 时区（默认系统时区）
     */
    fun formatDate(
        date: Date,
        timeZone1: TimeZone = TimeZone.getDefault()
    ): String {
        return SimpleDateFormat("EEE MMM d h:mm a", Locale.US).apply {
            timeZone = timeZone1
        }.format(date)
    }

    /**
     * 格式化指定日期对象
     * @param date 需要格式化的日期对象
     * @param timeZone 时区（默认系统时区）
     */
    fun formatDate2(
        date: Date,
        timeZone1: TimeZone = TimeZone.getDefault()
    ): String {
        return SimpleDateFormat("EEE MMM d", Locale.US).apply {
            timeZone = timeZone1
        }.format(date)
    }

    /**
     * 格式化指定日期对象
     * @param date 需要格式化的日期对象
     * @param timeZone 时区（默认系统时区）
     */
    fun formatDate3(
        date: Date,
        timeZone1: TimeZone = TimeZone.getDefault()
    ): String {
        return SimpleDateFormat("h:mm a", Locale.US).apply {
            timeZone = timeZone1
        }.format(date)
    }

    /**
     * 格式化时间戳
     * @param timestamp 毫秒时间戳
     */
    fun formatTimestamp(timestamp: Long): String {
        return formatDate(Date(timestamp))
    }
}