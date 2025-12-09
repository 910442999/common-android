package com.yuanquan.common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.yuanquan.common.R
import com.yuanquan.common.databinding.LayoutLogListItemBinding

class LogListAdapter : BaseQuickAdapter<String, LogListAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: LayoutLogListItemBinding = LayoutLogListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: String?) {
        if (item == null) return
        with(item) {
            if (item.contains("[ERROR]")) {
                holder.binding.tvFileType.text = item
                holder.binding.tvFileType.setTextColor(context.resources.getColor(R.color.red))
            } else {
                holder.binding.tvFileType.text = item
                holder.binding.tvFileType.setTextColor(context.resources.getColor(R.color.gray_333333))
            }
        }

    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

}