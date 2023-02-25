package com.yuanquan.common.utils;

import static android.view.View.DRAWING_CACHE_QUALITY_HIGH;

import android.graphics.Bitmap;
import android.text.Html;
import android.view.View;

public class UiUtils {
    /**
     * 设置Html.fromHtml(resource: String)
     */
    public static String setHtmlText(String text) {
        return Html.fromHtml(text).toString();
    }

    /**
     * 该方式原理主要是：View组件显示的内容可以通过cache机制保存为bitmap
     */

    public static Bitmap createBitmapFromView(View view) {
        Bitmap bitmap = null;
        //开启view缓存bitmap
        view.setDrawingCacheEnabled(true);
        //设置view缓存Bitmap质量
        view.setDrawingCacheQuality(DRAWING_CACHE_QUALITY_HIGH);
        //获取缓存的bitmap
        Bitmap cache = view.getDrawingCache();
        if (cache != null && !cache.isRecycled()) {
            bitmap = Bitmap.createBitmap(cache);
        }
        //销毁view缓存bitmap
        view.destroyDrawingCache();
        //关闭view缓存bitmap
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    /**
     * 设置屏幕常亮
     *
     * @param activity
     * @param b
     */
//    public static void theScreenIsAlwaysOn(Activity activity, boolean b) {
//        if (b) {
//            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        } else {
//            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        }
//    }

//    public static int getPhoneWidthPixels(Context context) {
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics var2 = new DisplayMetrics();
//        if (wm != null) {
//            wm.getDefaultDisplay().getMetrics(var2);
//        }
//
//        return var2.widthPixels;
//    }
//
//    public static int getPhoneHeightPixels(Context context) {
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics var2 = new DisplayMetrics();
//        if (wm != null) {
//            wm.getDefaultDisplay().getMetrics(var2);
//        }
//
//        return var2.heightPixels;
//    }
//
//    /**
//     * 判断是否是全面屏
//     */
//    private volatile static boolean mHasCheckAllScreen;
//    private volatile static boolean mIsAllScreenDevice;
//
//    public static boolean isAllScreenDevice(Context context) {
//        if (mHasCheckAllScreen) {
//            return mIsAllScreenDevice;
//        }
//        mHasCheckAllScreen = true;
//        mIsAllScreenDevice = false;
//        // 低于 API 21的，都不会是全面屏。。。
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            return false;
//        }
//        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        if (windowManager != null) {
//            Display display = windowManager.getDefaultDisplay();
//            Point point = new Point();
//            display.getRealSize(point);
//            float width, height;
//            if (point.x < point.y) {
//                width = point.x;
//                height = point.y;
//            } else {
//                width = point.y;
//                height = point.x;
//            }
//            if (height / width >= 1.97f) {
//                mIsAllScreenDevice = true;
//            }
//        }
//        return mIsAllScreenDevice;
//    }

//
//    /**
//     * 服务人数
//     *
//     * @param context 4、当服务数量<=9999时，显示”【数字】人学习“
//     *                5、当9999999>=服务数量>9999时，显示”【数字/10000】人学习”（保留1位小数）
//     *                6、当服务数量>9999999时，显示“超999万人学习”
//     */
//    public static String servicesNumber(Context context, int number) {
//        if (number > 99) {
//            if (number <= 9999) {
//                return number + context.getString(R.string.learn);
//            } else if (9999999 >= number && number > 9999) {
//                int i = number / 10000;
//                return String.format(context.getString(R.string.thousands_people_learn), String.valueOf(i));
//            } else if (number > 9999999) {
//                return String.format(context.getString(R.string.tens_thousands_people_learn), "999");
//            }
//        }
//        return context.getString(R.string.hot_registration);
//    }
//
//    /**
//     * 服务人数
//     *
//     * @param context 4、当服务数量<=9999时，显示”【数字】人学习“
//     *                5、当9999999>=服务数量>9999时，显示”【数字/10000】人学习”（保留1位小数）
//     *                6、当服务数量>9999999时，显示“超999万人学习”
//     */
//    public static String servicesNumberSearch(Context context, int number) {
//        if (number > 99) {
//            if (number <= 9999) {
//                return String.format(context.getString(R.string.services_number), String.valueOf(number));
//            } else if (9999999 >= number && number > 9999) {
//                int i = number / 10000;
//                return String.format(context.getString(R.string.services_number_w), String.valueOf(i));
//            } else if (number > 9999999) {
//                return String.format(context.getString(R.string.services_number_w), "999");
//            }
//        }
//        return context.getString(R.string.hot_registration);
//    }
//
//    public static SpannableString servicesNumberSearch(Context context, int number, int color) {
//        String format = "";
//        String formatNumber = "";
//        if (number <= 9999) {
//            format = context.getString(R.string.services_number);
//            formatNumber = String.valueOf(number);
//        } else if (9999999 >= number && number > 9999) {
//            int i = number / 10000;
//            format = context.getString(R.string.services_number_w);
//            formatNumber = String.valueOf(i);
//        } else if (number > 9999999) {
//            format = context.getString(R.string.services_number_w);
//            formatNumber = "999";
//        }
//        String format1 = String.format(format, formatNumber);
//        int index = format1.indexOf(formatNumber);
//        SpannableString spannableString = new SpannableString(format1);
//        ForegroundColorSpan colorSpan = new ForegroundColorSpan(context.getResources().getColor(color));
//        spannableString.setSpan(
//                colorSpan, index, index + formatNumber.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE
//        );
//        return spannableString;
//
//    }
}
