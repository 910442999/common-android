package com.yuanquan.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yuanquan.common.R;

/**
 * Created by yjf
 *
 * @description:
 * @date :2019/11/29
 */
public class PreviewImageDialog {

    private Dialog dialog;
    private String url = "";
    private Bitmap bitmap;
    private int type = 0;
    private int width = 0;
    private int height = 0;
    Context context;

    public PreviewImageDialog(Context context) {
        this.context = context;
    }


    public PreviewImageDialog builder() {
        // 获取Dialog布局
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_preview_image, null);
        View ivColse = view.findViewById(R.id.iv_close);
        ImageView iv_imageview = view.findViewById(R.id.iv_imageview);
        ivColse.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // 定义Dialog布局和参数
        dialog = new Dialog(context, R.style.ActivityFullScreenTheme);
        dialog.setContentView(view);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        if (width > 0) {
            lp.width = width;
        } else {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        if (height > 0) {
            lp.height = height;
        } else {
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);
        dialog.setOnDismissListener(dialogInterface -> {
            if (onDisMissListener != null) {
                onDisMissListener.dismiss();
            }
        });
        if (type == 0) {
            Glide.with(context).load(url).into(iv_imageview);
        } else if (type == 1) {
            iv_imageview.setImageBitmap(bitmap);
        }
        return this;

    }


    public OnSheetItemClickListener onSheetItemClickListener;

    public PreviewImageDialog setOnSheetItemClickListener(OnSheetItemClickListener onSheetItemClickListener) {
        this.onSheetItemClickListener = onSheetItemClickListener;
        return this;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public PreviewImageDialog show() {
        if (isShow()) {
            dialog.dismiss();
        }
        dialog.show();
        return this;
    }

    public boolean isShow() {
        if (dialog != null && dialog.isShowing()) {
            return true;
        }
        return false;
    }

    public interface OnSheetItemClickListener {
        void onClick(String nickName);
    }

    public OnDisMissListener onDisMissListener;

    public PreviewImageDialog setOnDisMissListener(OnDisMissListener onDisMissListener) {
        this.onDisMissListener = onDisMissListener;
        return this;
    }

    public interface OnDisMissListener {
        void dismiss();
    }

}
