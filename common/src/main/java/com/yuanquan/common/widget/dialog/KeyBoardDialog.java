package com.yuanquan.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;

import com.yuanquan.common.R;
import com.yuanquan.common.utils.KeyBoardUtils;


/**
 * 自定义键盘弹窗dialog
 */

public class KeyBoardDialog {
    Context context;
    String text;
    private String textHint = "";
    private String textSend = "";

    public KeyBoardDialog(Context context) {
        this.context = context;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTextSend(String textSend) {
        this.textSend = textSend;
    }

    public void setTextHint(String textHint) {
        this.textHint = textHint;
    }

    public void show() {
        View dialoglayout = LayoutInflater.from(context).inflate(R.layout.dialog_input_bottom_alert, null, false);
        final Dialog alertDialog;
//            alertDialog = new Dialog(context, R.style.DialogFullScreenTheme);
        alertDialog = new Dialog(context, R.style.DialogNoFullScreenTheme);

        alertDialog.setContentView(dialoglayout);
        Window dialogWindow = alertDialog.getWindow();
        if (dialogWindow != null) {
            // 一定要设置Background，如果不设置，window属性设置无效,如果通过 window 设置宽高时，想要设置宽为屏宽，就必须调用下面这行代码。
            dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
            dialogWindow.setGravity(Gravity.BOTTOM);
            dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.x = 0;
            lp.y = 0;
            dialogWindow.setAttributes(lp);

        }
        //        AutoSizeCompat.autoConvertDensityOfGlobal((context.getResources()));//如果没有自定义需求用这个方法
        //        AutoSizeCompat.autoConvertDensity(context.getResources(), 667, false);//如果有自定义需求就用这个方法

        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setCancelable(true);
        final AppCompatEditText editSendMsg = dialoglayout.findViewById(R.id.et_name);

        View iv_close = dialoglayout.findViewById(R.id.v_view);
        TextView tx_send_msg = dialoglayout.findViewById(R.id.tv_select);
        tx_send_msg.setText(textSend);
        tx_send_msg.setEnabled(false);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(2, editSendMsg.getText().toString(), editSendMsg, context, alertDialog);
            }
        });
        tx_send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(1, editSendMsg.getText().toString(), editSendMsg, context, alertDialog);
            }
        });
        editSendMsg.setHint(textHint);
        if (!TextUtils.isEmpty(text)) {
            editSendMsg.setText(text);
        }
        editSendMsg.setFocusable(true);
        editSendMsg.setFocusableInTouchMode(true);
        editSendMsg.requestFocus();

        KeyBoardUtils.openKeybord(editSendMsg, context);
        editSendMsg.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (KeyEvent.KEYCODE_ENTER == keyCode && KeyEvent.ACTION_UP == event.getAction()) {
                    sendMessage(1, editSendMsg.getText().toString(), editSendMsg, context, alertDialog);
                    return true;
                }
                //                else if (KeyEvent.ACTION_UP == event.getAction()) {
                //                    if (onDialogClick != null) {
                //                        onDialogClick.setText(text);
                //                        editSendMsg.setText("");
                //                    }
                //                    KeyBoardUtils.closeKeybord(editSendMsg, context);
                //                    if (alertDialog != null && alertDialog.isShowing())
                //                        alertDialog.dismiss();
                //                    return true;
                //                }
                return false;
            }
        });
        alertDialog.show();
    }

    private void sendMessage(int type, String text, AppCompatEditText editSendMsg, Context context, Dialog alertDialog) {
        if (onClickListener != null) {
            if (type == 1) {
                onClickListener.sendText(text);
            } else {
                onClickListener.setText(text);
            }
            editSendMsg.setText("");
        }
        KeyBoardUtils.closeKeybord(editSendMsg, context);
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
    }

    public interface OnDialogClick {
        void sendText(String text);

        void setText(String text);
    }

    /**
     * 设置回调
     */
    OnDialogClick onClickListener;

    public void setOnDialogListener(OnDialogClick onClickListener) {
        this.onClickListener = onClickListener;

    }
}
