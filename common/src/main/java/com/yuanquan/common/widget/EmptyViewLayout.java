package com.yuanquan.common.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.yuanquan.common.R;

public class EmptyViewLayout extends LinearLayout {
    private Context context;
    private String data;
    int resId;

    public EmptyViewLayout(Context context, String data) {
        super(context);
        this.context = context;
        this.data = data;
        initView();
    }

    public EmptyViewLayout(Context context, @DrawableRes int resId, String data) {
        super(context);
        this.context = context;
        this.resId = resId;
        this.data = data;
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(context).inflate(R.layout.include_layout_empty, this);
        if (resId > 0) {
            ImageView iv_empty_image = view.findViewById(R.id.iv_empty_image);
            iv_empty_image.setImageResource(resId);
        }
        TextView tv_empty_txt = view.findViewById(R.id.tv_empty_txt);
        tv_empty_txt.setText(data);
    }
}
