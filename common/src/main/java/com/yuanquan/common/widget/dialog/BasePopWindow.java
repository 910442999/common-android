package com.yuanquan.common.widget.dialog;

import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

public class BasePopWindow extends PopupWindow {
    public BasePopWindow(View contentView) {
        this.setContentView(contentView);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setBackgroundDrawable(null);
    }

    public void showH(View parent) {
        if (!this.isShowing()) {
            int[] location = new int[2];
            parent.getLocationOnScreen(location);
            this.showAtLocation(parent, Gravity.RIGHT, 0, 0);
//            this.showAsDropDown(parent, parent.getLayoutParams().width / 2, 18);
        } else {
            this.dismiss();
        }
    }

    public void showV(View parent) {
        if (!this.isShowing()) {
            int[] location = new int[2];
            parent.getLocationOnScreen(location);
            this.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        } else {
            this.dismiss();
        }


    }
}