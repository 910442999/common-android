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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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

    // 全局监听器管理器
    private static final KeyboardListenerManager listenerManager = new KeyboardListenerManager();

    public interface OnKeyboardListener {
        void onKeyboardChanged(boolean isVisible);
    }

    public static void setKeyboardListener(final Activity activity, final OnKeyboardListener listener) {
        // 使用弱引用防止内存泄漏
        WeakReference<Activity> activityRef = new WeakReference<>(activity);

        if (listener != null) {
            listenerManager.addListener(activity, listener);
        } else {
            listenerManager.removeListenersForActivity(activity);
            return;
        }

        // 确保只添加一次根视图监听
        if (listenerManager.shouldAttachListener(activity)) {
            View rootView = activity.getWindow().getDecorView();
            boolean[] isKeyboardVisible = new boolean[]{false};

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                    boolean isVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
                    if (isKeyboardVisible[0] != isVisible) {
                        isKeyboardVisible[0] = isVisible;
                        listenerManager.notifyListeners(activity, isVisible);
                    }
                    return insets;
                });
            } else {
                // 兼容旧版API
                final View contentView = activity.findViewById(android.R.id.content);
                final ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                    private final Rect r = new Rect();
                    private boolean wasOpened = false;

                    @Override
                    public void onGlobalLayout() {
                        Activity currentActivity = activityRef.get();
                        if (currentActivity == null || currentActivity.isFinishing()) {
                            removeListener();
                            return;
                        }

                        contentView.getWindowVisibleDisplayFrame(this.r);
                        int screenHeight = contentView.getRootView().getHeight();
                        int heightDiff = screenHeight - (this.r.bottom - this.r.top);
                        int threshold = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                200.0f, activity.getResources().getDisplayMetrics());
                        boolean isOpen = heightDiff > threshold;

                        if (isOpen != this.wasOpened) {
                            this.wasOpened = isOpen;
                            listenerManager.notifyListeners(activity, isOpen);
                        }
                    }

                    private void removeListener() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            contentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            contentView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                    }
                };

                contentView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

                // 在Activity销毁时自动移除监听器
                rootView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {
                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            contentView.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
                        } else {
                            contentView.getViewTreeObserver().removeGlobalOnLayoutListener(layoutListener);
                        }
                        rootView.removeOnAttachStateChangeListener(this);
                    }
                });
            }
        }
    }

    // 移除特定监听器
    public static void removeKeyboardListener(Activity activity, OnKeyboardListener listener) {
        listenerManager.removeListener(activity, listener);
    }

    // 移除特定Activity的所有监听器
    public static void removeAllListenersForActivity(Activity activity) {
        listenerManager.removeListenersForActivity(activity);
    }

    // 监听器管理器
    private static class KeyboardListenerManager {
        private final List<ActivityListener> listeners = new ArrayList<>();

        public void addListener(Activity activity, OnKeyboardListener listener) {
            // 检查是否已存在相同Activity和listener的组合
            for (ActivityListener al : listeners) {
                if (al.activityRef.get() == activity && al.listener == listener) {
                    return;
                }
            }

            listeners.add(new ActivityListener(activity, listener));
        }

        public void removeListener(Activity activity, OnKeyboardListener listener) {
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ActivityListener al = listeners.get(i);
                Activity storedActivity = al.activityRef.get();

                if (storedActivity == null || storedActivity == activity && al.listener == listener) {
                    listeners.remove(i);
                }
            }
        }

        public void removeListenersForActivity(Activity activity) {
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ActivityListener al = listeners.get(i);
                Activity storedActivity = al.activityRef.get();

                if (storedActivity == null || storedActivity == activity) {
                    listeners.remove(i);
                }
            }
        }

        public void notifyListeners(Activity sourceActivity, boolean isVisible) {
            for (ActivityListener al : listeners) {
                Activity storedActivity = al.activityRef.get();
                if (storedActivity != null && storedActivity == sourceActivity) {
                    try {
                        al.listener.onKeyboardChanged(isVisible);
                    } catch (Exception e) {
                        // 防止个别listener出错影响其他
                        e.printStackTrace();
                    }
                }
            }
        }

        public boolean shouldAttachListener(Activity activity) {
            // 如果已有该Activity的监听器，则不再附加新的根监听
            for (ActivityListener al : listeners) {
                Activity storedActivity = al.activityRef.get();
                if (storedActivity != null && storedActivity == activity) {
                    // 当前Activity已有监听，不需要再次附加根监听
                    return false;
                }
            }
            return true;
        }
    }

    // 包装类，存储Activity和监听器的关联
    private static class ActivityListener {
        final WeakReference<Activity> activityRef;
        final OnKeyboardListener listener;

        ActivityListener(Activity activity, OnKeyboardListener listener) {
            this.activityRef = new WeakReference<>(activity);
            this.listener = listener;
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
