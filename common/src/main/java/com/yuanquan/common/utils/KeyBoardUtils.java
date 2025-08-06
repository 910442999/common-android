package com.yuanquan.common.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 软键盘工具类
 */
public class KeyBoardUtils {


    public static void openKeyboard(View view, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


    public static void closeKeyboard(View view, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void closeKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        // 优先使用 View 获取焦点
        View focusedView = activity.getCurrentFocus();
        if (focusedView != null && focusedView.getWindowToken() != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            // 回退方案：通过 DecorView 关闭键盘
            View decorView = activity.getWindow().getDecorView();
            imm.hideSoftInputFromWindow(decorView.getWindowToken(), 0);
        }
    }

    public interface OnKeyboardListener {
        void onKeyboardChanged(boolean isVisible);
    }

    public static void setKeyboardListener(Activity activity, OnKeyboardListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View rootView = activity.getWindow().getDecorView();
            final boolean[] isKeyboardVisible = {false};
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                boolean isVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
                if (isKeyboardVisible[0] != isVisible) {
                    isKeyboardVisible[0] = isVisible;
                    listener.onKeyboardChanged(isVisible);
                }
                return insets;
            });
        } else {
            // 老方法
            final View contentView = activity.findViewById(android.R.id.content);
            contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                private final Rect r = new Rect();
                private boolean wasOpened = false;

                @Override
                public void onGlobalLayout() {
                    contentView.getWindowVisibleDisplayFrame(r);
                    int screenHeight = contentView.getRootView().getHeight();
                    int heightDiff = screenHeight - (r.bottom - r.top);
                    // 这里我们使用200dp作为阈值，因为很多设备上导航栏高度不超过200dp
                    int threshold = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, activity.getResources().getDisplayMetrics());
                    boolean isOpen = heightDiff > threshold;
                    if (isOpen != wasOpened) {
                        wasOpened = isOpen;
                        listener.onKeyboardChanged(isOpen);
                    }
                }
            });
        }
    }

    private static View rootView;
    private static int initialScrollY;

    public static void adjustPanOnKeyboardVisible(Activity activity) {
        rootView = activity.findViewById(android.R.id.content);
        initialScrollY = rootView.getScrollY();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean isKeyboardVisible = false;

            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);
                int screenHeight = rootView.getRootView().getHeight();
                int keyboardHeight = screenHeight - rect.bottom;

                if (keyboardHeight > 0) {
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                        int viewThreshold = (int) (screenHeight * 0.15); // 调整阈值根据需要
                        View focusedView = rootView.findFocus();
                        if (focusedView != null) {
                            int[] location = new int[2];
                            focusedView.getLocationOnScreen(location);
                            int viewBottom = location[1] + focusedView.getHeight();

                            if (keyboardHeight > viewThreshold && viewBottom > rect.bottom - viewThreshold) {
                                int scrollDistance = viewBottom - (rect.bottom - viewThreshold);
                                rootView.scrollBy(0, scrollDistance);
                            }
                        }
                    }
                } else {
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                        restoreLayout();
                    }
                }
            }
        });
    }

    public static void restoreLayout() {
        rootView.scrollTo(0, initialScrollY);
    }

    public void removeOnGlobalLayoutListener() {
//         rootView.getViewTreeObserver().removeOnGlobalLayoutListener(activity);
    }


}
