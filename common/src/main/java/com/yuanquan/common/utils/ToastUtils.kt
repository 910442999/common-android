package com.yuanquan.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.yuanquan.common.R
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference

object ToastUtils {
    private const val MIN_SHOW_INTERVAL = 1000L // 1秒间隔
    private var lastShowTime = 0L
    private var lastMessage: String? = null
    private val toastRef = AtomicReference<WeakReference<Toast>?>(null)
    private var customViewRef: WeakReference<View>? = null

    @JvmStatic
    fun show(context: Context, text: String?) {
        showInternal(context, text, -1, -1f)
    }

    @JvmStatic
    fun show(
        context: Context,
        @DrawableRes imageRes: Int,
        text: String?,
        textSize: Float = -1f
    ) {
        showInternal(context, text, imageRes, textSize)
    }

    @SuppressLint("InflateParams")
    private fun showInternal(
        context: Context,
        text: String?,
        @DrawableRes imageRes: Int,
        textSize: Float
    ) {
        if (text.isNullOrEmpty() || text == "null") return

        val now = System.currentTimeMillis()
        if (now - lastShowTime < MIN_SHOW_INTERVAL && text == lastMessage) return

        // 取消上一个Toast
        toastRef.get()?.get()?.cancel()

        // 复用或创建新Toast
        val toast = Toast(context.applicationContext).apply {
            val view = customViewRef?.get() ?: run {
                LayoutInflater.from(context).inflate(R.layout.custom_toast, null).also {
                    customViewRef = WeakReference(it)
                }
            }

            // 配置视图
            view.findViewById<TextView>(R.id.toast_text)?.apply {
                textSize.takeIf { it > 0 }?.let { setTextSize(it) }
                this.text = text
            }

            view.findViewById<ImageView>(R.id.toast_image)?.apply {
                visibility = if (imageRes != -1) View.VISIBLE else View.GONE
                setImageResource(imageRes)
            }

            setView(view)
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.TOP, 0, SysUtils.dp2Px(context = context, 100F))
        }

        // 显示并保存状态
        toast.show()
        lastShowTime = now
        lastMessage = text
        toastRef.set(WeakReference(toast))
    }

    fun cancel() {
        toastRef.get()?.get()?.cancel()
        lastMessage = null
        lastShowTime = 0L
    }
}