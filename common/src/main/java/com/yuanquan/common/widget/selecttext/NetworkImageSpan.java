package com.yuanquan.common.widget.selecttext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

public class NetworkImageSpan extends DynamicDrawableSpan {
    private Drawable mDrawable;
    private Bitmap mPlaceholder;
    private Bitmap bitmap;
    private String mUrl;
    private Context mContext;
    private int mWidth;
    private int mHeight;

    public NetworkImageSpan(Context context, String url, Bitmap placeholder) {
        this(context, url, placeholder, 0, 0);
    }


    public NetworkImageSpan(Context context, String url, Bitmap placeholder, int width, int height) {
        super();
        mContext = context;
        mUrl = url;
        mPlaceholder = placeholder;
        mWidth = width;
        mHeight = height;
        loadDrawable();
    }

    public NetworkImageSpan(Context context, Bitmap bitmap, Bitmap placeholder, int width, int height) {
        super();
        mContext = context;
        this.bitmap = bitmap;
        mPlaceholder = placeholder;
        mWidth = width;
        mHeight = height;
        loadDrawable();
    }

    private void loadDrawable() {
        if (bitmap != null) {
            mDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
            if (mWidth > 0 && mHeight > 0) {
                mDrawable.setBounds(0, 0, mWidth, mHeight);
            } else {
                mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
            }
        } else {
            mDrawable = new BitmapDrawable(mContext.getResources(), mPlaceholder);
            mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
            RequestOptions requestOptions = new RequestOptions()
                    .transform(new CircleCrop())
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            Glide.with(mContext)
                    .asBitmap()
                    .load(mUrl)
                    .apply(requestOptions)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            // 图片加载失败时的处理，可根据需求进行适当操作
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            mDrawable = new BitmapDrawable(mContext.getResources(), resource);
                            if (mWidth > 0 && mHeight > 0) {
                                mDrawable.setBounds(0, 0, mWidth, mHeight);
                            } else {
                                mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
                            }
                            return false;
                        }
                    }).apply(requestOptions)
//                    .preload(); // 提前加载图片到缓存
                    .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        }
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
