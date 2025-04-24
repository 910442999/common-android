package com.yuanquan.common.utils

import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.ByteBuffer
import java.security.MessageDigest

class GrayCircleTransformation(
    private val borderWidth: Float = 0f,
    private val borderColor: Int = Color.TRANSPARENT
) : BitmapTransformation() {

    private val ID =
        "com.yuanquan.common.utils.GrayCircleTransformation(border=$borderWidth,$borderColor)"
    private val ID_BYTES = ID.toByteArray(CHARSET)

    override fun transform(
        pool: BitmapPool,
        source: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val size = source.width.coerceAtMost(source.height)
        val width = (source.width - size) / 2
        val height = (source.height - size) / 2

        val result = pool.get(size, size, Bitmap.Config.ARGB_8888)

        // 灰度处理
        val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }

        // 圆形裁剪
        val canvas = Canvas(result)
        val path = Path().apply {
            addCircle(size / 2f, size / 2f, size / 2f, Path.Direction.CCW)
        }

        // 绘制灰度圆形图片
        canvas.save()
        canvas.clipPath(path)
        canvas.drawBitmap(source, -width.toFloat(), -height.toFloat(), paint)
        canvas.restore()

        // 绘制边框（当borderWidth > 0时）
        if (borderWidth > 0) {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = borderColor
                style = Paint.Style.STROKE
                strokeWidth = borderWidth
                strokeCap = Paint.Cap.ROUND
            }
            val radius = size / 2f - borderWidth / 2
            canvas.drawCircle(size / 2f, size / 2f, radius, borderPaint)
        }

        return result
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
        messageDigest.update(ByteBuffer.allocate(4).putFloat(borderWidth).array())
        messageDigest.update(ByteBuffer.allocate(4).putInt(borderColor).array())
    }

    override fun equals(other: Any?): Boolean {
        return other is GrayCircleTransformation &&
                other.borderWidth == borderWidth &&
                other.borderColor == borderColor
    }

    override fun hashCode(): Int {
        var result = ID.hashCode()
        result = 31 * result + borderWidth.hashCode()
        result = 31 * result + borderColor
        return result
    }
}