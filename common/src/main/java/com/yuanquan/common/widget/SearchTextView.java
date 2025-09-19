package com.yuanquan.common.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuanquan.common.R;
import com.yuanquan.common.interfaces.OnCleanClickListener;
import com.yuanquan.common.utils.KeyBoardUtils;

/**
 * Created by Carson_Ho on 17/8/10.
 */

public class SearchTextView extends LinearLayout {

    /**
     * 初始化成员变量
     */
    private Context context;

    private TextView tv_search;
    //    private ICallBack mCallBack;
    private boolean visible;
    private Drawable clearDrawable;
    private boolean enableClear = true;

    /**
     * 构造函数
     * 作用：对搜索框进行初始化
     */
    public SearchTextView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public SearchTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public SearchTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    /**
     * 关注b
     * 作用：初始化搜索框
     */
    private void init() {
        initView();
        clearDrawable = getResources().getDrawable(R.mipmap.icon_close);
        clearDrawable.setBounds(0, 0, 60, 60);

        /**
         * 搜索框的文本变化实时监听
         */
        tv_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (enableClear) {
                    setClearIconVisible(s.length() > 0);
                }
            }

            // 输入文本后调用该方法
            @Override
            public void afterTextChanged(Editable s) {
                // 每次输入后，模糊查询数据库 & 显示
                // 注：若搜索框为空,则模糊搜索空字符 = 显示所有的搜索历史
//                String tempName = et_search.getText().toString();

            }
        });
        tv_search.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    KeyBoardUtils.closeKeyboard(v, context);
                }
            }
        });

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Drawable drawable = clearDrawable;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (visible && drawable != null && event.getX() <= (getWidth() - getPaddingRight())
                            && event.getX() >= (getWidth() - getPaddingRight() - drawable.getBounds().width())) {
                        tv_search.setText("");
                        if (onCleanClickListener != null) onCleanClickListener.onClean();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void setEnableClear(boolean visible) {
        enableClear = visible;
    }

    /**
     * 关注1
     * 作用：判断是否显示删除图标
     */
    private void setClearIconVisible(boolean visible) {
        this.visible = visible;
        tv_search.setCompoundDrawables(null, null, visible ? clearDrawable : null, null);
    }

    /**
     * 关注c：绑定搜索框xml视图
     */
    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.layout_search_text_view, this);
        tv_search = (TextView) findViewById(R.id.tv_search);
    }

    public void clean() {
        tv_search.setText("");
    }

    public void setSearchTextHint(String text) {
        tv_search.setHint(text);
    }

    public void setSearchText(String text) {
        tv_search.setText(text);
    }

    public void setSearchTextSize(float size) {
        tv_search.setTextSize(size);
    }

    public void setSearchTextColor(int color) {
        tv_search.setTextColor(color);
    }

    private OnCleanClickListener onCleanClickListener;

    public void setOnCleanClickListener(OnCleanClickListener listener) {
        this.onCleanClickListener = listener;
    }
}
