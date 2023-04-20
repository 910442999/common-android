package com.yuanquan.common.utils;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.yuanquan.common.R;


/**
 * Created by jianfei.yang on 2019/5/23.
 */

public class AlertDialogUtils {
    private static Dialog dialog = null;

    public static void show(Context context, String title, View.OnClickListener submitListener) {
        show(context, title, null, "取消", "确定", null, null, submitListener);
    }

    public static void show(Context context, String title, String content, View.OnClickListener submitListener) {
        show(context, title, content, "取消","确定", null, null, submitListener);
    }

    public static void show(Context context, String title, String content, View.OnClickListener cancelListener, View.OnClickListener submitListener) {
        show(context, title, content, "取消", "确定", null, cancelListener, submitListener);
    }

    public static void show(Context context, String title, String content, String cancel, String submit, View.OnClickListener contentlListener, View.OnClickListener cancelListener, View.OnClickListener submitListener) {
        dialog = new AlertDialog.Builder(context, R.style.MyDialog).create();
        dialog.show();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.dialog_alert_custom, null);
        TextView txtTitle = (TextView) view.findViewById(R.id.txt_title);
        TextView txtContent = (TextView) view.findViewById(R.id.txt_content);
        TextView txtCancel = (TextView) view.findViewById(R.id.txt_cancel);
        TextView txtSubmit = (TextView) view.findViewById(R.id.txt_submit);
        txtTitle.setText(title);
        if (TextUtils.isEmpty(cancel)) {
            txtCancel.setVisibility(View.GONE);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(SysUtils.dp2Px(context, 160), SysUtils.dp2Px(context, 30));
            txtSubmit.setLayoutParams(layoutParams);
        } else {
            txtCancel.setText(cancel);
        }
        if (!TextUtils.isEmpty(content)) {
            txtContent.setText(content);
        } else {
            txtContent.setVisibility(View.GONE);
        }

        txtSubmit.setText(submit);
        txtSubmit.setOnClickListener(v -> {
            if (submitListener != null) {
                submitListener.onClick(v);
            } else {
                dismiss();
            }
        });
        txtCancel.setOnClickListener(v -> {
            if (cancelListener != null) {
                cancelListener.onClick(v);
            } else {
                dismiss();
            }
        });
        txtContent.setOnClickListener(v -> {
            if (contentlListener != null) {
                contentlListener.onClick(v);
            }
        });
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setContentView(view, params);
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    public static void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
