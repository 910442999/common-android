package com.yuanquan.common.adapter;


import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.dragswipe.listener.DragAndSwipeDataCallback;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.yuanquan.common.R;

public class DragAndSwipeAdapter extends BaseQuickAdapter<String, QuickViewHolder> implements DragAndSwipeDataCallback {

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_draggable_view, parent);
    }

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, String item) {
        int i = holder.getLayoutPosition() % 3;
        if (i == 0) {
            holder.setImageResource(R.id.iv_head, R.mipmap.check);
        } else if (i == 1) {
            holder.setImageResource(R.id.iv_head, R.mipmap.check1);
        } else if (i == 2) {
            holder.setImageResource(R.id.iv_head, R.mipmap.check2);
        }
        holder.setText(R.id.tv, item);
    }

    @Override
    public void dataMove(int fromPosition, int toPosition) {
        move(fromPosition, toPosition);
    }

    @Override
    public void dataRemoveAt(int position) {
        removeAt(position);
    }
}
