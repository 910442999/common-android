package com.yuanquan.common

import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.ActionMode
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnScrollChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import com.yuanquan.common.utils.LogUtil
import com.yuanquan.common.utils.SysUtils
import com.yuanquan.common.utils.onClick
import com.yuanquan.common.widget.popup.CommonPopupWindow


class TextLongDownActivity3 : Activity() {
    var mScrollX = 100
    var mScrollY = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_long_down2)
        var tv_text2 = findViewById<TextView>(R.id.tv_text2)
        var scroll_view = findViewById<NestedScrollView>(R.id.scroll_view)
        val mPopupView: View =
            LayoutInflater.from(this).inflate(R.layout.layout_operate_windows, null)
        val popupWindow = CommonPopupWindow.Builder(this)
            .setView(mPopupView) //                .setWidthAndHeight(186, 144)
            .setOutsideTouchable(false).create()
        tv_text2.text =
            "阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放"
//        tv_text2.movementMethod = ScrollingMovementMethod.getInstance()
//        tv_text2.movementMethod = null

        var tv_huaxian = mPopupView.findViewById<TextView>(R.id.tv_huaxian)
        var tv_select = mPopupView.findViewById<TextView>(R.id.tv_select)
        var tv_click = mPopupView.findViewById<TextView>(R.id.tv_click)
        var tv_add_text = mPopupView.findViewById<TextView>(R.id.tv_add_text)
        tv_huaxian.onClick {
            val selectionStart: Int = tv_text2.getSelectionStart()
            val selectionEnd: Int = tv_text2.getSelectionEnd()
            var spannable: SpannableString = SpannableString(tv_text2.text)
            val underlineSpan = UnderlineSpan()
            spannable.setSpan(
                underlineSpan,
                selectionStart,
                selectionEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv_text2.setText(spannable)
            scroll_view.scrollTo(mScrollX, 1000)
        }
        tv_select.onClick {
            val selectionStart: Int = tv_text2.getSelectionStart()
            val selectionEnd: Int = tv_text2.getSelectionEnd()
            val backgroundSpan = BackgroundColorSpan(Color.YELLOW)
            var spannable: SpannableString = SpannableString(tv_text2.text)
            spannable.setSpan(
                backgroundSpan,
                selectionStart,
                selectionEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv_text2.setText(spannable)
            scroll_view.scrollTo(mScrollX, 1000)
        }
        tv_click.onClick {
            val selectionStart: Int = tv_text2.getSelectionStart()
            val selectionEnd: Int = tv_text2.getSelectionEnd()
            var spannable: SpannableString = SpannableString(tv_text2.text)

            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    // 处理点击事件
                    Toast.makeText(applicationContext, "点击了选中的文本", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            spannable.setSpan(
                clickableSpan,
                selectionStart,
                selectionEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv_text2.setText(spannable)
            tv_text2.setMovementMethod(LinkMovementMethod.getInstance())
            scroll_view.scrollTo(mScrollX, 1000)

        }
        tv_add_text.onClick {
            val appendedText = "  world!  "
            tv_text2.append(appendedText)
            Toast.makeText(applicationContext, "追加了文本", Toast.LENGTH_SHORT)
                .show()
            scroll_view.scrollTo(mScrollX, 1000)
        }
        tv_text2.setTextIsSelectable(true)
//        val handler = Handler()
//
//        val runnable = object : Runnable {
//            override fun run() {
//                val newText = "New text"
//                tv_text2.append(newText)
//
//                // 5秒后再次执行
//                handler.postDelayed(this, 1000)
//            }
//        }
//
//// 初始延迟5秒启动
//        handler.postDelayed(runnable, 1000)
//        tv_text2.setOnScrollChangeListener(object : OnScrollChangeListener {
//            override fun onScrollChange(
//                v: View?,
//                scrollX: Int,
//                scrollY: Int,
//                oldScrollX: Int,
//                oldScrollY: Int
//            ) {
//
//                LogUtil.e("文本滚动的坐标   scroll X: $scrollX, scroll Y: $scrollY  oldScroll X: $oldScrollX, oldScroll Y: $oldScrollY")
////                tv_text2.clearFocus();
//            }
//
//        })
        tv_text2.customSelectionActionModeCallback = object : ActionMode.Callback2() {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                LogUtil.e("111111111111111111111111111111111111111")

                val selStart: Int = tv_text2?.selectionStart ?: 0
                val selEnd: Int = tv_text2?.selectionEnd ?: 0

                LogUtil.e("selStart " + selStart)
                LogUtil.e("selEnd " + selEnd)
                val min = 0.coerceAtLeast(selStart.coerceAtMost(selEnd))
                val max = 0.coerceAtLeast(selStart.coerceAtLeast(selEnd))

                LogUtil.e("min " + min)
                LogUtil.e("max " + max)
                val content = tv_text2?.text?.subSequence(min, max)

                LogUtil.e("content " + content)

                val layout: Layout = tv_text2.layout
                if (layout != null) {
                    val x: Float = layout.getPrimaryHorizontal(selStart)
                    val y: Int = layout.getLineBaseline(layout.getLineForOffset(selStart))
                    // 输出光标的坐标
                    LogUtil.e("文本的相对坐标（y轴跟随滚动变化） x: $x, y: $y")

                    val startLine = layout.getLineForOffset(selStart)
                    val endLine = layout.getLineForOffset(selEnd)

                    val selectionStartY = layout.getLineTop(startLine)
                    val selectionEndY = layout.getLineBottom(endLine)

                    val location = IntArray(2)
                    tv_text2.getLocationOnScreen(location)
                    val editTextY = location[1]

                    val selectionStartScreenY = editTextY + selectionStartY
                    val selectionEndScreenY = editTextY + selectionEndY

                    // 输出选中文本的位置在屏幕高度中的坐标

                    LogUtil.e("选中文本的位置在屏幕高度中的坐标   Start Y: $selectionStartScreenY, End Y: $selectionEndScreenY")
                    var screenWidth = SysUtils.getScreenWidth(this@TextLongDownActivity3)
                    popupWindow.showAtLocation(
                        tv_text2,
                        Gravity.NO_GRAVITY,
                        screenWidth - mPopupView.width,
                        selectionEndScreenY
                    ) // view中心位置

                }

                return true //返回false则不会显示弹窗
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                LogUtil.e("2222222222222222222222222222222222222   " + mode?.type.toString())
                // 移除所有菜单项
                menu?.clear()
                // 返回 true 以指示操作模式已准备好
                return true;
            }

            override fun onActionItemClicked(
                actionMode: ActionMode,
                menuItem: MenuItem
            ): Boolean {
                LogUtil.e("3333333333333333333333333333333333333333")
                return false //返回true则系统的"复制"、"搜索"之类的item将无效，只有自定义item有响应
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                LogUtil.e("444444444444444444444444444444444444444444")
                popupWindow.dismiss()
            }

            override fun onGetContentRect(mode: ActionMode?, view: View?, outRect: Rect?) {
                super.onGetContentRect(mode, view, outRect)
                LogUtil.e("555555555555555555555555555555" + outRect.toString())
            }
        }
    }
}