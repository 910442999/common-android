package com.yuanquan.common.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuanquan.common.R;
import com.yuanquan.common.interfaces.ICallBack;
import com.yuanquan.common.utils.KeyBoardUtils;

/**
 * Created by Carson_Ho on 17/8/10.
 */

public class SearchEditText extends LinearLayout {

    /**
     * 初始化成员变量
     */
    private Context context;

    // 搜索框组件
    private EditText et_search; // 搜索按键
    private TextView tv_search;
    // 数据库变量
    // 用于存放历史搜索记录
    // 回调接口
    private ICallBack mCallBack;// 搜索按键回调接口

    /**
     * 构造函数
     * 作用：对搜索框进行初始化
     */
    public SearchEditText(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    /**
     * 关注b
     * 作用：初始化搜索框
     */
    private void init() {

        // 1. 初始化UI组件
        initView();

        /**
         * 监听输入键盘更换后的搜索按键
         * 调用时刻：点击键盘上的搜索键时
         */
        et_search.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (!(mCallBack == null)) {
                        mCallBack.searchAction(et_search.getText().toString());
                    }
                }
                return false;
            }
        });


        /**
         * 搜索框的文本变化实时监听
         */
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            // 输入文本后调用该方法
            @Override
            public void afterTextChanged(Editable s) {
                // 每次输入后，模糊查询数据库 & 显示
                // 注：若搜索框为空,则模糊搜索空字符 = 显示所有的搜索历史
                if (mCallBack != null) {
                    mCallBack.afterTextChanged(et_search.getText().toString());
                }
            }
        });
        et_search.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                KeyBoardUtils.closeKeybord(v, context);
            }
        });
        tv_search.setOnClickListener(view -> {
            if (!(mCallBack == null)) {
                mCallBack.rightAction(et_search.getText().toString());
            }
        });
    }


    /**
     * 关注c：绑定搜索框xml视图
     */
    private void initView() {

        // 1. 绑定R.layout.search_layout作为搜索框的xml文件
        LayoutInflater.from(context).inflate(R.layout.search_layout, this);

        // 2. 绑定搜索框EditText
        et_search = (EditText) findViewById(R.id.et_search);
        tv_search = (TextView) findViewById(R.id.tv_search);
    }

    public void clean() {
        et_search.clearFocus();
        et_search.setText("");
    }

    public void clearFocus() {
        et_search.clearFocus();
    }

    public void setSearchTextHint(String text) {
        et_search.setHint(text);
    }

    public void setSearchTextSize(float size) {
        et_search.setTextSize(size);
    }

    public void setSearchTextColor(int color) {
        et_search.setTextColor(color);
    }

    public void setSearchButtonVisibility(int visibility) {
        tv_search.setVisibility(visibility);
    }

    public void setSearchText(String text) {
        tv_search.setText(text);
    }

    /**
     * 点击键盘中搜索键后的操作，用于接口回调
     */
    public void setOnSearchClick(ICallBack mCallBack) {
        this.mCallBack = mCallBack;
    }
}
