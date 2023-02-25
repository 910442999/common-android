package com.yuanquan.common.widget.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.contrarywind.view.WheelView;
import com.yuanquan.common.LanguageUtils;
import com.yuanquan.common.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 出生年月选择器
 */
public class ScreenTimeDialog {
    TimePickerView pickerView;

    public ScreenTimeDialog(Context context, String title) {
        Calendar selectedDate = Calendar.getInstance();//系统当前时间
        Calendar startDate = Calendar.getInstance();
        startDate.set(1930, 1, 1);
        pickerView = new TimePickerBuilder(context, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                if (onclickListener != null) {
                    onclickListener.onSelect(getTime(date));
                }
            }
        })
                .setLayoutRes(R.layout.pickerview_custom_time, v -> { //自定义布局
                    TextView tv_title = v.findViewById(R.id.tv_title);
                    tv_title.setText(title);
                    TextView tv_finish = v.findViewById(R.id.tv_finish);
                    tv_finish.setText(LanguageUtils.optString("提交"));
                    tv_finish.setOnClickListener(v1 -> {
                        pickerView.returnData(); //pickerView调用返回值
                        pickerView.dismiss();
                    });
                })
                .setDate(selectedDate)
                .setRangDate(startDate, selectedDate)
                .setContentTextSize(15)
                .setLineSpacingMultiplier(2.0f)
                .setDividerColor(context.getResources().getColor(R.color.bg_AAAAAA))
                .setDividerType(WheelView.DividerType.WRAP)
                .setTextColorCenter(context.getResources().getColor(R.color.colorPrimary))
                .setTextColorOut(context.getResources().getColor(R.color.txt_33))
                .isAlphaGradient(true)
                .setItemVisibleCount(7)
                .setType(new boolean[]{true, true, true, false, false, false})
                .setLabel("", "", "", "", "", "")
                .build();
        pickerView.show();
    }

    private String getTime(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    public interface OnSheetSelectListener {
        void onSelect(String which);
    }

    OnSheetSelectListener onclickListener;

    public void setOnSheetItemSelectListener(OnSheetSelectListener onclickListener) {
        this.onclickListener = onclickListener;
    }


}
