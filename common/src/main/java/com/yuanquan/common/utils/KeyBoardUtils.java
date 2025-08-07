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

import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 软键盘工具类
 */
public class KeyBoardUtils {


    public static void openKeyboard(View view, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


    public static void closeKeyboard(View view, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
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

    private static View rootView;
    private static int initialScrollY;
    private static ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

    public static void adjustPanOnKeyboardVisible(Activity activity) {
        rootView = activity.findViewById(android.R.id.content);
        initialScrollY = rootView.getScrollY();
        onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
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
        };
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public static void restoreLayout() {
        rootView.scrollTo(0, initialScrollY);
    }

    public void removeOnGlobalLayoutListener() {
        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }


    public interface OnKeyboardListener {
        void onKeyboardChanged(boolean isVisible);
    }

    // 全局监听器管理器
    private static final KeyboardListenerManager listenerManager = new KeyboardListenerManager();

    public static void setKeyboardListener(@NonNull final Activity activity, @NonNull final OnKeyboardListener listener) {
        listenerManager.addListener(activity, listener);

        // 如果是首次为该Activity添加监听器，则附加根监听
        if (!listenerManager.hasAttachedRootListener(activity)) {
            attachRootListener(activity);
            listenerManager.markRootListenerAttached(activity);
        }
    }

    // 为 Fragment 设置监听器
    public static void setKeyboardListener(@NonNull final Fragment fragment,
                                           @NonNull final OnKeyboardListener listener) {
        Activity activity = fragment.getActivity();
        if (activity != null) {
            setKeyboardListener(activity, listener);
        } else {
            // 使用 Fragment 的生命周期观察者
            fragment.getLifecycle().addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source,
                                           @NonNull Lifecycle.Event event) {
                    if (event == Lifecycle.Event.ON_CREATE) {
                        Activity activity = fragment.getActivity();
                        if (activity != null) {
                            setKeyboardListener(activity, listener);
                            fragment.getLifecycle().removeObserver(this);
                        }
                    } else if (event == Lifecycle.Event.ON_DESTROY) {
                        // 如果 Fragment 被销毁而 Activity 还没有附加，移除观察者
                        fragment.getLifecycle().removeObserver(this);
                    }
                }
            });
        }
    }

    private static void attachRootListener(@NonNull final Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            attachModernListener(activity);
        } else {
            attachLegacyListener(activity);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void attachModernListener(@NonNull final Activity activity) {
        final View rootView = activity.getWindow().getDecorView();
        final WeakReference<Activity> activityRef = new WeakReference<>(activity);
        final boolean[] lastKeyboardState = {false};

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            boolean currentState = insets.isVisible(WindowInsetsCompat.Type.ime());

            if (currentState != lastKeyboardState[0]) {
                lastKeyboardState[0] = currentState;

                Activity currentActivity = activityRef.get();
                if (currentActivity != null && !currentActivity.isDestroyed()) {
                    listenerManager.notifyListeners(currentActivity, currentState);
                }
            }
            return insets;
        });
    }

    private static void attachLegacyListener(@NonNull final Activity activity) {
        final View contentView = activity.findViewById(android.R.id.content);
        if (contentView == null) return;

        final WeakReference<Activity> activityRef = new WeakReference<>(activity);
        final WeakReference<View> contentViewRef = new WeakReference<>(contentView);

        // 创建全局布局监听器
        final ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            private final Rect rect = new Rect();
            private boolean lastOpened = false;

            @Override
            public void onGlobalLayout() {
                Activity currentActivity = activityRef.get();
                View currentContentView = contentViewRef.get();

                // 检查活动或视图是否已失效
                if (currentActivity == null || currentContentView == null || currentActivity.isFinishing() || currentActivity.isDestroyed()) {
                    // 不需要调用removeListener，视图分离时会处理
                    return;
                }

                // 获取可见显示区域
                currentContentView.getWindowVisibleDisplayFrame(rect);

                // 计算键盘高度
                int screenHeight = currentContentView.getRootView().getHeight();
                int heightDiff = screenHeight - (rect.bottom - rect.top);

                // 设置阈值（200dp）
                int threshold = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200.0f, activity.getResources().getDisplayMetrics());

                boolean isOpen = heightDiff > threshold;

                // 仅在状态变化时通知
                if (isOpen != lastOpened) {
                    lastOpened = isOpen;
                    listenerManager.notifyListeners(activity, isOpen);
                }
            }
        };

        // 添加全局布局监听
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

        // 添加视图附加状态监听器以处理清理
        contentView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                // 移除全局布局监听
                View view = contentViewRef.get();
                if (view != null && view.getViewTreeObserver().isAlive()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
                    } else {
                        view.getViewTreeObserver().removeGlobalOnLayoutListener(layoutListener);
                    }
                }

                // 移除Activity相关的所有监听器
                Activity currentActivity = activityRef.get();
                if (currentActivity != null) {
                    listenerManager.removeListenersForActivity(currentActivity);
                }

                // 移除当前附加状态监听器
                v.removeOnAttachStateChangeListener(this);
            }
        });
    }

    // 移除特定Activity的所有监听器
    public static void removeAllActivityKeyboardListeners(@NonNull Activity activity) {
        listenerManager.removeListenersForActivity(activity);
    }

    // 移除特定监听器
    public static void removeActivityKeyboardListener(@NonNull Activity activity, @NonNull OnKeyboardListener listener) {
        listenerManager.removeListener(activity, listener);
    }

    // 移除Fragment的监听器（如果嵌套在activity的时候只能根据监听移除，否则activity中的监听回调回无效）
    public static void removeKeyboardListenerForFragment(@NonNull Fragment fragment,
                                                         @NonNull OnKeyboardListener listener) {
        Activity activity = fragment.getActivity();
        if (activity != null) {
            removeActivityKeyboardListener(activity, listener);
        }
    }

    // 监听器管理器类
    private static class KeyboardListenerManager {
        // 存储每个Activity的监听器集合
        private final Map<Activity, ActivityListeners> listenerMap = new HashMap<>();
        // 存储已附加根监听的Activity
        private final List<Activity> rootListenerAttached = new ArrayList<>();

        public void addListener(@NonNull Activity activity, @NonNull OnKeyboardListener listener) {
            ActivityListeners listeners = listenerMap.get(activity);
            if (listeners == null) {
                listeners = new ActivityListeners();
                listenerMap.put(activity, listeners);
            }
            listeners.addListener(listener);
        }

        public void removeListener(@NonNull Activity activity, @NonNull OnKeyboardListener listener) {
            ActivityListeners listeners = listenerMap.get(activity);
            if (listeners != null) {
                listeners.removeListener(listener);
                if (listeners.isEmpty()) {
                    listenerMap.remove(activity);
                    rootListenerAttached.remove(activity);
                }
            }
        }

        public void removeListenersForActivity(@NonNull Activity activity) {
            listenerMap.remove(activity);
            rootListenerAttached.remove(activity);
        }

        public void notifyListeners(@NonNull Activity sourceActivity, boolean isVisible) {
            ActivityListeners listeners = listenerMap.get(sourceActivity);
            if (listeners != null) {
                listeners.notifyListeners(isVisible);
            }
        }

        public boolean hasAttachedRootListener(@NonNull Activity activity) {
            return rootListenerAttached.contains(activity);
        }

        public void markRootListenerAttached(@NonNull Activity activity) {
            if (!rootListenerAttached.contains(activity)) {
                rootListenerAttached.add(activity);
            }
        }
    }

    // 每个Activity的监听器集合
    private static class ActivityListeners {
        private final List<OnKeyboardListener> listeners = new ArrayList<>();

        public void addListener(@NonNull OnKeyboardListener listener) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }

        public void removeListener(@NonNull OnKeyboardListener listener) {
            listeners.remove(listener);
        }

        public boolean isEmpty() {
            return listeners.isEmpty();
        }

        public void notifyListeners(boolean isVisible) {
            // 创建副本以避免在迭代时修改列表
            List<OnKeyboardListener> copy = new ArrayList<>(listeners);
            for (OnKeyboardListener listener : copy) {
                try {
                    listener.onKeyboardChanged(isVisible);
                } catch (Exception e) {
                    // 防止一个监听器的异常影响其他监听器
                }
            }
        }
    }
}