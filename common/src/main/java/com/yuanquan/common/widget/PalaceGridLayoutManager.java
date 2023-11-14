package com.yuanquan.common.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Description:自定义九宫格LayoutManager类
 */
public class PalaceGridLayoutManager extends RecyclerView.LayoutManager {
    public static final int STATE_ONE = 1;//1
    public static final int STATE_TOW = 2;//2
    public static final int STATE_THREE = 3;//2*2
    public static final int STATE_FOUR = 4;//2*2
    public static final int STATE_FIVE = 5;//3*2
    public static final int STATE_SIX = 6;//3*2
    private Pool<Rect> mCacheBorders;   //用于规定Item显示的区域
    private int totalWidth;
    private int itemWidth;
    private int itemHeight;

    public PalaceGridLayoutManager(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mCacheBorders = new Pool<>(new Pool.New<Rect>() {
            @Override
            public Rect get() {
                return new Rect();
            }
        });
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return false;
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    /**
     * 测量RecyclerView控件的宽高
     *
     * @param recycler
     * @param state
     * @param widthSpec
     * @param heightSpec
     */
    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        super.onMeasure(recycler, state, widthSpec, heightSpec);
        if (getItemCount() <= 0 || state.isPreLayout()) {
            return;
        }
        totalWidth = View.MeasureSpec.getSize(widthSpec);//不能直接用getWidth 可能获取 0
        itemHeight = View.MeasureSpec.getSize(heightSpec);
        itemWidth = (totalWidth - getPaddingLeft() - getPaddingRight());
        int childCount = getItemCount();
        if (childCount < 0) {
            return;
        }
        if (childCount == STATE_ONE) {
            //整个布局高度
        } else if (childCount == STATE_TOW) {
            itemWidth = itemWidth / 2;
        }
        else if (childCount == STATE_THREE) {
            //三个横向
            itemWidth = itemWidth / 3;
        } else if (/*childCount == STATE_THREE||*/childCount == STATE_FOUR || childCount == STATE_FIVE) {
            itemWidth = itemWidth / 2;
            itemHeight = itemHeight / 2;
        } else if (childCount == STATE_SIX) {
            itemWidth = itemWidth / 3;
            itemHeight = itemHeight / 2;
        } else if (childCount > 6 && childCount <= 9) {
            itemWidth = itemWidth / 3;
            itemHeight = itemHeight / 3;
        }
        Log.e("TAG", "onMeasure: childCount" + childCount);
        Log.e("TAG", "onMeasure: " + itemWidth + "   " + itemHeight);
        //TODO:解决 Padding问题
        itemHeight += getPaddingTop() + getPaddingBottom();
        //        widthSpec = View.MeasureSpec.makeMeasureSpec(itemWidth, View.MeasureSpec.EXACTLY);
        //        heightSpec = View.MeasureSpec.makeMeasureSpec(itemHeight, View.MeasureSpec.EXACTLY);
        super.onMeasure(recycler, state, widthSpec, heightSpec);
    }

    /**
     * @param recycler
     * @param state
     */
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //        if (getItemCount() <= 0 || state.isPreLayout()) {//预布局状态不考虑
        //            return;
        //        }

        if (state.getItemCount() == 0) {
            /*
             * 没有Item可布局,就回收全部临时缓存(参考自带的LinearLayoutManager)
             * 这里没有item，是指adapter里面的数据集
             * 可能临时被清空了，但不确定何时还会继续添加回来。
             */
            removeAndRecycleAllViews(recycler);
            return;
        }
        //界面上的所有item都detach掉, 并缓存在scrap中,以便下次直接拿出来显示
        detachAndScrapAttachedViews(recycler);
        layoutChunk();
        fill(recycler);
    }

    /**
     * 测量
     */
    private void layoutChunk() {
        //        int childCount = getItemCount();
        int cl = getPaddingLeft();
        int ct = getPaddingTop();
        switch (getItemCount()) {
            case STATE_ONE:
                for (int i = 0; i < getItemCount(); i++) {
                    Rect item = mCacheBorders.get(i);
                    item.set(cl, ct, cl + itemWidth, ct + itemHeight);
                }
                break;
            case STATE_TOW:

            case STATE_THREE:
                //三个横排
                for (int i = 0; i < getItemCount(); i++) {
                    Rect item = mCacheBorders.get(i);
                    item.set(cl, ct, cl + itemWidth, ct + itemHeight);
                    // 累加宽度
                    cl += itemWidth;
                }
                break;
            //三角形
//            case STATE_THREE:
//                for (int i = 0; i < getItemCount(); i++) {
//                    Log.e("TAG", "3布局: " + i);
//                    Rect item = mCacheBorders.get(i);
//                    item.set(cl, ct, cl + itemWidth, ct + itemHeight);
//                    // 累加宽度
//                    cl += itemWidth;
//                    // 如果是换行
//                    if ((i + 1) % 2 == 0) {//2*2
//                        Log.e("TAG", "3换行: " + i);
//                        // 重置左边的位置
//                        cl = getPaddingLeft() + itemWidth / 2;
//                        // 叠加高度
//                        ct += itemHeight;
//                    }
//                }
//                break;
            case STATE_FOUR:
                for (int i = 0; i < getItemCount(); i++) {
                    Rect item = mCacheBorders.get(i);
                    item.set(cl, ct, cl + itemWidth, ct + itemHeight);
                    // 累加宽度
                    cl += itemWidth;
                    // 如果是换行
                    if ((i + 1) % 2 == 0) {//2*2
                        // 重置左边的位置
                        cl = getPaddingLeft();
                        // 叠加高度
                        ct += itemHeight;
                    }
                }
                break;
            case STATE_FIVE:
                for (int i = 0; i < getItemCount(); i++) {
                    Log.e("TAG", "5布局: " + i);
                    Rect item = mCacheBorders.get(i);
                    item.set(cl, ct, cl + itemWidth, ct + itemHeight);
                    // 累加宽度
                    cl += itemWidth;
                    // 如果是换行
                    if (i == 1) {
                        Log.e("TAG", "5换行: " + i);
                        itemWidth = totalWidth / 3;
                        // 重置左边的位置
                        cl = getPaddingLeft();
                        // 叠加高度
                        ct += itemHeight;
                    }
                }
                break;
            default:
                for (int i = 0; i < getItemCount(); i++) {
                    Rect item = mCacheBorders.get(i);
                    item.set(cl, ct, cl + itemWidth, ct + itemHeight);
                    // 累加宽度
                    cl += itemWidth;
                    // 如果是换行
                    if ((i + 1) % 3 == 0) {//3列
                        // 重置左边的位置
                        cl = getPaddingLeft();
                        // 叠加高度
                        ct += itemHeight;
                    }
                }
                break;
        }
    }


    /**
     * 填充
     *
     * @param recycler
     */
    private void fill(RecyclerView.Recycler recycler) {
        int itemSpecW;
        int itemSpecH;
        for (int i = 0; i < getItemCount(); i++) {
            Rect frame = mCacheBorders.get(i);
            View scrap = recycler.getViewForPosition(i);
            addView(scrap);
            itemSpecW = View.MeasureSpec.makeMeasureSpec(frame.width(), View.MeasureSpec.EXACTLY);
            itemSpecH = View.MeasureSpec.makeMeasureSpec(frame.height(), View.MeasureSpec.EXACTLY);
            scrap.measure(itemSpecW, itemSpecH);
            layoutDecorated(scrap, frame.left, frame.top, frame.right, frame.bottom);
        }
    }


}
