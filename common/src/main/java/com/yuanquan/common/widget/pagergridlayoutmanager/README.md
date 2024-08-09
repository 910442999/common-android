//是否开启调试日志
PagerGridLayoutManager.setDebug(BuildConfig.DEBUG);

PagerGridLayoutManager layoutManager = new PagerGridLayoutManager(
/*rows*/3,
/*columns*/ 3,
/*PagerGridLayoutManager.VERTICAL*/PagerGridLayoutManager.HORIZONTAL,
/*reverseLayout*/ false
);
/*
是否启用处理滑动冲突滑动冲突，default: true；若不需要库中自带的处理方式，则置为false，自行处理。
setHandlingSlidingConflictsEnabled() 必须要在{@link RecyclerView#setLayoutManager(RecyclerView.LayoutManager)} 之前调用，否则无效
you must call this method before {@link RecyclerView#setLayoutManager(RecyclerView.LayoutManager)}
*/
layoutManager.setHandlingSlidingConflictsEnabled(true);

recyclerView.setLayoutManager(layoutManager);

//设置监听
layoutManager.setPagerChangedListener(new PagerGridLayoutManager.PagerChangedListener() {
/**
* 页数回调
* 仅会在页数变化时回调
* @param pagerCount 页数，从1开始，为0时说明无数据
*/
@Override
public void onPagerCountChanged(int pagerCount) {
Log.w(TAG, "onPagerCountChanged-pagerCount:" + pagerCount);
}

    /**
     * 选中的页面下标
     * 从0开始
     * @param prePagerIndex     上次的页码，当{{@link PagerGridLayoutManager#getItemCount()}}为0时，为-1，{{@link PagerGridLayoutManager#NO_ITEM}}
     * @param currentPagerIndex 当前的页码，当{{@link PagerGridLayoutManager#getItemCount()}}为0时，为-1，{{@link PagerGridLayoutManager#NO_ITEM}}
     */
    @Override
    public void onPagerIndexSelected(int prePagerIndex, int currentPagerIndex) {
        Log.w(TAG, "onPagerIndexSelected-prePagerIndex " + prePagerIndex + ",currentPagerIndex:" + currentPagerIndex);
    }
});
//设置滑动方向
layoutManager.setOrientation(/*PagerGridLayoutManager.HORIZONTAL*/ PagerGridLayoutManager.VERTICAL);
/*
是否反向布局，自动兼容RTL；
注意：水平方向反向是排列顺序和滑动放向都反向，垂直方向仅排列顺序反向；
Whether the layout is reversed, automatically compatible with RTL;
Note: The horizontal reverse is the reverse of the arrangement order and the sliding direction, and the vertical direction is only the reverse of the arrangement order;
*/
layoutManager.setReverseLayout(/*true*/ false);
//设置行数
layoutManager.setRows(2);
//设置列数
layoutManager.setColumns(2);

//滚动到指定位置，注意：这个方法只会滚动到目标位置所在的页。
recyclerView.scrollToPosition(10);
//平滑滚动到指定位置，注意：这个方法只会滚动到目标位置所在的页。
recyclerView.smoothScrollToPosition(10);

//滚动到指定页
layoutManager.scrollToPagerIndex(3);
//平滑滚动到指定页，注意：如果滚动的页与当前页超过3，避免长时间滚动，会先直接滚动到就近的页，再做平滑滚动
layoutManager.smoothScrollToPagerIndex(6);
//滚动到上一页
layoutManager.scrollToPrePager();
//滚动到下一页
layoutManager.scrollToNextPager();
//平滑滚动到上一页
layoutManager.smoothScrollToPrePager();
//平滑滚动到下一页
layoutManager.smoothScrollToNextPager();
//设置滑动每像素需要花费的时间，不可过小，不然可能会出现划过再回退的情况。默认值：70
layoutManager.setMillisecondPreInch(70);
//设置最大滚动时间，如果您想此值无效，请使用{@link Integer#MAX_VALUE}。默认值：200 ms
layoutManager.setMaxScrollOnFlingDuration(200);
