package com.yuanquan.common.utils

import android.os.SystemClock
import android.view.View

/***
 * 设置延迟时间的View扩展
 * @param delay Long 延迟时间，默认1000毫秒
 * @return T
 */
fun <T : View> T.withTrigger(delay: Long = 1000): T {
    triggerDelay = delay
    return this
}

/***
 * 点击事件的View扩展
 * @param block: (T) -> Unit 函数
 * @return Unit
 */
fun <T : View> T.onClick(time: Long = 300,block: (T) -> Unit) = setOnClickListener {
    if (onClickEnable(time)) {
        block(it as T)
    } else {
//        ToastUtils.show(MyApplication.getInstance().applicationContext.getString(R.string.click_toast))
    }
}

private var <T : View> T.triggerDelay: Long
    get() = if (getTag(1123461123) != null) getTag(1123461123) as Long else -1
    set(value) {
        setTag(1123461123, value)
    }

/***
 * 带延迟过滤的点击事件View扩展
 * @param delay Long 延迟时间，默认2000毫秒
 * @param block: (T) -> Unit 函数
 * @return Unit
 */
fun <T : View> T.clickWithTrigger(time: Long = 1000, block: (T) -> Unit) {
    triggerDelay = time
    setOnClickListener {
        if (onClickEnable(time)) {
            block(it as T)
        }
    }
}

private var <T : View> T.triggerLastTime: Long
    get() = if (getTag(1123460103) != null) getTag(1123460103) as Long else 0
    set(value) {
        setTag(1123460103, value)
    }

fun <T : View> T.onClickEnable(time: Long): Boolean {
    var flag = false
    val currentClickTime = System.currentTimeMillis()
    if (currentClickTime - triggerLastTime >= time) {
        flag = true
    }
    triggerLastTime = currentClickTime
    return flag
}

/**
 * 连续点击 n 次 触发点击事件
 */
fun <T : View> T.onClickDisplay(block: (T) -> Unit) = setOnClickListener {
    onClickDisplay(5, block)
}

fun <T : View> T.onClickDisplay(size: Int = 5, block: (T) -> Unit) = setOnClickListener {
    if (onClickDisplayEnable(size)) {
        block(it as T)
    }
}

// 需要点击几次 就设置几
var mHits: LongArray? = null
fun onClickDisplayEnable(size: Int): Boolean {
    var flag = false
    if (mHits == null) {
        mHits = LongArray(size)
    }
    System.arraycopy(mHits!!, 1, mHits, 0, mHits!!.size - 1) //把从第二位至最后一位之间的数字复制到第一位至倒数第一位
    mHits!![mHits!!.size - 1] = SystemClock.uptimeMillis() //记录一个时间
    if (SystemClock.uptimeMillis() - mHits!![0] <= 1000) { //一秒内连续点击。
        mHits = null //这里说明一下，我们在进来以后需要还原状态，否则如果点击过快，第六次，第七次 都会不断进来触发该效果。重新开始计数即可
        flag = true
    }
    return flag
}
