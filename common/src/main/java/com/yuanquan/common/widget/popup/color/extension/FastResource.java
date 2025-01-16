package com.yuanquan.common.widget.popup.color.extension;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.SparseArray;

import androidx.annotation.NonNull;

public class FastResource {
    private static final SparseArray<FastColorDrawable> drawables = new SparseArray<>();

    public static Drawable createColorBackground(int mainColor) {
        GradientDrawable shape = new GradientDrawable();
        shape.setStroke(1, mainColor);
        shape.setCornerRadius(4);

        StateListDrawable state = new StateListDrawable();
        state.addState(new int[]{android.R.attr.state_selected}, shape);
        return state;
    }

    public static Drawable getColorDrawable(int color) {
        FastColorDrawable drawable = drawables.get(color);
        if (drawable == null) {
            drawable = createColorDrawable(color);
            drawables.append(color, drawable);
        }
        return drawable;
    }

    @NonNull
    private static FastColorDrawable createColorDrawable(int color) {
        return new FastColorDrawable(color, 1, 1);
    }
}
