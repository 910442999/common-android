package com.yuanquan.common.selecttext

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.yuanquan.common.R
import com.yuanquan.common.widget.MySelectTextHelper

class MainActivity2 : AppCompatActivity() {
    var mSelectableTextHelper: MySelectTextHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        var tv_text2 = findViewById<TextView>(R.id.tv_text2)

        var spannable: SpannableStringBuilder =
            SpannableStringBuilder("阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放阿斯蒂芬打发打发打发都是阿斯蒂芬阿斯蒂芬爱的色放")

        tv_text2.text = spannable

        val handler = Handler()

        val runnable = object : Runnable {
            override fun run() {
                val newText = "New text"
                tv_text2.append(newText)

                // 5秒后再次执行
                handler.postDelayed(this, 1000)
            }
        }

// 初始延迟5秒启动
        handler.postDelayed(runnable, 1000)

        mSelectableTextHelper = MySelectTextHelper.Builder(tv_text2) // 放你的textView到这里！！
            .setCursorHandleColor(ContextCompat.getColor(this, R.color.colorAccent)) // 游标颜色
            .setCursorHandleSizeInDp(22f) // 游标大小 单位dp
            .setSelectedColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorAccentTransparent
                )
            ) // 选中文本的颜色
            .setSelectAll(false) // 初次选中是否全选 default true
            .setScrollShow(true) // 滚动时是否继续显示 default true
            .setSelectedAllNoPop(true) // 已经全选无弹窗，设置了监听会回调 onSelectAllShowCustomPop 方法
            .setMagnifierShow(true) // 放大镜 default true
            .setSelectTextLength(2)// 首次选中文本的长度 default 2
            .setPopDelay(100)// 弹窗延迟时间 default 100毫秒
//                .setPopAnimationStyle(R.style.Base_Animation_AppCompat_Dialog)// 弹窗动画 default 无动画
            .addItem(
                R.drawable.ic_msg_copy,
                R.string.copy,
                object : MySelectTextHelper.Builder.onSeparateItemClickListener {
                    override fun onClick() {
//                        copy(mSelectableTextHelper, selectedText)
                    }
                }).addItem(
                R.drawable.ic_msg_select_all,
                R.string.select_all,
                object : MySelectTextHelper.Builder.onSeparateItemClickListener {
                    override fun onClick() {
//                        selectAll()
                        SelectTextEventBus.instance.dispatch(SelectTextEvent("dismissAllPop"))
                        mSelectableTextHelper?.updateSelectText()
                    }
                }).addItem(
                R.drawable.ic_msg_forward,
                R.string.forward,
                object : MySelectTextHelper.Builder.onSeparateItemClickListener {
                    override fun onClick() {
//                        forward()

                        val appendedText = "  world!  "
                        tv_text2.append(appendedText)
                        Toast.makeText(applicationContext, "追加了文本", Toast.LENGTH_SHORT)
                            .show()

                        SelectTextEventBus.instance.dispatch(SelectTextEvent("dismissAllPop"))
                        mSelectableTextHelper?.reset()
                    }
                }).setPopSpanCount(5) // 设置操作弹窗每行个数 default 5
            .setPopStyle(
                R.drawable.shape_color_4c4c4c_radius_8 /*操作弹窗背*/,
                R.mipmap.ic_arrow /*箭头图片*/
            ) // 设置操作弹窗背景色、箭头图片
            .build()
        mSelectableTextHelper!!.setSelectListener(object : MySelectTextHelper.OnSelectListener {
            /**
             * 点击回调
             */
            override fun onClick(v: View?, originalContent: CharSequence?) {
                // 拿原始文本方式
//                clickTextView(msgBean.content!!) // 推荐
                // clickTextView(originalContent!!) // 不推荐 富文本可能被修改值 导致gif动不了
            }

            /**
             * 长按回调
             */
            override fun onLongClick(v: View?) {
//                postShowCustomPop(MsgAdapter.SHOW_DELAY)
            }

            /**
             * 选中文本回调
             */
            override fun onTextSelected(content: CharSequence?) {
//                selectedText = content.toString()
            }

            /**
             * 弹窗关闭回调
             */
            override fun onDismiss() {}

            /**
             * 点击TextView里的url回调
             *
             * 已被下面重写
             * textView.setMovementMethod(new LinkMovementMethodInterceptor());
             */
            override fun onClickUrl(url: String?) {
//                toast("点击了：  $url")

//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                mContext.startActivity(intent)
            }

            /**
             * 全选显示自定义弹窗回调
             */
            override fun onSelectAllShowCustomPop() {
//                postShowCustomPop(MsgAdapter.SHOW_DELAY)
            }

            /**
             * 重置回调
             */
            override fun onReset() {
                SelectTextEventBus.instance.dispatch(SelectTextEvent("dismissOperatePop"))
            }

            /**
             * 解除自定义弹窗回调
             */
            override fun onDismissCustomPop() {
                SelectTextEventBus.instance.dispatch(SelectTextEvent("dismissOperatePop"))
            }

            /**
             * 是否正在滚动回调
             */
            override fun onScrolling() {
//                removeShowSelectView()
            }
        })
    }
}