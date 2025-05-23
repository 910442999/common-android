package com.yuanquan.common.utils

import android.graphics.*
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class GrayscaleTransformation : BitmapTransformation() {

    // 唯一标识符用于缓存
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("grayscale_transformation".toByteArray(Key.CHARSET))
    }

    override fun transform(
        pool: BitmapPool,
        source: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        // 从缓存池获取可复用Bitmap
        val config = getNonNullConfig(source)
        val result = pool.get(outWidth, outHeight, config)
        result.setHasAlpha(source.hasAlpha())

        // 创建灰度画布
        val canvas = Canvas(result)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(createGrayscaleMatrix())
            isAntiAlias = true
            isDither = true
            isFilterBitmap = true
        }

        // 绘制处理后的图像
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    // 生成灰度颜色矩阵
    private fun createGrayscaleMatrix(): ColorMatrix {
        return ColorMatrix().apply {
            setSaturation(0f)
        }
    }

    // 获取非空Bitmap配置
    private fun getNonNullConfig(bitmap: Bitmap): Bitmap.Config {
        return bitmap.config ?: Bitmap.Config.ARGB_8888
    }

    override fun equals(other: Any?): Boolean {
        return other is GrayscaleTransformation
    }

    override fun hashCode(): Int {
        return "grayscale_transformation".hashCode()
    }
}