package com.yuanquan.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yuanquan.common.R;

/**
 * 自定义加载提示框
 */

public class LoadingDialog extends Dialog {
    private TextView tvContent;

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.MyDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        tvContent = findViewById(R.id.tv_content);
    }

    @Override
    public boolean isShowing() {
        return super.isShowing();
    }

    @Override
    public void show() {
        super.show();
    }

    public void show(String content) {
        show();
        if (!TextUtils.isEmpty(content)) {
            tvContent.setText(content);
        } else {
            tvContent.setText("");
        }
    }

    public void setLoadContent(String content) {
        if (!TextUtils.isEmpty(content)) {
            tvContent.setText(content);
        } else {
            tvContent.setText("");
        }
    }

}
