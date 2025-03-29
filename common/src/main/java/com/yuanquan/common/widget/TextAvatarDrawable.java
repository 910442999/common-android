package com.yuanquan.common.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class TextAvatarDrawable extends Drawable {
    private final Paint backgroundPaint;
    private final Paint textPaint;
    private final String text;
    private final int size;

    public TextAvatarDrawable(String text, int color, int size) {
        this.text = (text == null || text.isEmpty()) ? "" : text.substring(0, 1);
        this.size = size;

        // 初始化背景画笔
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(color);

        // 初始化文字画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size * 0.5f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void draw(Canvas canvas) {
        // 绘制圆形背景
        canvas.drawCircle(size/2f, size/2f, size/2f, backgroundPaint);

        // 计算文字位置
        Rect textBounds = new Rect();
        textPaint.getTextBounds(text, 0, 1, textBounds);
        float y = size/2f - (textBounds.top + textBounds.bottom)/2f;

        // 绘制文字
        canvas.drawText(text, size/2f, y, textPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        backgroundPaint.setAlpha(alpha);
        textPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        backgroundPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return size;
    }

    @Override
    public int getIntrinsicHeight() {
        return size;
    }
}