package com.yuanquan.common.utils

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView


object CountDownTimerUtils {
    @SuppressLint("StaticFieldLeak")
    private var timer: CountDownTimer? = null

    /**
     * 由于该倒计时类会存在不会显示0秒,且最后1秒实际是接近2秒的时间,因此处理时将剩余秒数多减了一秒
     * 在创建timer时,倒计时的秒数应该多加1秒,自动计时类计时时会产生毫秒值得误差,如果去整数的值
     * 在计算时可能会出现跳秒的情况(实际倒计时的秒数差的不大,就几十毫秒),为了给计秒做补偿,多加500毫秒
     * 保证误差同时也能保证计秒准确
     *
     * @param second      需要设置的倒计时秒数
     * @param view        倒计时运行时需要设置文本变化的控件TextView或者Button
     * @param defaultText 计时结束后view上显示的内容
     */
    fun getTimer(second: Int, view: View, defaultText: String?) {
        timer = object : CountDownTimer((second * 1000 + 1500).toLong(), 1000) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                val remainderTime = millisUntilFinished / 1000 - 1
                //判断view是否是TextView,如果是就设置显示倒计时的文本(Button是TextView子类)
                //如果是TextView的话,设置显示倒计时同时设置view不可点击
                if (view is TextView) {
                    view.text = String.format("%ds", remainderTime)
                }
                if (remainderTime == 0L) {
                    //判断为1秒时,结束计时,并恢复view可以点击
                    onFinish()
                    cancel()
                }
            }

            override fun onFinish() {
                view.isClickable = true
                if (view is TextView) {
                    view.text = defaultText
                }
            }
        }
        //开启计时器
        timer?.start()
        //设置不能被点击
        view.isClickable = false
    }

    fun getTimer(second: Int, view: TextView, defaultText: String?, timerText: String?) {
        timer = object : CountDownTimer((second * 1000 + 1500).toLong(), 1000) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                val remainderTime = millisUntilFinished / 1000 - 1
                //判断view是否是TextView,如果是就设置显示倒计时的文本(Button是TextView子类)
                //如果是TextView的话,设置显示倒计时同时设置view不可点击
                view.text = String.format("%s%ds", timerText, remainderTime)
                if (remainderTime == 0L) {
                    //判断为1秒时,结束计时,并恢复view可以点击
                    onFinish()
                    cancel()
                }
            }

            override fun onFinish() {
                view.isEnabled = true
                view.text = defaultText
            }
        }
        //开启计时器
        timer?.start()
        //设置不能被点击
        view.isEnabled = false
    }

    /**
     * 取消计时器计时
     */
    fun cancelTimer() {
        if (timer != null) {
            timer?.cancel()
            timer?.onFinish()
        }
    }
}

