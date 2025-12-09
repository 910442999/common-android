package com.yuanquan.common.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.yuanquan.common.adapter.LogAdapter
import com.yuanquan.common.api.URLConstant
import com.yuanquan.common.databinding.ActivityTestLogBinding
import com.yuanquan.common.ui.base.BaseActivity
import com.yuanquan.common.ui.base.BaseViewModel
import com.yuanquan.common.ui.webview.WebViewActivity
import com.yuanquan.common.utils.AlertDialogUtils
import com.yuanquan.common.utils.ClickUtils.onClick
import com.yuanquan.common.utils.FileLogUtils
import com.yuanquan.common.utils.ToastUtils
import java.io.File


class TestLogActivity :
    BaseActivity<BaseViewModel<ActivityTestLogBinding>, ActivityTestLogBinding>() {
    var list = arrayListOf<File>()
    var adapter = LogAdapter()
//    var handler: Handler = object : Handler(Looper.getMainLooper()) {
//        override fun handleMessage(msg: Message) {
//            super.handleMessage(msg)
//            Toast.makeText(mContext, "操作完成", Toast.LENGTH_LONG).show();
//        }
//    }

    override fun initView() {
        vb.tvRight.onClick {
            FileLogUtils.deleteAllLogFiles()
            val logFiles = FileLogUtils.getLogFiles()
            list.clear()
            logFiles.forEach { file ->
                list.add(file)
//            println("文件名: ${file.name}, 大小: ${file.length()}字节, 修改时间: ${Date(file.lastModified())}")
            }
            adapter.submitList(list)
        }
//        vb.text7.onClick {
//            startActivity(
//                Intent(mContext, WebViewActivity::class.java).putExtra(
//                    "url", URLConstant.getBoxHost()
//                ).putExtra("showRefresh", true).putExtra(
//                    "title", LanguageUtils.optString("盒子设置")
//                )
//            )
//        }
    }

    override fun initData() {

        vb.rv.setLayoutManager(LinearLayoutManager(mContext))
        vb.rv.setAdapter(adapter)
        adapter.setOnItemClickListener { adapter, view, position ->
            var intent = Intent(this, TestLogListActivity::class.java)
            intent.putExtra("file_path", list[position].absolutePath);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        adapter.setOnItemLongClickListener { adapter, view, position ->
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
                var deleteLogFile = FileLogUtils.deleteLogFile(list[position])
                if (deleteLogFile) {
                    list.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    ToastUtils.show(mContext,"Delete successful")
                } else {
                    ToastUtils.show(mContext,"Delete failed")
                }
            }
            return@setOnItemLongClickListener true
        }
        list.clear()
        val logFiles = FileLogUtils.getLogFiles()
        logFiles.forEach { file ->
            list.add(file)
//            println("文件名: ${file.name}, 大小: ${file.length()}字节, 修改时间: ${Date(file.lastModified())}")
        }
        adapter.submitList(list)
    }

    val REQUEST_EXTERNAL_STORAGE: Int = 1
    var PERMISSIONS_STORAGE: Array<String> = arrayOf<String>(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    fun verifyStoragePermissions(activity: Activity?) {
        // 检查是否已经获得了权限
        val permission = ActivityCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 没有权限，去请求权限
            ActivityCompat.requestPermissions(
                activity!!,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }
}