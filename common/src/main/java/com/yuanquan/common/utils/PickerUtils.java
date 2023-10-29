package com.yuanquan.common.utils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.View;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
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

    public static <T> void showOptionsPicker(Context context, List<T> optionsItems, final OnOptionSelectListener onSelectListener) {

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
                .build();
        mPickerView.setPicker(optionsItems);
        mPickerView.show();
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
