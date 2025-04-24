package com.yuanquan.common.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.yuanquan.common.R
import com.yuanquan.common.utils.SysUtils.dp2Px
import com.yuanquan.common.widget.TextAvatarDrawable
import androidx.core.graphics.toColorInt

/**
 * glide加载图片
 */
object GlideManager {
    /**
     * 用glide加载图片
     *
     * @param context
     * @param url
     * @param imageView
     */
    @JvmStatic
    fun image(context: Context, url: String?, imageView: ImageView) {
        image(context, url, imageView, R.mipmap.empty1)
    }

    /**
     * 网络图片
     *
     * @param context
     * @param url
     * @param iv
     * @param resourceId 展位图片
     */
    @JvmStatic
    fun image(context: Context, url: String?, iv: ImageView, resourceId: Int) {
        load(context, url, iv, resourceId, null)
    }

    /**
     * 网络图片
     *
     * @param context
     * @param url
     * @param iv
     * @param resourceId 展位图片
     */
    @JvmStatic
    fun load(
        context: Context,
        url: String?,
        iv: ImageView,
        resourceId: Int,
        transformation: Transformation<Bitmap?>?
    ) {
        load(context, url, iv, resourceId, true, DiskCacheStrategy.ALL, transformation)
    }

    /**
     * 网络图片
     *
     * @param context
     * @param url
     * @param iv
     * @param resourceId 展位图片
     */
    @JvmStatic
    fun load(
        context: Context,
        url: String?,
        iv: ImageView,
        resourceId: Int,
        skip: Boolean,
        strategy: DiskCacheStrategy,
        transformation: Transformation<Bitmap?>?
    ) {
        var builder = Glide.with(context) //这里写入url
            .load(url).placeholder(resourceId).error(resourceId).skipMemoryCache(skip)
            .diskCacheStrategy(strategy)
        if (transformation != null) {
            builder = builder.transform(transformation)
        }
        builder.into(iv)
    }

