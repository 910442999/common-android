package com.yuanquan.common.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.yuanquan.common.R

class CircleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clipPath = Path()
    private val colorMatrix = ColorMatrix()
    private var drawMatrix = Matrix()
    private var borderWidth: Float = 0f
    private var borderColor: Int = Color.TRANSPARENT
    private var isGray: Boolean = false

    init {
        // 初始化自定义属性
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView)
        borderWidth = ta.getDimension(R.styleable.CircleImageView_circleImageBorderWidth, 0f)
        borderColor =
            ta.getColor(R.styleable.CircleImageView_circleImageBorderColor, Color.TRANSPARENT)
        isGray = ta.getBoolean(R.styleable.CircleImageView_circleImageGrayscale, false)
        ta.recycle()

        // 开启硬件加速层
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateClipPath()
        updateImageMatrix()
    }

    private fun updateClipPath() {
        val radius = (width.coerceAtMost(height) - borderWidth * 2) / 2f
        val centerX = width / 2f
        val centerY = height / 2f
        clipPath.reset()
        clipPath.addCircle(centerX, centerY, radius, Path.Direction.CW)
    }

    private fun updateImageMatrix() {
        if (drawable == null) return

        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight
        val viewWidth = width - 2 * borderWidth
        val viewHeight = height - 2 * borderWidth

        val scale: Float
        val dx: Float
        val dy: Float

        when (scaleType) {
            ScaleType.CENTER_CROP -> {
                val widthRatio = viewWidth / drawableWidth.toFloat()
                val heightRatio = viewHeight / drawableHeight.toFloat()
                scale = maxOf(widthRatio, heightRatio)
                dx = (viewWidth - drawableWidth * scale) * 0.5f
                dy = (viewHeight - drawableHeight * scale) * 0.5f
            }

            else -> {
                val widthRatio = viewWidth / drawableWidth.toFloat()
                val heightRatio = viewHeight / drawableHeight.toFloat()
                scale = minOf(widthRatio, heightRatio)
                dx = (viewWidth - drawableWidth * scale) * 0.5f
                dy = (viewHeight - drawableHeight * scale) * 0.5f
            }
        }

        drawMatrix.setScale(scale, scale)
        drawMatrix.postTranslate(dx + borderWidth, dy + borderWidth)
    }

    override fun onDraw(canvas: Canvas) {
        if (drawable == null) {
            super.onDraw(canvas)
            return
        }

        // 应用灰度效果
        if (isGray) {
            colorMatrix.setSaturation(0f)
            imagePaint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        } else {
            imagePaint.colorFilter = null
        }

        // 绘制圆形图片
        canvas.save()
        canvas.clipPath(clipPath)
        canvas.concat(drawMatrix)
        drawable.draw(canvas)
        canvas.restore()

        // 绘制边框
        if (borderWidth > 0) {
            borderPaint.style = Paint.Style.STROKE
            borderPaint.color = borderColor
            borderPaint.strokeWidth = borderWidth
            canvas.drawPath(clipPath, borderPaint)
        }
    }

    // 属性设置方法
    fun setBorderWidth(width: Float) {
        borderWidth = width
        updateClipPath()
        invalidate()
    }

    fun setBorderColor(color: Int) {
        borderColor = color
        invalidate()
    }

    fun setGrayscale(grayscale: Boolean) {
        isGray = grayscale
        invalidate()
    }
}