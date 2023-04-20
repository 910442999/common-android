package com.yuanquan.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.yuanquan.common.R;

/**
 * 自定义加载提示框
 */

public class LoadingDialog extends Dialog {

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.MyDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
    }

    @Override
    public boolean isShowing() {
        return super.isShowing();
    }

    @Override
    public void show() {
        super.show();
    }
}
