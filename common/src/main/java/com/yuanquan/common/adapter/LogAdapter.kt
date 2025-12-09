package com.yuanquan.common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.yuanquan.common.databinding.LayoutLogItemBinding
import java.io.File

class LogAdapter : BaseQuickAdapter<File, LogAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: LayoutLogItemBinding = LayoutLogItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: File?) {
        if (item == null) return
        with(item) {
            holder.binding.tvFileType.text = item.name
        }

    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

}