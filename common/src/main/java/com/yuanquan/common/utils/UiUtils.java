package com.yuanquan.common.utils;

import static android.view.View.DRAWING_CACHE_QUALITY_HIGH;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

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
}
