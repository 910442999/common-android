package com.yuanquan.common.utils

import android.os.SystemClock
import android.view.View
/**
 * 点击事件工具类
 */
object ClickUtils {
    /**
     * 设置延迟时间的View扩展
     * @param delay Long 延迟时间，默认1000毫秒
     * @return T
     */
    fun <T : View> T.withTrigger(delay: Long = 1000): T {
        triggerDelay = delay
        return this
    }

    /**
     * 普通点击事件（带防抖）
     * @param time 防抖时间间隔，默认300毫秒
     * @param block 点击回调
     */
    fun <T : View> T.onClick(time: Long = 300, block: (T) -> Unit) = setOnClickListener {
        if (isClickEnable(time)) {
            block(it as T)
        }
    }

    /**
     * 带延迟过滤的点击事件
     * @param delay 延迟时间，默认1000毫秒
     * @param block 点击回调
     */
    fun <T : View> T.clickWithTrigger(delay: Long = 1000, block: (T) -> Unit) {
        triggerDelay = delay
        setOnClickListener {
            if (isClickEnable(delay)) {
                block(it as T)
            }
        }
    }

    // 防抖点击状态
    private var <T : View> T.triggerDelay: Long
        get() = getTag(R.id.click_trigger_delay) as? Long ?: -1
        set(value) {
            setTag(R.id.click_trigger_delay, value)
        }

    // 上次点击时间
    private var <T : View> T.triggerLastTime: Long
        get() = getTag(R.id.click_last_time) as? Long ?: 0
        set(value) {
            setTag(R.id.click_last_time, value)
        }

    /**
     * 检查是否可点击（防抖）
     * @param interval 时间间隔
     */
    fun <T : View> T.isClickEnable(interval: Long): Boolean {
        val currentTime = SystemClock.elapsedRealtime()
        val lastTime = triggerLastTime
        val enable = currentTime - lastTime >= interval

        if (enable) {
            triggerLastTime = currentTime
        }

        return enable
    }
}

// 定义资源ID避免冲突
private object R {
    object id {
        const val click_trigger_delay = 0x7f000001
        const val click_last_time = 0x7f000002
    }
}