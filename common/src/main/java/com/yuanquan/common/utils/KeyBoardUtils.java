package com.yuanquan.common.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * 软键盘工具类
 */
public class KeyBoardUtils {


    public static void openKeybord(View view, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


    public static void closeKeybord(View view, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void closeKeybord(Activity activity) {
        if (activity.getCurrentFocus() != null && activity.getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
            //隐藏软键盘
            //            activity. getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    //纪录根视图的显示高度
    private static int rootViewVisibleHeight;
    private OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener;

    private void setOnSoftKeyBoardChangeListener(OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
        this.onSoftKeyBoardChangeListener = onSoftKeyBoardChangeListener;
    }

    public interface OnSoftKeyBoardChangeListener {
        void keyBoardShow(int height);

        void keyBoardHide(int height);
    }

    public static void setListener(Activity activity, OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
//获取activity的根视图
        View rootView = activity.getWindow().getDecorView();
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        rootViewVisibleHeight = r.height();
        //监听视图树中全局布局发生改变或者视图树中的某个视图的可视状态发生改变
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //获取当前根视图在屏幕上显示的大小
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);

                int visibleHeight = r.height();
                LogUtil.i("KeyBoardListener", "height: " + visibleHeight);
                if (rootViewVisibleHeight == 0) {
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

                //根视图显示高度没有变化，可以看作软键盘显示／隐藏状态没有改变
                if (rootViewVisibleHeight == visibleHeight) {
                    return;
                }

                //根视图显示高度变小超过200，可以看作软键盘显示了
                if (rootViewVisibleHeight - visibleHeight > 200) {
                    if (onSoftKeyBoardChangeListener != null) {
                        onSoftKeyBoardChangeListener.keyBoardShow(rootViewVisibleHeight - visibleHeight);
                    }
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

                //根视图显示高度变大超过200，可以看作软键盘隐藏了
                if (visibleHeight - rootViewVisibleHeight > 200) {
                    if (onSoftKeyBoardChangeListener != null) {
                        onSoftKeyBoardChangeListener.keyBoardHide(visibleHeight - rootViewVisibleHeight);
                    }
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

            }
        });
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
