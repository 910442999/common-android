package com.yuanquan.common.widget.popup.color;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yuanquan.common.R;
import com.yuanquan.common.widget.dialog.BasePopWindow;
import com.yuanquan.common.widget.popup.color.adapter.ColorAdapter;

public class ColorPopWindow {
    private final BasePopWindow popWindow;
    private Context context;
    private RecyclerView colorsRecyclerView;
    private ColorAdapter colorAdapter;

    public ColorPopWindow(Context context) {
        this.context = context;
        View view = View.inflate(context, R.layout.layout_color_pop, null);
        popWindow = new BasePopWindow(view);
        popWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        colorsRecyclerView = view.findViewById(R.id.colors_recycler_view);
        colorAdapter = new ColorAdapter(FastUiSettings.getToolsColors());
        colorsRecyclerView.setAdapter(colorAdapter);
        colorsRecyclerView.setLayoutManager(new GridLayoutManager(context, 4));
        popWindow.setOnDismissListener(() -> {
            if (onDismissListener != null) {
                onDismissListener.onDismiss();
            }
        });
    }

    public boolean isShowing() {
        return popWindow != null && popWindow.isShowing();
    }

    public void show(View parent) {
        popWindow.showAsDropDown(parent, -parent.getLayoutParams().width * 2, 0);
        if (onShowListener != null) {
            onShowListener.onShow();
        }
    }

    public void setWidth(int width) {
        popWindow.setWidth(width);
    }

    public void show2(View parent) {
        popWindow.showAsDropDown(parent, 0, 0);
        if (onShowListener != null) {
            onShowListener.onShow();
        }
    }

    public void setOnColorClickListener(ColorAdapter.OnColorClickListener onColorClickListener) {
        colorAdapter.setOnColorClickListener(color -> {
            onColorClickListener.onColorClick(color);
            popWindow.dismiss();
        });
    }


    public void setColor(String color) {
        colorAdapter.setColor(color);
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    private OnDismissListener onDismissListener;

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public interface OnShowListener {
        void onShow();
    }

    private OnShowListener onShowListener;

    public void setOnShowListener(OnShowListener onShowListener) {
        this.onShowListener = onShowListener;
    }
}
