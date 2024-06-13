package com.yuanquan.common.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yuanquan.common.utils.LogUtil;

/**
 * 文本中显示网络图片
 * 1、需要判断文本中是否有图片标签，有的还，则需要注意文本的宽高，以免图片因宽高不够，不显示
 */
public class GlideImageGetter implements Html.ImageGetter {
    private final Context mContext;
    private final TextView mTextView;
    private int mWidth;
    private OnClickListener onClickListener;

    public interface OnClickListener {
        void onClick(String url);
    }

    public GlideImageGetter(Context context, TextView textView, OnClickListener onClickListener) {
        mContext = context;
        mTextView = textView;
        this.onClickListener = onClickListener;
    }

    public GlideImageGetter(Context context, TextView textView, int width, OnClickListener onClickListener) {
        mContext = context;
        mTextView = textView;
        mWidth = width;
        this.onClickListener = onClickListener;
    }

    @Override
    public Drawable getDrawable(String source) {
        final UrlDrawable urlDrawable = new UrlDrawable();
        Glide.with(mContext).asBitmap().load(source).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                Drawable drawable = new BitmapDrawable(mContext.getResources(), resource);
                mTextView.post(() -> {
                    int width = 0;
                    if (mWidth > 0) {
                        width = mWidth;
                    } else {
                        width = mTextView.getWidth();
                    }
                    int height = Math.round(1.0f * width * resource.getHeight() / resource.getWidth());
                    drawable.setBounds(0, 0, width, height);
                    urlDrawable.setBounds(0, 0, width, height);
                    urlDrawable.setDrawable(drawable);

                    mTextView.setText(mTextView.getText()); // 触发文字重新绘制
                });
                mTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtil.e("html中的图片链接：" + source);
                        onClickListener.onClick(source);
                    }
                });
            }

            @Override
            public void onLoadCleared(Drawable placeholder) {
            }
        });
        return urlDrawable; // 返回包含占位图的UrlDrawable
    }

    // 用于持有下载图片的Drawable的占位类
    private class UrlDrawable extends BitmapDrawable {
        private Drawable drawable;

        @SuppressWarnings("deprecation")
        public UrlDrawable() {
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

        @Override
        public void draw(Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }
    }
}
