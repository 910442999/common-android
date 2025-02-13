package com.yuanquan.common.widget.color_picker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yuanquan.common.R


/**
 * 颜色选择预览
 */
class ColorSelectView : LinearLayout {

    private lateinit var colorView: RainbowColorView //彩虹条View
    private lateinit var colorBar: View //彩虹条index

    private lateinit var alphaView: RainbowColorView //透明度View
    private lateinit var alphaBar: View //透明度index

    private lateinit var boardView: RainbowColorView //明暗度View
    private lateinit var colorBoardBar: View //明暗度index

    private var previewBar: RainbowColorView? = null

    private lateinit var colorSelectBarLP: MarginLayoutParams
    private lateinit var alphaSelectBarLP: MarginLayoutParams
    private lateinit var colorBoardLP: MarginLayoutParams

    /*当前彩虹条 rgb*/
    private var colorRed = 255
    private var colorGreen: Int = 0
    private var colorBlue: Int = 0

    /*选择的rgb*/
    private var indexRed = 255
    private var indexGreen: Int = 0
    private var indexBlue: Int = 0

    /*透明度*/
    private var indexAlpha = 255

    private var index = 0

    private var colorsRecyclerView: RecyclerView? = null
    private var colorAdapter: ColorSelectAdapter? = null
    var spanCount: Int = 8
    private var toolsColors: List<String> = arrayListOf<String>(
        "#333433",
        "#D8D7D1",
        "#7E807F",
        "#F5DF00",
        "#695026",
        "#E6DBC8",
        "#000000",
        "#E8A743",
        "#5A8EDC",
        "#B6472D",
        "#7A8AC4",
        "#B5D260",
        "#C0627B",
        "#9F8468",
        "#728C7A",
        "#FFFFFF"
    )

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.view_color_picker, this)/*明暗度*/
        boardView = view.findViewById(R.id.color_board)
        colorBoardBar = view.findViewById(R.id.view_select_color_location)
        colorBoardLP = colorBoardBar.layoutParams as MarginLayoutParams

        /*预览*/
        previewBar = view.findViewById(R.id.preview_color)
        previewBar?.setOnClickListener {
            var color = previewBar?.getPreviewColor()
            if (color != null) {
                colorClicked?.invoke(color)
            }
        }
        /*彩虹条*/
        colorView = findViewById(R.id.view_color)
        colorBar = view.findViewById(R.id.view_select_color_bar)
        colorSelectBarLP = colorBar.layoutParams as MarginLayoutParams

        /*透明度*/
        alphaView = view.findViewById(R.id.view_alpha)
        alphaBar = view.findViewById(R.id.view_select_alpha_bar)
        alphaSelectBarLP = alphaBar.layoutParams as MarginLayoutParams

        colorsRecyclerView = view.findViewById(R.id.colors_recycler_view)
        colorAdapter = ColorSelectAdapter(toolsColors)
        colorsRecyclerView?.adapter = colorAdapter
        colorsRecyclerView?.setLayoutManager(GridLayoutManager(context, spanCount))
        colorAdapter?.setOnColorClickListener { color: String ->
            colorClicked?.invoke(color)
        }
        layoutTouch()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun layoutTouch() {/*调整彩虹条颜色*/
        colorView.setOnTouchListener { _, event ->
            val width = colorView.width
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP, MotionEvent.ACTION_MOVE -> {}
            }
            val leftMargin = event.x
            var x = 0f
            if (leftMargin < colorBar.width / 2.0f) {
                colorSelectBarLP.leftMargin = 0
            } else if (leftMargin > width - colorBar.width / 2.0f) {
                x = 100f
                colorSelectBarLP.leftMargin = width - colorBar.width
            } else {
                x = event.x / width * 100
                colorSelectBarLP.leftMargin = (leftMargin - colorBar.width / 2).toInt()
            }
            colorBar.layoutParams = colorSelectBarLP
            changedColorBar(x.toInt())
            true
        }

        /*调整透明度*/
        alphaView.setOnTouchListener { _, event ->
            val width = alphaView.width
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP, MotionEvent.ACTION_MOVE -> {}
            }
            val leftMargin = event.x
            var x = 0f
            if (leftMargin < alphaBar.width / 2.0f) {
                alphaSelectBarLP.leftMargin = 0
            } else if (leftMargin > width - alphaBar.width / 2.0f) {
                x = 100f
                alphaSelectBarLP.leftMargin = width - alphaBar.width
            } else {
                x = event.x / width * 100
                alphaSelectBarLP.leftMargin = (leftMargin - alphaBar.width / 2).toInt()
            }
            alphaBar.layoutParams = alphaSelectBarLP
            changeAlpha(x.toInt())
            true
        }

        /*调整颜色明暗*/
        boardView.setOnTouchListener { _, event ->
            val width = boardView.width
            val height = boardView.height
            val leftMargin: Int
            val topMargin: Int
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {}
                MotionEvent.ACTION_MOVE -> {
                    //防止越界处理
                    leftMargin = if (event.x > width - colorBoardBar.width / 2f) {
                        width - colorBoardBar.width
                    } else if (event.x < colorBoardBar.width / 2f) {
                        0
                    } else {
                        (event.x - colorBoardBar.width / 2f).toInt()
                    }
                    topMargin = if (event.y > height - colorBoardBar.height / 2f) {
                        height - colorBoardBar.height
                    } else if (event.y <= colorBoardBar.height / 2f) {
                        0
                    } else {
                        (event.y - colorBoardBar.height / 2f).toInt()
                    }
                    colorBoardLP.leftMargin = leftMargin
                    colorBoardLP.topMargin = topMargin
                    colorBoardBar.layoutParams = colorBoardLP
                    changeColorBoard()
                }
            }
            true
        }
    }

    /**
     * 颜色值调整
     */
    private fun changedColorBar(progressColor: Int) {
        colorRed = 0
        colorGreen = 0
        colorBlue = 0
        index = (progressColor / (100 / 6f)).toInt()
        val v = progressColor % (100 / 6f) / (100 / 6f)
        when (index) {
            0 -> {
                colorRed = 255
                colorGreen = (255 * v).toInt()
            }

            1 -> {
                colorRed = (255 * (1 - v)).toInt()
                colorGreen = 255
            }

            2 -> {
                colorGreen = 255
                colorBlue = (255 * v).toInt()
            }

            3 -> {
                colorGreen = (255 * (1 - v)).toInt()
                colorBlue = 255
            }

            4 -> {
                colorBlue = 255
                colorRed = (255 * v).toInt()
            }

            5 -> {
                colorBlue = (255 * (1 - v)).toInt()
                colorRed = 255
            }

            else -> colorRed = 255
        }
        boardView.setPaintColor(Color.rgb(colorRed, colorGreen, colorBlue))
        changeColorBoard()
    }

    /**
     * 颜色明暗度调整
     */
    private fun changeColorBoard() {
        var tempRed = colorRed
        var tempGreen = colorGreen
        var tempBlue = colorBlue
        val xPercent = 1 - colorBoardBar.x / (boardView.width - colorBoardBar.width)
        val yPercent = colorBoardBar.y / (boardView.height - colorBoardBar.height)
        when (index) {
            0, 5, 6 -> {
                tempGreen = (colorGreen + xPercent * (255 - colorGreen)).toInt()
                tempBlue = (colorBlue + xPercent * (255 - colorBlue)).toInt()
            }

            1, 2 -> {
                tempRed = (colorRed + xPercent * (255 - colorRed)).toInt()
                tempBlue = (colorBlue + xPercent * (255 - colorBlue)).toInt()
            }

            3, 4 -> {
                tempRed = (colorRed + xPercent * (255 - colorRed)).toInt()
                tempGreen = (colorGreen + xPercent * (255 - colorGreen)).toInt()
            }
        }
        indexRed = (tempRed - tempRed * yPercent).toInt()
        indexGreen = (tempGreen - tempGreen * yPercent).toInt()
        indexBlue = (tempBlue - tempBlue * yPercent).toInt()

        val color = Color.argb(indexAlpha, indexRed, indexGreen, indexBlue)
        previewBar?.setPreviewColor(color)
        colorChanged?.invoke(color)
        setAlphaRgb(color)
    }

    /**
     * 透明度
     */
    private fun changeAlpha(progress: Int) {
        indexAlpha = (progress / 100f * 255).toInt()
        val color = Color.argb(indexAlpha, indexRed, indexGreen, indexBlue)
        previewBar?.setPreviewColor(color)
        colorChanged?.invoke(color)
    }

    /**
     * 设置透明度Bar色值
     */
    private fun setAlphaRgb(@ColorInt rgbColor: Int? = null) {
        if (rgbColor == null) {
            alphaView.setAlphaColor(Color.rgb(255, 0, 0))
            return
        }
        alphaView.setAlphaColor(rgbColor)
    }

    /**
     * 设置透明度是否显示
     */
    fun setAlphaVisibility(visibility: Int) {
        alphaView.visibility = visibility
    }

    /**
     * 设置颜色spanCount
     */
    fun setColorSpanCount(spanCount: Int) {
        this.spanCount = spanCount
        colorsRecyclerView?.setLayoutManager(GridLayoutManager(context, spanCount))
    }

    fun setBoardViewPaintColor(@ColorInt paintColor: Int) {
        boardView.setPaintColor(paintColor)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)

        val layoutParams = colorBoardBar.layoutParams as FrameLayout.LayoutParams
        layoutParams.leftMargin = initBarPosition(BarIndex.End, boardView, colorBoardBar)
        colorBoardBar.layoutParams = layoutParams

        colorSelectBarLP.leftMargin = initBarPosition(BarIndex.Start, colorView, colorBar)
        colorBar.layoutParams = colorSelectBarLP

        alphaSelectBarLP.leftMargin = initBarPosition(BarIndex.End, alphaView, alphaBar)
        alphaBar.layoutParams = alphaSelectBarLP

        setAlphaRgb()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }

    private fun initBarPosition(barIndex: BarIndex, parentView: View, barView: View): Int =
        when (barIndex) {
            BarIndex.Start -> 0
            BarIndex.Centre -> (parentView.width / 2) - (barView.width / 2)
            BarIndex.End -> parentView.width - barView.width
        }

    private enum class BarIndex {
        Start, Centre, End
    }

    var colorChanged: ((Int) -> Unit)? = null
    var colorClicked: ((String) -> Unit)? = null
}