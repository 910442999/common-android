package com.yuanquan.common.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatEditText;

import com.yuanquan.common.R;
import com.yuanquan.common.interfaces.OnCleanClickListener;
import com.yuanquan.common.utils.KeyBoardUtils;

/**
 * Created by Carson_Ho on 17/8/10.
 */

public class EditText_Clear extends AppCompatEditText {


    /**
     * 步骤1：定义左侧搜索图标 & 一键删除图标
     */
    private Drawable clearDrawable;
    private boolean visible;

    public EditText_Clear(Context context) {
        super(context);
        init();
        // 初始化该组件时，对EditText_Clear进行初始化 ->>步骤2
    }

    public EditText_Clear(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditText_Clear(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 步骤2：初始化 图标资源
     */
    private void init() {
        clearDrawable = getResources().getDrawable(R.mipmap.icon_close);
        clearDrawable.setBounds(0, 0, 40, 40);
    }


    /**
     * 步骤3：通过监听复写EditText本身的方法来确定是否显示删除图标
     * 监听方法：onTextChanged（） & onFocusChanged（）
     * 调用时刻：当输入框内容变化时 & 焦点发生变化时
     */

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        setClearIconVisible(hasFocus() && text.length() > 0);
        // hasFocus()返回是否获得EditTEXT的焦点，即是否选中
        // setClearIconVisible（） = 根据传入的是否选中 & 是否有输入来判断是否显示删除图标->>关注1
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        setClearIconVisible(focused && length() > 0);
        if (!focused) {
            KeyBoardUtils.closeKeyboard(this, getContext());
        }
        // focused = 是否获得焦点
        // 同样根据setClearIconVisible（）判断是否要显示删除图标
    }

    /**
     * 关注1
     * 作用：判断是否显示删除图标
     */
    private void setClearIconVisible(boolean visible) {
        this.visible = visible;
        setCompoundDrawables(null, null, visible ? clearDrawable : null, null);
    }

    /**
     * 步骤4：对删除图标区域设置点击事件，即"点击 = 清空搜索框内容"
     * 原理：当手指抬起的位置在删除图标的区域，即视为点击了删除图标 = 清空搜索框内容
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            // 原理：当手指抬起的位置在删除图标的区域，即视为点击了删除图标 = 清空搜索框内容
            case MotionEvent.ACTION_UP:
                Drawable drawable = clearDrawable;
                if (visible && drawable != null && event.getX() <= (getWidth() - getPaddingRight())
                        && event.getX() >= (getWidth() - getPaddingRight() - drawable.getBounds().width())) {
                    setText("");
                    if (onCleanClickListener != null) onCleanClickListener.onClean();
                }
                // 判断条件说明
                // event.getX() ：抬起时的位置坐标
                // getWidth()：控件的宽度
                // getPaddingRight():删除图标图标右边缘至EditText控件右边缘的距离
                // 即：getWidth() - getPaddingRight() = 删除图标的右边缘坐标 = X1
                // getWidth() - getPaddingRight() - drawable.getBounds().width() = 删除图标左边缘的坐标 = X2
                // 所以X1与X2之间的区域 = 删除图标的区域
                // 当手指抬起的位置在删除图标的区域（X2=<event.getX() <=X1），即视为点击了删除图标 = 清空搜索框内容
                break;
        }
        return super.onTouchEvent(event);
    }

    private OnCleanClickListener onCleanClickListener;

    public void setOnCleanClickListener(OnCleanClickListener listener) {
        this.onCleanClickListener = listener;
    }
}

