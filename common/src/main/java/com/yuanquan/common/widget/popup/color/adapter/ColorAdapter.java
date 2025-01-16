package com.yuanquan.common.widget.popup.color.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yuanquan.common.R;
import com.yuanquan.common.widget.popup.color.extension.FastResource;

import java.util.List;

/**
 * @author fenglibin
 */
public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {
    private List<String> colors;
    private String curColor;
    private int mainColor;
    private OnColorClickListener onColorClickListener;

    public ColorAdapter(List<String> colors) {
        this.colors = colors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sub_tool_color, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        String color1 = colors.get(position);
        int color = Color.parseColor(color1);
        viewHolder.colorDisplay.setBackground(FastResource.createColorBackground(mainColor));
        viewHolder.colorDisplay.setImageDrawable(FastResource.getColorDrawable(color));
        viewHolder.colorDisplay.setSelected(color1.equals(curColor));
        viewHolder.itemView.setOnClickListener(v -> {
            if (onColorClickListener != null) {
                onColorClickListener.onColorClick(color1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    public void setOnColorClickListener(OnColorClickListener onColorClickListener) {
        this.onColorClickListener = onColorClickListener;
    }

    public void setColor(String color) {
        curColor = color;
        notifyDataSetChanged();
    }

//    public void setStyle(FastStyle style) {
//        this.mainColor = style.getMainColor();
//
//        notifyDataSetChanged();
//    }

    public interface OnColorClickListener {
        void onColorClick(String color);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView colorDisplay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorDisplay = itemView.findViewById(R.id.color_display);
        }
    }
}
