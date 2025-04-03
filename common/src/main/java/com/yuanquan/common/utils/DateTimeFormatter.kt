package com.yuanquan.common.utils

import android.content.Context
import com.yuanquan.common.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期格式化工具类
 * 格式示例：Tue Apr 1 3:11PM
 */
object DateTimeFormatter {

    /**
     * 格式化指定日期对象
     * @param date 需要格式化的日期对象
     * @param timeZone 时区（默认系统时区）
     */
    fun formatDate(
        context: Context,
        date: Date,
        timeZone: Locale = Locale.getDefault()
    ): String {
        val dateFormat: String = context.getString(R.string.format_date_year_week)
        var simpleDateFormat = SimpleDateFormat(dateFormat, timeZone)
        return simpleDateFormat.format(date)
    }

    /**
     * 格式化中文日期
     * @param date 需要格式化的日期对象
     * @param timeZone 时区（默认系统时区）
     */
    fun formatDate2(
        context: Context,
        date: Date,
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        val dateFormat: String = context.getString(R.string.format_date_week)
        return SimpleDateFormat(dateFormat, Locale.getDefault()).apply {
            this.timeZone = timeZone
        }.format(date)
    }

    fun formatDate3(
        context: Context,
        date: Date,
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        val dateFormat: String = context.getString(R.string.format_time)
        return SimpleDateFormat(dateFormat, Locale.getDefault()).apply {
            this.timeZone = timeZone
        }.format(date)
    }
}