    /**
     * glide加载本地图片
     *
     * @param context
     * @param url
     * @param iv
     */
    @JvmStatic
    fun localImage(context: Context, url: Int, iv: ImageView) {
        Glide.with(context) //这里写入url
            .load(url).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.ALL).into(iv)
    }

    /**
     * 圆角图片
     *
     * @param context
     * @param url
     * @param iv
     * @param resourceId 图片占位图
     */
    /**
     * 圆角图片
     *
     * @param context
     * @param url
     * @param iv
     * @param resourceId 图片占位图
     */
    @JvmOverloads
    fun circular(
        context: Context,
        url: String?,
        iv: ImageView,
        resourceId: Int = R.mipmap.empty1,
        roundingRadius: Int = 6
    ) {
        Glide.with(context).load(url).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(resourceId).error(resourceId).apply(
                RequestOptions.bitmapTransform(
                    RoundedCorners(
                        dp2Px(context, roundingRadius.toFloat())
                    )
                )
            ) //                .transform(transformation)
            .into(iv)
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
    @JvmStatic
    fun asGif(context: Context, url: Int, imageView: ImageView) {
        Glide.with(context).asGif().load(url).into(imageView) //除非图像是动画gif ，否则将失败。
    }

    @JvmStatic
    fun asGif(context: Context, url: String?, imageView: ImageView) {
        Glide.with(context).asGif().load(url).into(imageView) //除非图像是动画gif ，否则将失败。
    }

    @JvmStatic
    fun asBitmap(context: Context, url: String?, listener: OnBitmapListener?) {
        Glide.with(context).asBitmap().load(url).into(object : SimpleTarget<Bitmap?>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                listener?.onResourceReady(resource)
            }
        })
    }

    @JvmStatic
    fun asBitmap(context: Context, url: String?, resourceId: Int, listener: OnBitmapListener?) {
        Glide.with(context).asBitmap().load(url).placeholder(resourceId).error(resourceId)
            .into(object : SimpleTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    listener?.onResourceReady(resource)
                }
            })
    }

    @JvmStatic
    fun asBitmap(
        context: Context,
        url: String?,
        resourceId: Int,
        skip: Boolean,
        strategy: DiskCacheStrategy,
        listener: OnBitmapListener?
    ) {
        Glide.with(context).asBitmap().load(url).placeholder(resourceId).error(resourceId)
            .skipMemoryCache(skip).diskCacheStrategy(strategy)
            .into(object : SimpleTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    listener?.onResourceReady(resource)
                }
            })
    }

    @JvmStatic
    fun asDrawable(context: Context, url: String?, listener: OnDrawableListener?) {
        Glide.with(context).asDrawable().load(url).into(object : SimpleTarget<Drawable?>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable?>?
            ) {
                listener?.onResourceReady(resource)
            }
        })
    }

    @JvmStatic
    fun asDrawable(context: Context, url: String?, resourceId: Int, listener: OnDrawableListener?) {
        Glide.with(context).asDrawable().load(url).placeholder(resourceId).error(resourceId)
            .into(object : SimpleTarget<Drawable?>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable?>?
                ) {
                    listener?.onResourceReady(resource)
                }
            })
    }

    @JvmStatic
    fun asDrawable(
        context: Context,
        url: String?,
        resourceId: Int,
        skip: Boolean,
        strategy: DiskCacheStrategy,
        listener: OnDrawableListener?
    ) {
        Glide.with(context).asDrawable().load(url).skipMemoryCache(skip).diskCacheStrategy(strategy)
            .placeholder(resourceId).error(resourceId).into(object : SimpleTarget<Drawable?>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable?>?
                ) {
                    listener?.onResourceReady(resource)
                }
            })
    }

    /**
     * 圆形图片
     *
     * @param context
     * @param headerImageUrl
     * @param pvHeader
     * @param resourceId     图片占位图
     */
    @JvmOverloads
    fun headerImage(
        context: Context,
        headerImageUrl: String?,
        pvHeader: ImageView,
        resourceId: Int = R.mipmap.icon_header
    ) {
        Glide.with(context).load(headerImageUrl).skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(resourceId).error(resourceId)
            .apply(RequestOptions.bitmapTransform(CircleCrop())).into(pvHeader)
    }

    @JvmOverloads
    fun headerImage(
        context: Context,
        headerImageUrl: String?,
        pvHeader: ImageView,
        subNickName: String?,
        textSize: Float,
        resourceId: Int = R.mipmap.icon_header
    ) {
        if (headerImageUrl.isNullOrEmpty()) {
            if (!subNickName.isNullOrEmpty()) {
                val avatarDrawable = TextAvatarDrawable(
                    subNickName,
                    UiUtils.getColourText(subNickName).toColorInt(),
                    dp2Px(context, textSize)
                )
                pvHeader.setImageDrawable(avatarDrawable)
            } else {
                Glide.with(context).load(resourceId).skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(resourceId)
                    .error(resourceId).apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(pvHeader)
            }
        } else {
            headerImage(context, headerImageUrl, pvHeader, resourceId)
        }
    }

    @JvmStatic
    fun headerGrayscaleImage(
        context: Context,
        headerImageUrl: String?,
        pvHeader: ImageView,
        nickName: String?,
        textSize: Float,
        resourceId: Int
    ) {
        if (headerImageUrl.isNullOrEmpty()) {
            if (!nickName.isNullOrEmpty()) {
                val avatarDrawable = TextAvatarDrawable(
                    nickName,
                    "#999999".toColorInt(),
                    dp2Px(context, textSize)
                )
                pvHeader.setImageDrawable(avatarDrawable)
            }
        } else {
            Glide.with(context).load(headerImageUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(resourceId)
                .error(resourceId)
                .apply(RequestOptions.bitmapTransform(GrayCircleTransformation())) // 将图片转换成灰色
                .dontAnimate()
                .into(pvHeader)
        }
    }

    interface OnBitmapListener {
        fun onResourceReady(bitmap: Bitmap?)
    }

    interface OnDrawableListener {
        fun onResourceReady(drawable: Drawable?)
    }
}