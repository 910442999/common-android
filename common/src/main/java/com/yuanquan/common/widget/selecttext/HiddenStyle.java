package com.yuanquan.common.widget.selecttext;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

public class HiddenStyle extends CharacterStyle implements UpdateAppearance {
    @Override
    public void updateDrawState(TextPaint paint) {
        // 隐藏文本的样式
        paint.setColor(Color.TRANSPARENT); // 将文本颜色设置为透明
        paint.setAlpha(0); // 设置透明度为0
        paint.setStyle(Paint.Style.FILL_AND_STROKE); // 设置填充和描边样式
        paint.setStrokeWidth(0); // 设置描边宽度为0
    }
}