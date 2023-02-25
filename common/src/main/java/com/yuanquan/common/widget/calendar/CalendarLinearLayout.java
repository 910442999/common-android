package com.yuanquan.common.widget.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.haibin.calendarview.CalendarLayout;

/**
 * 如果嵌套各种View出现事件冲突，可以实现这个方法即可
 */
public class CalendarLinearLayout extends LinearLayout implements CalendarLayout.CalendarScrollView {

    private RecyclerView mRecyclerView;

    public CalendarLinearLayout(Context context) {
        super(context);
    }

    public CalendarLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 如果你想让下拉无效，return false
     *
     * @return isScrollToTop
     */
    @Override
    public boolean isScrollToTop() {
        if (mRecyclerView == null) {
            if (getChildCount() > 1 && getChildAt(1) instanceof RecyclerView) {
                mRecyclerView = (RecyclerView) getChildAt(1);
            }
        }
        return mRecyclerView != null && mRecyclerView.computeVerticalScrollOffset() == 0;
    }

}
