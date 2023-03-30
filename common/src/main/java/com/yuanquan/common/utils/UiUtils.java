package com.yuanquan.common.utils;

import android.text.Html;
import android.view.View;
import android.view.ViewGroup;

public class UiUtils {
    /**
     * 设置Html.fromHtml(resource: String)
     */
    public static String setHtmlText(String text) {
        return Html.fromHtml(text).toString();
    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            layoutParams.setMargins(l, t, r, b);
        }
    }
}
