package com.yuanquan.common.widget.selecttext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

import androidx.annotation.NonNull;

public class BitmapImageSpan extends DynamicDrawableSpan {
    private Drawable mDrawable;
    private Bitmap mPlaceholder;
    private Context mContext;
    private int mWidth;
    private int mHeight;

    public BitmapImageSpan(Context context, Bitmap placeholder) {
        this(context, placeholder, 0, 0);
    }

    public BitmapImageSpan(Context context, Bitmap placeholder, int width, int height) {
        super();
        mContext = context;
        mPlaceholder = placeholder;
        mWidth = width;
        mHeight = height;
        loadDrawable();
    }

    private void loadDrawable() {
        mDrawable = new BitmapDrawable(mContext.getResources(), mPlaceholder);
        mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
    }

    @Override
    public Drawable getDrawable() {
        return mDrawable;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        Drawable d = getDrawable();
        if (d != null) {
            canvas.save();
//            LogUtil.e("111111111111111    " + top);
//            LogUtil.e("222222222222222    " + bottom);
//            LogUtil.e("333333333333333    " + d.getBounds().top);
//            LogUtil.e("444444444444444    " + d.getBounds().bottom);
//            LogUtil.e("444444444444444    " + d.getIntrinsicHeight());
            int transY = bottom - d.getBounds().bottom + 10;
//            transY -= paint.getFontMetricsInt().descent / 2;
            // 计算绘制图片的垂直居中位置
//            int transY = y + (bottom - top - d.getBounds().bottom) / 2;
            canvas.translate(x, transY);
            d.draw(canvas);
            canvas.restore();
        }
    }

}
