package com.yuanquan.common.widget.flow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatTextView;

import com.yuanquan.common.R;


/**
 * @author zwl
 * @date on 2021/8/5
 */
public class FoldAdapter extends FlowAdapter<String> {

    @Override
    public View getView(ViewGroup parent, String item, int position) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fold, null);
    }

    @Override
    public void initView(View view, String item, int position) {
        AppCompatTextView textView = view.findViewById(R.id.item_tv);
        textView.setText(item);
        textView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.setItemClick(item);
            }
        });

    }
}
