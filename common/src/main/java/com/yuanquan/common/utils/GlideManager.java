package com.yuanquan.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yuanquan.common.R;
import com.yuanquan.common.widget.TextAvatarDrawable;

/**
 * glide加载图片
 */

public class GlideManager {
    public interface OnBitmapListener {
        void onResourceReady(Bitmap bitmap);
    }

    public interface OnDrawableListener {
        void onResourceReady(Drawable drawable);
    }


    /**
     * 用glide加载图片
     *
     * @param context
     * @param url
     * @param imageView
     */
    public static void image(Context context, String url, ImageView imageView) {
        image(context, url, imageView, R.mipmap.empty1);
    }

    /**
     * 网络图片
     *
     * @param context
     * @param url
     * @param iv
     * @param resourceId 展位图片
     */
    public static void image(Context context, String url, ImageView iv, int resourceId) {
        load(context, url, iv, resourceId, null);
    }

    /**
     * 网络图片
     *
     * @param context
     * @param url
     * @param iv
     * @param resourceId 展位图片
     */
    public static void load(Context context, String url, ImageView iv, int resourceId, Transformation<Bitmap> transformation) {
        load(context, url, iv, resourceId, true, DiskCacheStrategy.ALL, transformation);
    }

    /**
     * 网络图片
     *
     * @param context
     * @param url
     * @param iv
     * @param resourceId 展位图片
     */
    public static void load(Context context, String url, ImageView iv, int resourceId, boolean skip, DiskCacheStrategy strategy, Transformation<Bitmap> transformation) {
        RequestBuilder<Drawable> builder = Glide.with(context)
                //这里写入url
                .load(url).placeholder(resourceId).error(resourceId).skipMemoryCache(skip).diskCacheStrategy(strategy);
        if (transformation != null) {
            builder = builder.transform(transformation);
        }
        builder.into(iv);
    }

    /**
     * glide加载本地图片
     *
     * @param context
     * @param url
     * @param iv
     */
    public static void localImage(Context context, int url, ImageView iv) {
        Glide.with(context)
                //这里写入url
                .load(url).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.ALL).into(iv);

    }

    public static void circular(Context context, String url, ImageView iv) {
        circular(context, url, iv, R.mipmap.empty1);
    }

    /**
     * 圆角图片
     *
     * @param context
     * @param url
     * @param iv
     * @param resourceId 图片占位图
     */
    public static void circular(Context context, String url, ImageView iv, int resourceId) {
        circular(context, url, iv, resourceId, 6);
    }

    /**
     * 圆角图片
     *
     * @param context
     * @param url
     * @param iv
     * @param resourceId 图片占位图
     */
    public static void circular(Context context, String url, ImageView iv, int resourceId, int roundingRadius) {
        Glide.with(context).load(url).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(resourceId).error(resourceId).apply(RequestOptions.bitmapTransform(new RoundedCorners(SysUtils.dp2Px(context, roundingRadius))))
                //                .transform(transformation)
                .into(iv);

    }

    //    public static void circularRound(Context context, String url, ImageView iv, int resourceId, int roundingRadius, boolean leftTop, boolean rightTop, boolean leftBottom, boolean rightBottom) {
    //        GlideRoundTransform glideRoundTransform = new GlideRoundTransform(context, DensityUtil.dp2px(context, roundingRadius));
    //        glideRoundTransform.setExceptCorner(leftTop, rightTop, leftBottom, rightBottom);
    //
    //        //        RoundedCornersTransform roundedCornersTransform = new RoundedCornersTransform(context, DensityUtil.dp2px(context, roundingRadius), leftTop, rightTop, leftBottom, rightBottom);
    //        //        RequestOptions transform = new RequestOptions().transform(
    //        //                new CenterCrop(), roundedCornersTransform
    //        //        );
    //        Glide.with(context).asBitmap().load(splicingImageUrl(url))
    //                .skipMemoryCache(true)
    //                .diskCacheStrategy(DiskCacheStrategy.ALL)
    //                .placeholder(resourceId)
    //                .error(resourceId)
    //                //                .apply(transform)
    //                .transform(glideRoundTransform)
    //                .into(iv);
    //
    //    }


    public static void asGif(Context context, int url, ImageView imageView) {
        Glide.with(context).asGif().load(url).into(imageView);//除非图像是动画gif ，否则将失败。
    }

    public static void asGif(Context context, String url, ImageView imageView) {
        Glide.with(context).asGif().load(url).into(imageView);//除非图像是动画gif ，否则将失败。
    }

    public static void asBitmap(Context context, String url, OnBitmapListener listener) {
        Glide.with(context).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (listener != null) listener.onResourceReady(resource);
            }
        });
    }

    public static void asBitmap(Context context, String url, int resourceId, OnBitmapListener listener) {
        Glide.with(context).asBitmap().load(url).placeholder(resourceId).error(resourceId).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (listener != null) listener.onResourceReady(resource);
            }
        });
    }

    public static void asBitmap(Context context, String url, int resourceId, boolean skip, DiskCacheStrategy strategy, OnBitmapListener listener) {
        Glide.with(context).asBitmap().load(url).placeholder(resourceId).error(resourceId).skipMemoryCache(skip).diskCacheStrategy(strategy).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (listener != null) listener.onResourceReady(resource);
            }
        });
    }

    public static void asDrawable(Context context, String url, OnDrawableListener listener) {
        Glide.with(context).asDrawable().load(url).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                if (listener != null) listener.onResourceReady(resource);
            }
        });
    }

    public static void asDrawable(Context context, String url, int resourceId, OnDrawableListener listener) {
        Glide.with(context).asDrawable().load(url).placeholder(resourceId).error(resourceId).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                if (listener != null) listener.onResourceReady(resource);
            }
        });
    }

    public static void asDrawable(Context context, String url, int resourceId, boolean skip, DiskCacheStrategy strategy, OnDrawableListener listener) {
        Glide.with(context).asDrawable().load(url).skipMemoryCache(skip).diskCacheStrategy(strategy).placeholder(resourceId).error(resourceId).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                if (listener != null) listener.onResourceReady(resource);
            }
        });
    }

    public static void headerImage(Context context, String headerImageUrl, ImageView pvHeader) {
        headerImage(context, headerImageUrl, pvHeader, R.mipmap.icon_header);
    }

    /**
     * 圆形图片
     *
     * @param context
     * @param headerImageUrl
     * @param pvHeader
     * @param resourceId     图片占位图
     */
    public static void headerImage(Context context, String headerImageUrl, ImageView pvHeader, int resourceId) {
        Glide.with(context).load(headerImageUrl).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(resourceId).error(resourceId).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(pvHeader);
    }

    public static void headerImage(Context context, String headerImageUrl, ImageView pvHeader, String colourCode, String subNickName, Float textSize) {
        headerImage(context, headerImageUrl, pvHeader, colourCode, subNickName, textSize, R.mipmap.icon_header);
    }

    public static void headerImage(Context context, String headerImageUrl, ImageView pvHeader, String colourCode, String subNickName, Float textSize, int resourceId) {
        if (headerImageUrl == null || headerImageUrl.isEmpty()) {
            if (subNickName != null && !subNickName.isEmpty()) {
                TextAvatarDrawable avatarDrawable = new TextAvatarDrawable(subNickName, Color.parseColor(colourCode), SysUtils.dp2Px(context, textSize));
                pvHeader.setImageDrawable(avatarDrawable);
            } else {
                Glide.with(context).load(resourceId).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(resourceId).error(resourceId).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(pvHeader);
            }
        } else {
            headerImage(context, headerImageUrl, pvHeader, resourceId);
        }
    }

}
