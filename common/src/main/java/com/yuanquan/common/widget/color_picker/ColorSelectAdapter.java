package com.yuanquan.common.widget.color_picker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yuanquan.common.R;

import java.util.List;

/**
 * @author fenglibin
 */
public class ColorSelectAdapter extends RecyclerView.Adapter<ColorSelectAdapter.ViewHolder> {
    private List<String> colors;
    private String curColor;
    private OnColorClickListener onColorClickListener;

    public ColorSelectAdapter(List<String> colors) {
        this.colors = colors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color_select, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        String color1 = colors.get(position);
        int color = Color.parseColor(color1);
        viewHolder.colorDisplay.setBackgroundColor(color);
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
