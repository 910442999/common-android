package com.yuanquan.common.widget.calendar;

import android.content.Context;
import android.graphics.Canvas;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.WeekView;
import com.yuanquan.common.utils.SysUtils;

/**
 * 简单周视图
 * Created by huanghaibin on 2017/11/29.
 */

public class SimpleWeekView extends WeekView {
    private int mRadius;
    /**
     * 圆点半径
     */
    private float mPointRadius;
    private int mH, mW;
    private String currentDay = "今";

    public SimpleWeekView(Context context) {
        super(context);
        mPointRadius = SysUtils.dp2Px(context, 2);
        mH = SysUtils.dp2Px(getContext(), 2);
        mW = SysUtils.dp2Px(getContext(), 8);
    }

    @Override
    protected void onPreviewHook() {
        mRadius = Math.min(mItemWidth, mItemHeight) / 5 * 2;
    }

    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, boolean hasScheme) {
        int cx = x + mItemWidth / 2;
        int cy = mItemHeight / 2;
        canvas.drawCircle(cx, cy, mRadius, mSelectedPaint);
        return false;
    }

    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x) {
        canvas.drawCircle(x + mItemWidth / 2, mItemHeight - mH, mPointRadius, mSelectedPaint);
    }

    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, boolean hasScheme, boolean isSelected) {
        float baselineY = mTextBaseLine;
        int cx = x + mItemWidth / 2;
        int cy = mItemHeight / 2;
        if (calendar.isCurrentDay() && !isSelected) {
            canvas.drawCircle(cx, cy, mRadius, mSelectedPaint);
        }
        canvas.drawText(calendar.isCurrentDay() ? this.currentDay : String.valueOf(calendar.getDay()),
                cx,
                baselineY,
                calendar.isCurrentDay() || isSelected ? mSelectTextPaint :
                        calendar.isCurrentMonth() ? mSchemeTextPaint : mOtherMonthTextPaint);
        if (hasScheme) {
            canvas.drawCircle(cx, mItemHeight - mH, mPointRadius, mSelectedPaint);
        }
    }

    protected void setCurrentDay(String currentDay) {
        this.currentDay = currentDay;
    }
}
