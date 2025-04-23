package com.yuanquan.common.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.yuanquan.common.R

/**
 * // 前置标签
 * tagTextView.setPrefixTags("这是一条消息",
 *     Arrays.asList("[紧急]", "【通知】"),
 *     Arrays.asList(0, 1));
 *
 * // 后置标签
 * tagTextView.setSuffixTags("最新动态",
 *     Arrays.asList("[2023]", "★"),
 *     Arrays.asList(0, 2));
 *
 * // 点击监听
 * tagTextView.setOnTagClickListener(tag -> {
 *     Toast.makeText(context, "点击标签: " + tag, Toast.LENGTH_SHORT).show();
 * });
 *
 * 1. 全局设置所有标签颜色
 * tagTextView.setTagTextColor(Color.RED)
 * tagTextView.setPrefixTags("重要通知", listOf("[紧急]", "【最新】"), listOf(0, 1))
 *2. 按类型设置不同颜色
 * val colorMap = mapOf(
 *     0 to Color.BLUE,
 *     1 to Color.GREEN
 * )
 * tagTextView.setTagTextColors(colorMap)
 * tagTextView.setSuffixTags("系统消息", listOf("[未读]", "!!"), listOf(0, 1))
 *
 * 3. 临时设置单次颜色
 *
 * tagTextView.setSuffixTagsWithColor(
 *     content = "版本更新",
 *     tags = listOf("v2.3.0", "推荐"),
 *     types = listOf(0, 1),
 *     textColor = Color.parseColor("#FF4081")
 * )
 */
class TagTextView : AppCompatTextView {
    interface OnTagClickListener {
        fun onTagClick(tag: String?)
    }

    private var mContext: Context? = null
    private var mTagClickListener: OnTagClickListener? = null
    private var isPrefixMode = false // 标记当前模式

    // 新增颜色配置参数
    private var tagTextColor: Int? = null
    private var tagTextColorsByType: Map<Int, Int> = emptyMap()

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        mContext = context
        movementMethod = LinkMovementMethod.getInstance()
    }

    // 新增颜色设置方法
    fun setTagTextColor(color: Int) {
        this.tagTextColor = color
    }

    fun setTagTextColors(colorMap: Map<Int, Int>) {
        this.tagTextColorsByType = colorMap
    }

    fun setContentAndTag(
        content: String,
        tags: List<String>,
        types: List<Int>,
        start: Boolean = true
    ) {
        if (start) {
            this.setPrefixTags(content, tags, types)
        } else {
            this.setSuffixTags(content, tags, types)
        }
    }

    // region 前置标签方法
    fun setPrefixTags(content: String, tags: List<String>, types: List<Int>) {
        isPrefixMode = true
        val spannable = buildSpannableWithPrefix(content, tags, types)
        text = spannable
    }

    fun setPrefixTagsWithColor(
        content: String,
        tags: List<String>,
        types: List<Int>,
        textColor: Int? = null
    ) {
        this.tagTextColor = textColor
        setPrefixTags(content, tags, types)
    }

    fun setSuffixTagsWithColor(
        content: String,
        tags: List<String>,
        types: List<Int>,
        textColor: Int? = null
    ) {
        this.tagTextColor = textColor
        setSuffixTags(content, tags, types)
    }

    private fun buildSpannableWithPrefix(
        content: String,
        tags: List<String>,
        types: List<Int>
    ): SpannableString {
        val buffer = StringBuffer()
        for (tag in tags) {
            buffer.append(tag)
        }
        buffer.append(content)

        val spannable = SpannableString(buffer)
        for (i in tags.indices) {
            addTagSpan(
                spannable, tags[i], types[i],
                0, getPrefixTagStartIndex(tags, i)
            )
        }
        return spannable
    }

    private fun getPrefixTagStartIndex(tags: List<String>, index: Int): Int {
        var start = 0
        for (i in 0 until index) {
            start += tags[i].length
        }
        return start
    }

    // endregion
    // region 后置标签方法
    fun setSuffixTags(content: String, tags: List<String>, types: List<Int>) {
        isPrefixMode = false
        val spannable = buildSpannableWithSuffix(content, tags, types)
        text = spannable
    }

    private fun buildSpannableWithSuffix(
        content: String,
        tags: List<String>,
        types: List<Int>
    ): SpannableString {
        val buffer = StringBuffer(content)
        for (tag in tags) {
            buffer.append(tag)
        }

        val spannable = SpannableString(buffer)
        val contentLen = content.length
        for (i in tags.indices) {
            val start = contentLen + getSuffixTagStartIndex(tags, i)
            addTagSpan(
                spannable, tags[i], types[i],
                contentLen, start
            )
        }
        return spannable
    }

    private fun getSuffixTagStartIndex(tags: List<String>, index: Int): Int {
        var start = 0
        for (i in 0 until index) {
            start += tags[i].length
        }
        return start
    }

    // endregion
    // region 公共逻辑
    private fun addTagSpan(
        spannable: SpannableString, tag: String, type: Int,
        baseIndex: Int, start: Int
    ) {
        val tagView = createTagView(tag, type)
        val bitmap = convertViewToBitmap(tagView)
        val drawable: Drawable = BitmapDrawable(resources, bitmap)
        drawable.setBounds(0, 0, tagView.width, tagView.height)

        val span = ClickableImageSpan(drawable, tag, mTagClickListener)
        val end = start + tag.length
        spannable.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun createTagView(tagText: String, type: Int): View {
        val layoutRes = when (type) {
            1 -> R.layout.layout_texttab1 // 自定义类型1的布局
            else -> R.layout.layout_texttab // 默认布局
        }

        val view = LayoutInflater.from(mContext).inflate(layoutRes, null)
        val tvTag = view.findViewById<TextView>(R.id.tabText)
        tvTag.text = tagText

        // 应用颜色优先级：类型颜色 > 全局颜色 > 布局默认颜色
        tagTextColorsByType[type]?.let {
            tvTag.setTextColor(it)
        } ?: run {
            tagTextColor?.let { tvTag.setTextColor(it) }
        }

        measureView(view)
        return view
    }

    private fun measureView(view: View) {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        // 添加最小宽度保护
        val measuredWidth = view.measuredWidth.coerceAtLeast(1)
        val measuredHeight = view.measuredHeight.coerceAtLeast(1)
        view.layout(0, 0, measuredWidth, measuredHeight)
    }

    // endregion
    // region 点击处理
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            handleTagClick(event)
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun handleTagClick(event: MotionEvent) {
        if (mTagClickListener == null) return

        val x = event.x.toInt()
        val y = event.y.toInt()
        val layout = layout ?: return

        val line = layout.getLineForVertical(y)
        val offset = layout.getOffsetForHorizontal(line, x.toFloat())

        val spans = spannable.getSpans(
            offset, offset,
            ClickableImageSpan::class.java
        )
        if (spans.size > 0) {
            spans[0].onClick()
        }
    }

    private val spannable: Spannable
        get() = text as Spannable

    private class ClickableImageSpan(
        d: Drawable,
        private val mTag: String,
        private val mListener: OnTagClickListener?
    ) :
        ImageSpan(d) {
        fun onClick() {
            mListener?.onTagClick(mTag)
        }
    }

    fun setOnTagClickListener(listener: OnTagClickListener?) {
        mTagClickListener = listener
    } // endregion

    companion object {
        private fun convertViewToBitmap(view: View): Bitmap {
            view.buildDrawingCache()
            return Bitmap.createBitmap(view.drawingCache)
        }
    }
}