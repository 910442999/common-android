package com.yuanquan.common.utils

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Process
import android.os.SystemClock
import android.view.View
import java.util.regex.Pattern

object CommonUtils {
    @JvmStatic
    fun killProcessApp(context: Context, clazz: Class<*>?) {
        killProcessApp(context, 0, clazz)
    }

    @JvmStatic
    fun killProcessApp(context: Context, position: Int, clazz: Class<*>?) {
        Handler().postDelayed({ //重启app,这一步一定要加上，如果不重启app，可能打开新的页面显示的语言会不正确
            val intent = Intent(context, clazz)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra("position", position)
            context.startActivity(intent)
            Process.killProcess(Process.myPid())
            System.exit(0)
        }, 600)
    }

    // 需要点击几次 就设置几
    var mHits: LongArray? = null

    @JvmStatic
    fun onTestDisplaySetting(context: Context?, onClickListener: View.OnClickListener) {
        if (mHits == null) {
            mHits = LongArray(5)
        }
        System.arraycopy(mHits, 1, mHits, 0, mHits!!.size - 1) //把从第二位至最后一位之间的数字复制到第一位至倒数第一位
        mHits!![mHits!!.size - 1] = SystemClock.uptimeMillis() //记录一个时间
        if (SystemClock.uptimeMillis() - mHits!![0] <= 1000) { //一秒内连续点击。
            mHits = null //这里说明一下，我们在进来以后需要还原状态，否则如果点击过快，第六次，第七次 都会不断进来触发该效果。重新开始计数即可
            onClickListener.onClick(null)
        }
    }


    //    /**
    //     * 获取唯一识别码
    //     *
    //     * @param context
    //     * @return
    //     */
    //    public static String getUniqueIdentificationCode(Context context) {
    //        String getUniqueIdentificationCode = PreferenceUtil.getString("getUniqueIdentificationCode");
    //        if (TextUtils.isEmpty(getUniqueIdentificationCode)) {
    //            getUniqueIdentificationCode = SystemUtils.getInstallationGUID(context);
    //            PreferenceUtil.putString("getUniqueIdentificationCode", getUniqueIdentificationCode);
    //        }
    //        return getUniqueIdentificationCode;
    //    }
    //获取剪切板内容
    @JvmStatic
    fun getClipboardContent(context: Context): String? {
        var str: String? = null
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        //无数据时直接返回
        if (!clipboard.hasPrimaryClip()) {
            return str
        }
        //如果是文本信息
        if (clipboard.primaryClipDescription!!.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
            clipboard.primaryClipDescription!!.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)
        ) {
            val cdText = clipboard.primaryClip
            if (cdText != null) {
                val item = cdText.getItemAt(0)
                //此处是TEXT文本信息
                if (item.text != null) {
                    str = item.text.toString()
                    val clip = ClipData.newPlainText("", "")
                    clipboard.setPrimaryClip(clip)
                    return str
                }
            }
        }
        return str
    }

    /**
     * 复制
     *
     * @param url
     */
    @JvmStatic
    fun copy(context: Context, url: String?) {
        //获取剪贴板管理器：
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // 创建普通字符型ClipData
        val mClipData = ClipData.newPlainText("Label", url)
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData)
    }

    @JvmStatic
    fun setWidthHeight(view: View, width: Int, bili: Float) {
        val heightFloat = (width / bili).toInt()
        val layoutParams = view.layoutParams
        layoutParams.width = width
        layoutParams.height = heightFloat
        view.layoutParams = layoutParams
    }

    /**
     * 是否是纯数字正则
     *
     * @param data
     * @return
     */
    @JvmStatic
    fun isNumeric(data: String?): Boolean {
        val matches = Pattern.matches("[0-9]*", data)
        if (!matches) {
            return false
        }
        return true
    }

    @JvmStatic
    fun validatePassword(password: String?): Boolean {
        val x = "^(?![A-Z]*$)(?![a-z]*$)(?![0-9]*$)(?![^a-zA-Z0-9]*$)\\S+$" //4选2
        //        x = "^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\\W_]+$)(?![a-z0-9]+$)(?![a-z\\W_]+$)(?![0-9\\W_]+$)[a-zA-Z0-9\\W_]{8,16}$";//4选三
        if (Pattern.matches(x, password)) {
            return true
        }
        return false
    }

    var regEx: String = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"

    @JvmStatic
    fun validateEmail(email: String): Boolean {
        val matcherObj = Pattern.compile(regEx).matcher(email)
        return matcherObj.matches()
    }
}