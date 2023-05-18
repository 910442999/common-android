package com.yuanquan.common.utils;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.os.Process.killProcess;
import static android.os.Process.myPid;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {

    public static void killProcessApp(Context context, Class clazz) {
        killProcessApp(context, 0, clazz);
    }

    public static void killProcessApp(Context context, int position, Class clazz) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //重启app,这一步一定要加上，如果不重启app，可能打开新的页面显示的语言会不正确
                Intent intent = new Intent(context, clazz);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("position", position);
                context.startActivity(intent);
                killProcess(myPid());
                System.exit(0);
            }
        }, 600);
    }

    /**
     * 重启app
     *
     * @param context
     * @param position
     * @param clazz
     */
    public static void reStart(Context context, int position, Class clazz) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //重启app,这一步一定要加上，如果不重启app，可能打开新的页面显示的语言会不正确
                Intent intent = new Intent(context, clazz);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("position", position);
                context.startActivity(intent);
            }
        }, 600);
    }

    // 需要点击几次 就设置几
    static long[] mHits = null;

    public static void onTestDisplaySetting(Context context, View.OnClickListener onClickListener) {
        if (mHits == null) {
            mHits = new long[5];
        }
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);//把从第二位至最后一位之间的数字复制到第一位至倒数第一位
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();//记录一个时间
        if (SystemClock.uptimeMillis() - mHits[0] <= 1000) {//一秒内连续点击。
            mHits = null;    //这里说明一下，我们在进来以后需要还原状态，否则如果点击过快，第六次，第七次 都会不断进来触发该效果。重新开始计数即可
            onClickListener.onClick(null);
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
    public static String getClipboardContent(Context context) {
        String str = null;
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        //无数据时直接返回
        if (!clipboard.hasPrimaryClip()) {
            return str;
        }
        //如果是文本信息
        if (clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            ClipData cdText = clipboard.getPrimaryClip();
            if (cdText != null) {
                ClipData.Item item = cdText.getItemAt(0);
                //此处是TEXT文本信息
                if (item.getText() != null) {
                    str = item.getText().toString();
                    ClipData clip = ClipData.newPlainText("", "");
                    clipboard.setPrimaryClip(clip);
                    return str;
                }
            }
        }
        return str;
    }

    /**
     * 复制
     *
     * @param url
     */
    public static void copy(Context context, String url) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", url);
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
    }

    public static void setWidthHight(View view, int width, float bili) {
        int heightFloat = (int) (width / bili);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = heightFloat;
        view.setLayoutParams(layoutParams);
    }

    /**
     * 是否是纯数字正则
     *
     * @param data
     * @return
     */
    public static boolean isNumeric(String data) {
        boolean matches = Pattern.matches("[0-9]*", data);
        if (!matches) {
            return false;
        }
        return true;
    }

    public static boolean validatePassword(String password) {
        String x = "^(?![A-Z]*$)(?![a-z]*$)(?![0-9]*$)(?![^a-zA-Z0-9]*$)\\S+$";//4选2
        //        x = "^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\\W_]+$)(?![a-z0-9]+$)(?![a-z\\W_]+$)(?![0-9\\W_]+$)[a-zA-Z0-9\\W_]{8,16}$";//4选三
        if (Pattern.matches(x, password)) {
            return true;
        }
        return false;
    }

    static String regEx = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    public static boolean validateEmail(String email) {
        Matcher matcherObj = Pattern.compile(regEx).matcher(email);
        return matcherObj.matches();
    }

}
