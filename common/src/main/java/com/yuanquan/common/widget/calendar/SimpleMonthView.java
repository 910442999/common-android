package com.yuanquan.common.widget.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.MonthView;
import com.yuanquan.common.LanguageUtils;
import com.yuanquan.common.utils.SysUtils;

/**
 * 高仿魅族日历布局
 * Created by huanghaibin on 2017/11/15.
 */

public class SimpleMonthView extends MonthView {

    private int mRadius;
    /**
     * 背景圆点
     */
    private Paint mPointPaint = new Paint();
    /**
     * 圆点半径
     */
    private float mPointRadius;
    private int mH, mW;

    public SimpleMonthView(Context context) {
        super(context);
        mPointPaint.setAntiAlias(true);
        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setTextAlign(Paint.Align.CENTER);
        mPointRadius = SysUtils.dp2Px(context, 2);
        mH = SysUtils.dp2Px(getContext(), 2);
        mW = SysUtils.dp2Px(getContext(), 8);
    }

    @Override
    protected void onPreviewHook() {
        mRadius = Math.min(mItemWidth, mItemHeight) / 5 * 2;
    }

    @Override
    protected void onLoopStart(int x, int y) {

    }

    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme) {
        int cx = x + mItemWidth / 2;
        int cy = y + mItemHeight / 2;
        canvas.drawCircle(cx, cy, mRadius, mSelectedPaint);
        return false;
    }

    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y) {
        canvas.drawCircle(x + mItemWidth / 2, y + mItemHeight - mH, mPointRadius, mSelectedPaint);
    }

    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected) {
        float baselineY = mTextBaseLine + y;
        int cx = x + mItemWidth / 2;
        int cy = y + mItemHeight / 2;

        if (calendar.isCurrentDay() && !isSelected) {
            canvas.drawCircle(cx, cy, mRadius, mSelectedPaint);
        }

        canvas.drawText(calendar.isCurrentDay() ? LanguageUtils.optString("今") : String.valueOf(calendar.getDay()),
                cx,
                baselineY,
                calendar.isCurrentDay() || isSelected ? mSelectTextPaint :
                        calendar.isCurrentMonth() ? mSchemeTextPaint : mOtherMonthTextPaint);

        if (hasScheme) {
            canvas.drawCircle(cx, y + mItemHeight - mH, mPointRadius, mSelectedPaint);
        }
    }
}
