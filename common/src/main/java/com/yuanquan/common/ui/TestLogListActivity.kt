package com.yuanquan.common.ui

import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.LoadState.NotLoading
import com.chad.library.adapter4.loadState.leading.LeadingLoadStateAdapter.OnLeadingListener
import com.yuanquan.common.adapter.LogListAdapter
import com.yuanquan.common.databinding.ActivityTestLogListBinding
import com.yuanquan.common.ui.base.BaseActivity
import com.yuanquan.common.ui.base.BaseViewModel
import com.yuanquan.common.utils.AlertDialogUtils
import com.yuanquan.common.utils.ClickUtils.onClick
import com.yuanquan.common.utils.FileLogUtils
import com.yuanquan.common.utils.FileLogUtils.PagedReadResult
import com.yuanquan.common.utils.ToastUtils
import java.io.File


class TestLogListActivity :
    BaseActivity<BaseViewModel<ActivityTestLogListBinding>, ActivityTestLogListBinding>() {
    var adapter = LogListAdapter()
    private lateinit var helper: QuickAdapterHelper

    private val bottomDataPosition: Int
        get() = adapter.items.size - 1
    private var page = 0
    var receivedFile: File? = null

    override fun initView() {
        vb.rv.setLayoutManager(LinearLayoutManager(mContext))
        helper = QuickAdapterHelper.Builder(adapter)
            .setLeadingLoadStateAdapter(object : OnLeadingListener {
                override fun onLoad() {
                    requestUoFetch()
                }

                override fun isAllowLoading(): Boolean {
                    return true
                }
            })
//            .setLeadPreloadSize(0) // 预加载（默认值为0）
            .build()
        vb.rv.adapter = helper.adapter
        adapter.setOnItemClickListener { adapter, view, position ->
            var intent = Intent(this, TestLogDetailActivity::class.java)
            intent.putExtra("data", adapter.items[position]);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun initData() {
        val filePath = intent.getStringExtra("file_path")
        if (filePath != null) {
            receivedFile = File(filePath)
        }
        requestUoFetch()
    }

    override fun initClick() {
        vb.tvRight.onClick {
            if (receivedFile != null) {
                AlertDialogUtils.show(
                    mContext,
                    "",
                    "delete?",
                    "Cancel",
                    "Confirm",
                    null,
                    null
                ) {
                    AlertDialogUtils.dismiss()
                    var deleteLogFile = FileLogUtils.clearFileContents(receivedFile!!)
                    if (deleteLogFile) {
                        adapter.submitList(arrayListOf())
                        adapter.notifyDataSetChanged()
                        ToastUtils.show(mContext,"Delete successful")
                    } else {
                        ToastUtils.show(mContext,"Delete failed")
                    }
                }
            }
        }
    }

    private fun requestUoFetch() {
        if (page == 0) {
            page++
            // 首次进入页面，设置数据
            val logData: PagedReadResult =
                FileLogUtils.readFromBottomWithPagination(
                    receivedFile,
                    page = page,
                    pageSize = 1000
                )
            vb.titleToolbar.text = "${logData.currentPage}/${logData.totalPages}"
            adapter.submitList(logData.lines)
            scrollToBottom()
            helper.leadingLoadState = NotLoading(false)
            return
        }
        page++

        /**
         * When starting to request data from the network, set the status to loading.
         * 当开始网络请求数据的时候，设置状态为加载中
         */
        helper.leadingLoadState = LoadState.Loading

        /*
         * get data from internet.
         * 从网络获取数据
         */
        val logData: PagedReadResult =
            FileLogUtils.readFromBottomWithPagination(receivedFile, page = page, pageSize = 1000)
//        LogUtil.e("当前页：" + logData.currentPage)
//        LogUtil.e("总页：" + logData.totalPages)
        vb.titleToolbar.text = "${logData.currentPage}/${logData.totalPages}"
        if (logData.hasNext) {
            adapter.addAll(0, logData.lines)
            helper.leadingLoadState = NotLoading(false)
        } else {
            helper.leadingLoadState = NotLoading(true)
        }

    }

    /**
     * 滚动到底部（不带动画）
     */
    private fun scrollToBottom() {
        val ll = vb.rv.layoutManager as LinearLayoutManager
        ll.scrollToPositionWithOffset(bottomDataPosition, 0)
//        vb.rv.smoothScrollToPosition(adapter.itemCount - 1)
    }
}