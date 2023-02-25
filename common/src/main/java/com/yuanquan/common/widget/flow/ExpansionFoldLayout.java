package com.yuanquan.common.widget.flow;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.yuanquan.common.R;
import com.yuanquan.common.utils.SysUtils;


public class ExpansionFoldLayout extends FlowListView {
    private View upFoldView;
    private View downFoldView;
    private int mRootWidth;

    public ExpansionFoldLayout(Context context) {
        this(context, null);
    }

    public ExpansionFoldLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpansionFoldLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRootWidth = SysUtils.getScreenWidth((Activity) context);
        upFoldView = LayoutInflater.from(context).inflate(R.layout.view_item_fold_up, null);
        upFoldView.setTag("up");
        downFoldView = LayoutInflater.from(context).inflate(R.layout.view_item_fold_down, null);
        downFoldView.setTag("down");
        upFoldView.setOnClickListener(v -> {
            mFold = false;
            flowAdapter.setFold(false);
            flowAdapter.notifyDataChanged();
        });

        downFoldView.setOnClickListener(v -> {
            mFold = true;
            flowAdapter.setFold(true);
            flowAdapter.notifyDataChanged();
        });

        setOnFoldChangedListener((canFold, fold, index, maxIndex, surplusWidth) -> {
            try {
                if (canFold) {
                    removeFromParent(downFoldView);
                    addView(downFoldView);
                    Log.d("yanjin", "maxIndex = " + maxIndex);
                    for (int x = 0; x < getChildCount(); x++) {
                        View view = getChildAt(x);
                        if (view.getTag() != null) {
                            Log.d("yanjin", "maxIndex tag = " + view.getTag());
                        }
                    }
                    if (fold) {
                        removeFromParent(upFoldView);
                        int upIndex = index(index, surplusWidth);
                        addView(upFoldView, upIndex);
                        //标签的后面不能有其他标签，所以好办法就是在他的后面加一个空的标签宽度是屏幕宽度
                        View emptyView = new View(getContext());
                        addView(emptyView, upIndex + 1, new FrameLayout.LayoutParams(mRootWidth, LayoutParams.MATCH_PARENT));
                    } else {
                        if (maxIndex != 0) {
                            removeFromParent(downFoldView);
                            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                            addView(downFoldView, maxIndex, layoutParams);
                            //标签的后面不能有其他标签，所以好办法就是在他的后面加一个空的标签宽度是屏幕宽度
                            View emptyView = new View(getContext());
                            addView(emptyView, maxIndex + 1, new FrameLayout.LayoutParams(mRootWidth, LayoutParams.MATCH_PARENT));
                        } else {
                            removeFromParent(downFoldView);
                            addView(downFoldView);
                            //标签的后面不能有其他标签，所以好办法就是在他的后面加一个空的标签宽度是屏幕宽度
                            View emptyView = new View(getContext());
                            addView(emptyView, new FrameLayout.LayoutParams(mRootWidth, LayoutParams.MATCH_PARENT));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private int index(int index, int surplusWidth) {
        int upIndex = index;
        int upWidth = getViewWidth(upFoldView);
        //当剩余空间大于等于展开View宽度直接加入index+1
        if (surplusWidth >= upWidth) {
            upIndex = index + 1;
        } else { //找到对应的位置
            for (int i = index; i >= 0; i--) {
                View view = getChildAt(index);
                int viewWidth = getViewWidth(view);
                upWidth -= viewWidth;
                if (upWidth <= 0) {
                    upIndex = i;
                    break;
                }
            }
        }
        return upIndex;
    }

    public static int getViewWidth(View view) {
        view.measure(0, 0);
        return view.getMeasuredWidth();
    }

    /**
     * 移除父布局中的子布局
     *
     * @param view
     */
    public static void removeFromParent(View view) {
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }
}

