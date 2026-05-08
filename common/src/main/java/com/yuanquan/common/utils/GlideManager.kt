package com.yuanquan.common.utils

import android.content.Context
import android.graphics.Bitmap
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
import com.yuanquan.common.widget.CircleImageView
import com.yuanquan.common.utils.UiUtils
import java.util.Locale

/**
 * glide加载图片
 * 特性：
 * 1. resourceId 可选默认不传
 * 2. dontAnimate 全局可配置，默认 true
 * 3. 自动识别GIF：强制忽略dontAnimate，保证动图播放
 * 4. 普通图片按传入dontAnimate配置，防闪烁
 */
object GlideManager {

    /**
     * 判断是否是 GIF 图片
     */
    private fun isGif(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        var normalized = url.trim().lowercase(Locale.ROOT)
        val queryIndex = normalized.indexOf('?')
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex)
        }
        val hashIndex = normalized.indexOf('#')
        if (hashIndex >= 0) {
            normalized = normalized.substring(0, hashIndex)
        }
        return normalized.endsWith(".gif")
    }

    // ==================== 基础图片加载 ====================
    @JvmStatic
    fun image(
        context: Context,
        url: String?,
        iv: ImageView,
        resourceId: Int? = null,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL, // 新增参数
        dontAnimate: Boolean = true // 补充默认值
    ) {
        load(
            context = context,
            url = url,
            iv = iv,
            resourceId = resourceId,
            dontAnimate = dontAnimate,
            transformation = null,
            strategy = strategy // 透传参数
        )
    }

    @JvmStatic
    fun load(
        context: Context,
        url: String?,
        iv: ImageView,
        resourceId: Int? = null,
        dontAnimate: Boolean = true,
        transformation: Transformation<Bitmap?>? = null,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL // 补充默认值
    ) {
        load(
            context = context,
            url = url,
            iv = iv,
            resourceId = resourceId,
            skip = false,
            strategy = strategy, // 透传参数
            dontAnimate = dontAnimate,
            transformation = transformation
        )
    }

    @JvmStatic
    fun load(
        context: Context,
        url: String?,
        iv: ImageView,
        resourceId: Int? = null,
        skip: Boolean = false,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL,
        dontAnimate: Boolean = true,
        transformation: Transformation<Bitmap?>? = null
    ) {
        var builder = Glide.with(context)
            .load(url)
            .skipMemoryCache(skip)
            .diskCacheStrategy(strategy)

        // 核心规则：GIF 强制不设置 dontAnimate；普通图片按参数配置
        if (!isGif(url) && dontAnimate) {
            builder = builder.dontAnimate()
        }

        resourceId?.let {
            builder = builder.placeholder(it).error(it)
        }
        transformation?.let {
            builder = builder.transform(it)
        }

        builder.into(iv)
    }

    // ==================== 本地图片 ====================
    @JvmStatic
    fun localImage(
        context: Context,
        resId: Int,
        iv: ImageView,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL, // 新增参数
        dontAnimate: Boolean = true // 补充默认值
    ) {
        val builder = Glide.with(context)
            .load(resId)
            .skipMemoryCache(true)
            .diskCacheStrategy(strategy) // 使用传入的缓存策略
        if (dontAnimate) builder.dontAnimate()
        builder.into(iv)
    }

    // ==================== 圆角图片 ====================
    @JvmStatic
    fun circular(
        context: Context,
        url: String?,
        iv: ImageView,
        resourceId: Int? = null,
        roundingRadius: Int = 6,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL, // 新增参数
        dontAnimate: Boolean = true // 补充默认值
    ) {
        var builder = Glide.with(context)
            .load(url)
            .skipMemoryCache(true)
            .diskCacheStrategy(strategy) // 使用传入的缓存策略
            .apply(
                RequestOptions.bitmapTransform(
                    RoundedCorners(dp2Px(context, roundingRadius.toFloat()))
                )
            )

        if (!isGif(url) && dontAnimate) {
            builder = builder.dontAnimate()
        }

        resourceId?.let {
            builder = builder.placeholder(it).error(it)
        }
        builder.into(iv)
    }

    // ==================== GIF 专用 ====================
    @JvmStatic
    fun asGif(
        context: Context,
        resId: Int,
        imageView: ImageView
    ) {
        Glide.with(context)
            .asGif()
            .load(resId)
            .into(imageView)
    }

    @JvmStatic
    fun asGif(
        context: Context,
        resId: Int,
        imageView: ImageView,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL, // 新增参数
        dontAnimate: Boolean = true // 新增参数（GIF会自动忽略该参数）
    ) {
        Glide.with(context)
            .asGif()
            .load(resId)
            .diskCacheStrategy(strategy) // 使用传入的缓存策略
            .into(imageView)
    }

    @JvmStatic
    fun asGif(
        context: Context,
        url: String?,
        imageView: ImageView,
        resourceId: Int? = null,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL, // 新增参数
        dontAnimate: Boolean = true // 新增参数（GIF会自动忽略该参数）
    ) {
        var builder = Glide.with(context)
            .asGif()
            .load(url)
            .diskCacheStrategy(strategy) // 使用传入的缓存策略
        resourceId?.let {
            builder = builder.placeholder(it).error(it)
        }
        builder.into(imageView)
    }

    // ==================== 获取 Bitmap ====================
    @JvmStatic
    fun asBitmap(
        context: Context,
        url: String?,
        listener: OnBitmapListener?,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL, // 新增参数
        dontAnimate: Boolean = true // 补充默认值
    ) {
        asBitmap(context, url, null, strategy, dontAnimate, listener) // 透传参数
    }

    @JvmStatic
    fun asBitmap(
        context: Context,
        url: String?,
        resourceId: Int? = null,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL, // 新增参数
        dontAnimate: Boolean = true,
        listener: OnBitmapListener?
    ) {
        var builder = Glide.with(context).asBitmap().load(url)
            .diskCacheStrategy(strategy) // 使用传入的缓存策略
        if (!isGif(url) && dontAnimate) builder = builder.dontAnimate()
        resourceId?.let { builder = builder.placeholder(it).error(it) }
        builder.into(object : SimpleTarget<Bitmap?>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                listener?.onResourceReady(resource)
            }
        })
    }

    @JvmStatic
    fun asBitmap(
        context: Context,
        url: String?,
        resourceId: Int? = null,
        skip: Boolean = false,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL,
        dontAnimate: Boolean = true,
        listener: OnBitmapListener?
    ) {
        var builder = Glide.with(context).asBitmap().load(url)
            .skipMemoryCache(skip)
            .diskCacheStrategy(strategy)

        if (!isGif(url) && dontAnimate) builder = builder.dontAnimate()
        resourceId?.let { builder = builder.placeholder(it).error(it) }

        builder.into(object : SimpleTarget<Bitmap?>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                listener?.onResourceReady(resource)
            }
        })
    }

    // ==================== 获取 Drawable ====================
    @JvmStatic
    fun asDrawable(
        context: Context,
        url: String?,
        listener: OnDrawableListener?,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL, // 新增参数
        dontAnimate: Boolean = true // 补充默认值
    ) {
        asDrawable(context, url, null, strategy, dontAnimate, listener) // 透传参数
    }

    @JvmStatic
    fun asDrawable(
        context: Context,
        url: String?,
        resourceId: Int? = null,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL, // 新增参数
        dontAnimate: Boolean = true,
        listener: OnDrawableListener?
    ) {
        var builder = Glide.with(context).asDrawable().load(url)
            .diskCacheStrategy(strategy) // 使用传入的缓存策略
        if (!isGif(url) && dontAnimate) builder = builder.dontAnimate()
        resourceId?.let { builder = builder.placeholder(it).error(it) }
        builder.into(object : SimpleTarget<Drawable?>() {
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
        resourceId: Int? = null,
        skip: Boolean = false,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL,
        dontAnimate: Boolean = true,
        listener: OnDrawableListener?
    ) {
        var builder = Glide.with(context).asDrawable().load(url)
            .skipMemoryCache(skip)
            .diskCacheStrategy(strategy)

        if (!isGif(url) && dontAnimate) builder = builder.dontAnimate()
        resourceId?.let { builder = builder.placeholder(it).error(it) }

        builder.into(object : SimpleTarget<Drawable?>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable?>?
            ) {
                listener?.onResourceReady(resource)
            }
        })
    }

    // ==================== 圆形头像 ====================
    @JvmStatic
    fun headerImage(
        context: Context,
        headerImageUrl: String?,
        pvHeader: ImageView,
        resourceId: Int? = null,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL, // 新增参数
        dontAnimate: Boolean = true
    ) {
        var builder = Glide.with(context).load(headerImageUrl)
            .skipMemoryCache(false)
            .diskCacheStrategy(strategy) // 使用传入的缓存策略
            .apply(RequestOptions.bitmapTransform(CircleCrop()))

        if (!isGif(headerImageUrl) && dontAnimate) {
            builder = builder.dontAnimate()
        }

        resourceId?.let {
            builder = builder.placeholder(it).error(it)
        }
        builder.into(pvHeader)
    }

    @JvmStatic
    fun headerImage(
        context: Context,
        headerImageUrl: String?,
        pvHeader: ImageView,
        subNickName: String?,
        textSize: Float,
        resourceId: Int? = null,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL, // 新增参数
        dontAnimate: Boolean = true
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
                resourceId?.let {
                    val builder = Glide.with(context).load(it)
                        .skipMemoryCache(false)
                        .diskCacheStrategy(strategy) // 使用传入的缓存策略
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    if (dontAnimate) builder.dontAnimate()
                    builder.into(pvHeader)
                }
            }
        } else {
            headerImage(
                context,
                headerImageUrl,
                pvHeader,
                resourceId,
                strategy,
                dontAnimate
            ) // 透传参数
        }
    }

    // ==================== 灰色圆形头像 ====================
    @JvmStatic
    fun headerGrayscaleImage(
        context: Context,
        headerImageUrl: String?,
        pvHeader: CircleImageView,
        nickName: String?,
        textSize: Float,
        resourceId: Int? = null,
        strategy: DiskCacheStrategy = DiskCacheStrategy.ALL, // 新增参数
        dontAnimate: Boolean = true
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
            var builder = Glide.with(context).load(headerImageUrl)
                .skipMemoryCache(false)
                .diskCacheStrategy(strategy) // 使用传入的缓存策略

            if (!isGif(headerImageUrl) && dontAnimate) {
                builder = builder.dontAnimate()
            }

            resourceId?.let {
                builder = builder.placeholder(it).error(it)
            }
            builder.into(pvHeader)
        }
    }

    interface OnBitmapListener {
        fun onResourceReady(bitmap: Bitmap)
    }

    interface OnDrawableListener {
        fun onResourceReady(drawable: Drawable)
    }
}
