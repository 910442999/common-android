package com.yuanquan.common.utils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.bigkoo.pickerview.view.TimePickerView;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.engine.CropFileEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.yuanquan.common.utils.picture.ImageFileCropEngine;
import com.yuanquan.common.utils.picture.GlideEngine;

import java.util.Date;
import java.util.List;

public class PickerUtils {
    public interface OnOptionSelectListener {
        void onOptionsSelect(int options1, int options2, int options3, View v);
    }

    public interface OnTimesSelectListener {
        void onTimeSelect(Date date, View v);
    }

    public static <T> OptionsPickerView showOptionsPicker(Context context, String textContentConfirm, String textContentCancel, int option1, List<T> optionsItems, final OnOptionSelectListener onSelectListener) {
        OptionsPickerView<T> mPickerView = new OptionsPickerBuilder(context, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                if (onSelectListener != null)
                    onSelectListener.onOptionsSelect(options1, option2, options3, v);
            }
        })
                .setSubmitColor(0xFFb5d260)//确定按钮文字颜色
                .setCancelColor(0xFFb5d260)//取消按钮文字颜色
                .setTitleBgColor(0xFFFFFFFF)//标题背景颜色 Night mode
                .setContentTextSize(18)//滚轮文字大小
                .setLineSpacingMultiplier(2)//滚轮文字大小
                .setSubmitText(textContentConfirm)
                .setCancelText(textContentCancel)
                .setSelectOptions(option1)
                .build();
        mPickerView.setPicker(optionsItems);
        mPickerView.show();
        return mPickerView;
    }

    public static <T> OptionsPickerView showOptionsPicker(Context context, String textContentConfirm, String textContentCancel, int option1, List<T> optionsItems, int res, CustomListener listener, final OnOptionSelectListener onSelectListener) {
        OptionsPickerView<T> mPickerView = new OptionsPickerBuilder(context, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                if (onSelectListener != null)
                    onSelectListener.onOptionsSelect(options1, option2, options3, v);
            }
        })
                .setSubmitColor(0xFFb5d260)//确定按钮文字颜色
                .setCancelColor(0xFFb5d260)//取消按钮文字颜色
                .setTitleBgColor(0xFFFFFFFF)//标题背景颜色 Night mode
                .setContentTextSize(18)//滚轮文字大小
                .setLineSpacingMultiplier(2)//滚轮文字大小
                .setSubmitText(textContentConfirm)
                .setCancelText(textContentCancel).
                setLayoutRes(res, listener)
                .setSelectOptions(option1)
                .build();
        mPickerView.setPicker(optionsItems);
        mPickerView.show();
        return mPickerView;
    }

    public static <T> OptionsPickerView showOptionsPicker(Context context, String textContentConfirm, String textContentCancel, int option1, List<T> optionsItems, int res, CustomListener listener, ViewGroup decorView, final OnOptionSelectListener onSelectListener) {
        OptionsPickerBuilder builder = new OptionsPickerBuilder(context, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                if (onSelectListener != null)
                    onSelectListener.onOptionsSelect(options1, option2, options3, v);
            }
        })
                .setSubmitColor(0xFFb5d260)//确定按钮文字颜色
                .setCancelColor(0xFFb5d260)//取消按钮文字颜色
                .setTitleBgColor(0xFFFFFFFF)//标题背景颜色 Night mode
                .setContentTextSize(18)//滚轮文字大小
                .setLineSpacingMultiplier(2)//滚轮文字大小
                .setSubmitText(textContentConfirm)
                .setCancelText(textContentCancel).
                setLayoutRes(res, listener)
                .setSelectOptions(option1);
        if (decorView != null) {
            builder.setDecorView(decorView);
        }
        OptionsPickerView<T> mPickerView = builder.build();
        mPickerView.setPicker(optionsItems);
        mPickerView.show();
        return mPickerView;
    }

    public static <T> OptionsPickerView showOptionsPicker(Context context, String textContentConfirm, String textContentCancel, int option1, List<T> optionsItems, int res, CustomListener listener, boolean isDialog, final OnOptionSelectListener onSelectListener) {
        OptionsPickerBuilder builder = new OptionsPickerBuilder(context, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                if (onSelectListener != null)
                    onSelectListener.onOptionsSelect(options1, option2, options3, v);
            }
        })
                .setSubmitColor(0xFFb5d260)//确定按钮文字颜色
                .setCancelColor(0xFFb5d260)//取消按钮文字颜色
                .setTitleBgColor(0xFFFFFFFF)//标题背景颜色 Night mode
                .setContentTextSize(18)//滚轮文字大小
                .setLineSpacingMultiplier(2)//滚轮文字大小
                .setSubmitText(textContentConfirm)
                .setCancelText(textContentCancel).
                setLayoutRes(res, listener)
                .setSelectOptions(option1)
                .isDialog(isDialog);
        OptionsPickerView<T> mPickerView = builder.build();
        mPickerView.setPicker(optionsItems);
        if (isDialog) {
            ViewGroup dialogContainerLayout = mPickerView.getDialogContainerLayout();
            ViewGroup.LayoutParams layoutParams = dialogContainerLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                marginLayoutParams.leftMargin = 0;
                marginLayoutParams.rightMargin = 0;
            }
            dialogContainerLayout.setLayoutParams(layoutParams);
            if (dialogContainerLayout.getChildCount() > 0) {
                View childView = dialogContainerLayout.getChildAt(0);
                ViewGroup.LayoutParams childLayoutParams = childView.getLayoutParams();
                childLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                childView.setLayoutParams(childLayoutParams);
            }
        }
        if (isDialog && mPickerView.getDialog() != null) {
            Window window = mPickerView.getDialog().getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setGravity(Gravity.BOTTOM);
                window.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);
            }
        }
        mPickerView.show();
        if (isDialog && mPickerView.getDialog() != null) {
            Window window = mPickerView.getDialog().getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setGravity(Gravity.BOTTOM);
                window.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);
                window.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT
                );
            }
        }
        return mPickerView;
    }

    public static void showTimePicker(Context context, final OnTimesSelectListener onTimesSelectListener) {
        showTimePicker(context, new boolean[]{true, true, true, false, false, false}, onTimesSelectListener);
    }

    public static void showTimePicker(Context context, boolean[] type, final OnTimesSelectListener onTimesSelectListener) {
        TimePickerView mPvTime = new TimePickerBuilder(context, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                if (onTimesSelectListener != null) {
                    onTimesSelectListener.onTimeSelect(date, v);
                }
            }
        })
                .setSubmitColor(0xFFb5d260)//确定按钮文字颜色
                .setCancelColor(0xFFb5d260)//取消按钮文字颜色
                .setTitleBgColor(0xFFFFFFFF)//标题背景颜色 Night mode
                .setContentTextSize(16)//滚轮文字大小
                .setLabel("年", "月", "日", "时", "分", "秒")
                .isCenterLabel(true)
                .setType(type)
                .build();
        mPvTime.show();
    }

    public static void pictureSelector(Context context, int language, int defaultLanguage, OnResultCallbackListener<LocalMedia> listener) {
        pictureSelector(context, language, defaultLanguage, 1, 1, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, new ImageFileCropEngine(), listener);
    }

    public static void pictureSelector(Context context, int language, int defaultLanguage, int maxSelectNum, int minSelectNum, int requestedOrientation, CropFileEngine engine, OnResultCallbackListener<LocalMedia> listener) {
        PictureSelector.create(context)
                .openGallery(SelectMimeType.ofImage())
                .setImageEngine(GlideEngine.createGlideEngine())
                .setMaxSelectNum(maxSelectNum)// 最大图片选择数量
                .setMinSelectNum(minSelectNum)// 最小选择数量
                .isDirectReturnSingle(true)
                .setLanguage(language)
                .setDefaultLanguage(defaultLanguage)
                .setRequestedOrientation(requestedOrientation)
                .setCropEngine(engine)
                .forResult(listener);
    }

}
