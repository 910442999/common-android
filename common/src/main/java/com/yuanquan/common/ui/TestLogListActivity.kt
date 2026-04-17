package com.yuanquan.common.ui

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
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
import com.yuanquan.common.utils.SPUtils
import com.yuanquan.common.utils.ToastUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


class TestLogListActivity :
    BaseActivity<BaseViewModel<ActivityTestLogListBinding>, ActivityTestLogListBinding>() {
    companion object {
        private const val PAGE_SIZE = 1000
        private const val FILTER_KEY_PREFIX = "test_log_filter_keyword_"
    }

    var adapter = LogListAdapter()
    private lateinit var helper: QuickAdapterHelper
    private val loadedLines = mutableListOf<String>()

    private val bottomDataPosition: Int
        get() = adapter.items.size - 1
    private var page = 0
    var receivedFile: File? = null
    private var currentKeyword = ""

    override fun initView() {
        vb.rv.setLayoutManager(LinearLayoutManager(mContext))
        helper = QuickAdapterHelper.Builder(adapter)
            .setLeadingLoadStateAdapter(object : OnLeadingListener {
                override fun onLoad() {
                    if (currentKeyword.isBlank()) {
                        requestUoFetch()
                    } else {
                        helper.leadingLoadState = NotLoading(true)
                    }
                }

                override fun isAllowLoading(): Boolean {
                    return currentKeyword.isBlank()
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
        vb.etFilterKeyword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                updateClearKeywordView()
            }
        })
    }

    override fun initData() {
        val filePath = intent.getStringExtra("file_path")
        if (filePath != null) {
            receivedFile = File(filePath)
        }
        restoreKeyword()
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
                        loadedLines.clear()
                        adapter.submitList(emptyList())
                        helper.leadingLoadState = NotLoading(true)
                        updateTitle()
                        ToastUtils.show(mContext,"Delete successful")
                    } else {
                        ToastUtils.show(mContext,"Delete failed")
                    }
                }
            }
        }
        vb.tvClearKeyword.onClick {
            clearKeyword()
        }
        vb.tvApplyFilter.onClick {
            applyKeyword(vb.etFilterKeyword.text?.toString()?.trim().orEmpty())
        }
    }

    private fun requestUoFetch() {
        if (currentKeyword.isNotBlank()) {
            filterLogs(currentKeyword)
            return
        }
        if (page == 0) {
            page++
            // 首次进入页面，设置数据
            val logData: PagedReadResult =
                FileLogUtils.readFromBottomWithPagination(
                    receivedFile,
                    page = page,
                    pageSize = PAGE_SIZE
                )
            loadedLines.clear()
            loadedLines.addAll(logData.lines)
            adapter.submitList(logData.lines)
            updateTitle(logData)
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
            FileLogUtils.readFromBottomWithPagination(receivedFile, page = page, pageSize = PAGE_SIZE)
//        LogUtil.e("当前页：" + logData.currentPage)
//        LogUtil.e("总页：" + logData.totalPages)
        updateTitle(logData)
        if (logData.hasNext) {
            loadedLines.addAll(0, logData.lines)
            adapter.addAll(0, logData.lines)
            helper.leadingLoadState = NotLoading(false)
        } else {
            loadedLines.addAll(0, logData.lines)
            if (logData.lines.isNotEmpty()) {
                adapter.addAll(0, logData.lines)
            }
            helper.leadingLoadState = NotLoading(true)
        }

    }

    private fun applyKeyword(keyword: String) {
        currentKeyword = keyword
        SPUtils.getInstance().put(getFilterSpKey(), keyword)
        updateClearKeywordView()
        if (keyword.isBlank()) {
            reloadPagedLogs()
        } else {
            filterLogs(keyword)
        }
    }

    private fun restoreKeyword() {
        val savedKeyword = SPUtils.getInstance().getString(getFilterSpKey(), "") ?: ""
        currentKeyword = savedKeyword.trim()
        vb.etFilterKeyword.setText(currentKeyword)
        vb.etFilterKeyword.setSelection(vb.etFilterKeyword.text?.length ?: 0)
        updateClearKeywordView()
        if (currentKeyword.isBlank()) {
            reloadPagedLogs()
        } else {
            filterLogs(currentKeyword)
        }
    }

    private fun clearKeyword() {
        vb.etFilterKeyword.setText("")
        SPUtils.getInstance().remove(getFilterSpKey())
        currentKeyword = ""
        updateClearKeywordView()
        reloadPagedLogs()
    }

    private fun reloadPagedLogs() {
        page = 0
        loadedLines.clear()
        adapter.submitList(emptyList())
        helper.leadingLoadState = NotLoading(false)
        requestUoFetch()
    }

    private fun filterLogs(keyword: String) {
        val filteredLines = readAllLines(receivedFile).filter { it.contains(keyword, ignoreCase = true) }
        loadedLines.clear()
        loadedLines.addAll(filteredLines)
        adapter.submitList(filteredLines)
        helper.leadingLoadState = NotLoading(true)
        updateTitle(filteredCount = filteredLines.size)
        if (filteredLines.isNotEmpty()) {
            scrollToBottom()
        }
    }

    private fun readAllLines(file: File?): List<String> {
        if (file == null || !file.exists()) {
            return emptyList()
        }
        val lines = mutableListOf<String>()
        try {
            FileInputStream(file).use { fis ->
                InputStreamReader(fis, StandardCharsets.UTF_8).use { isr ->
                    BufferedReader(isr).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            line?.let(lines::add)
                        }
                    }
                }
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return lines
    }

    private fun updateTitle(logData: PagedReadResult? = null, filteredCount: Int? = null) {
        vb.titleToolbar.text = when {
            currentKeyword.isNotBlank() -> "筛选结果 ${filteredCount ?: adapter.items.size} 条"
            logData != null -> "${logData.currentPage}/${logData.totalPages}"
            else -> ""
        }
    }

    private fun updateClearKeywordView() {
        val inputKeyword = vb.etFilterKeyword.text?.toString()?.trim().orEmpty()
        vb.tvClearKeyword.visibility =
            if (inputKeyword.isBlank()) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun getFilterSpKey(): String {
        val filePath = receivedFile?.absolutePath ?: "default"
        return FILTER_KEY_PREFIX + filePath.hashCode()
    }

    /**
     * 滚动到底部（不带动画）
     */
    private fun scrollToBottom() {
        if (bottomDataPosition < 0) {
            return
        }
        val ll = vb.rv.layoutManager as LinearLayoutManager
        ll.scrollToPositionWithOffset(bottomDataPosition, 0)
//        vb.rv.smoothScrollToPosition(adapter.itemCount - 1)
    }
}
