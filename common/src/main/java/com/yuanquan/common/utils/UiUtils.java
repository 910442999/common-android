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
}
