package com.yuanquan.common.widget.indicatorview

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import android.widget.LinearLayout
import androidx.core.view.setMargins
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

/**
 * 源码参考
 * https://github.com/czy1121/indicatorview
 */

fun LinearLayout.setIndicator(
    indicator: BaseIndicatorView,
    gravity: Int? = null,
    margin: Int? = null
) {
    if (indicator.parent == null) {
        val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        lp.gravity = gravity ?: (Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
        lp.setMargins(margin ?: (10 * resources.displayMetrics.density).toInt())
        addView(indicator, lp)
    } else if (indicator.parent != this) {
        (indicator.parent as ViewGroup).removeView(indicator)
        addView(indicator)
    }
}


fun ViewPager2.setIndicator(view: BaseIndicatorView) {
    val adapter = this.adapter ?: throw IllegalStateException("ViewPager2.adapter is null")
    view.setItemCount(adapter.itemCount)
    adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            view.setItemCount(adapter.itemCount)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = onChanged()
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = onChanged()
    })

    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            view.update(position, positionOffset)
        }

        override fun onPageSelected(position: Int) {
            if (scrollState == ViewPager2.SCROLL_STATE_IDLE) {
                view.update(position, 0f)
            }
        }
    })
}