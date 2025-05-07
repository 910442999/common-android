package com.yuanquan.common.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.yuanquan.common.R;
import com.yuanquan.common.api.HttpUtil;
import com.yuanquan.common.interfaces.OnDownloadListener;
import com.yuanquan.common.model.DownloadInfo;
import com.yuanquan.common.utils.FileUtils;
import com.yuanquan.common.utils.LogUtil;
import com.yuanquan.common.utils.SysUtils;
import com.yuanquan.common.utils.ToastUtils;
import com.yuanquan.common.widget.ProgressView;

import java.io.File;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * 绿色样式并且在当前展示进度的样式
 */
public class UpdateActivity extends AppCompatActivity {
    private ProgressView progressView;
    private View llProgress, llDownload;
    private TextView tvMsg, tvBtn2, tvTitle;
    private String appUpdate; //app当前版本是否需要更新
    private DownloadInfo downloadInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_download_dialog);
        downloadInfo = getIntent().getParcelableExtra("info");
        findView();
        tvTitle.setText("发现更新版本");
        tvBtn2.setText("立即升级");
        setDataAndListener();
        downloadAppData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        downloadInfo = getIntent().getParcelableExtra("info");
    }

    private void setDataAndListener() {
        tvMsg.setText(Html.fromHtml(downloadInfo.getUpdateLog()));
        tvMsg.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //右边的按钮
                progressView.setProgress(0);
                llProgress.setVisibility(View.VISIBLE);
                llDownload.setVisibility(View.INVISIBLE);
                downloadAppData();
            }
        });
    }

    private void downloadAppData() {
//        File outputFile = new File(FileUtils.getExternalFilesDir(this,Environment.DIRECTORY_DOWNLOADS), "app-download.apk");
        File outputFile = new File(FileUtils.getExternalCacheDir(this), "app-download.apk");
        HttpUtil.getInstance().downloadFile(downloadInfo.getApkUrl(), outputFile, new OnDownloadListener() {
            @Override
            public void onStart() {
                runOnUiThread(() -> {
                    llProgress.setVisibility(View.VISIBLE);
                    llDownload.setVisibility(View.INVISIBLE);
                });
            }

            @Override
            public void onProgress(int progress, long bytesWritten, long totalBytes) {
                // 计算进度百分比，使用浮点避免整数溢出
                LogUtil.e(progress);
                runOnUiThread(() -> {
                    progressView.setProgress(progress);
                });
            }

            @Override
            public void onComplete(@NonNull File file) {
                runOnUiThread(() -> {
                    llProgress.setVisibility(View.INVISIBLE);
                    llDownload.setVisibility(View.VISIBLE);
                    SysUtils.installApkFile(getBaseContext(), file);
                });
            }

            @Override
            public void onCancel() {
                runOnUiThread(() -> {
                    llProgress.setVisibility(View.INVISIBLE);
                    llDownload.setVisibility(View.VISIBLE);
                    ToastUtils.show(getBaseContext(), "取消下载");
                });
            }

            @Override
            public void onError(@NonNull Throwable e) {
                runOnUiThread(() -> {
                    llProgress.setVisibility(View.INVISIBLE);
                    llDownload.setVisibility(View.VISIBLE);
                    if (e instanceof SocketTimeoutException) {
                        ToastUtils.show(getBaseContext(), "下载连接超时");
                    } else if (e instanceof UnknownHostException) {
                        ToastUtils.show(getBaseContext(), "网络异常，请检查网络状态后重试");
                    } else {
                        ToastUtils.show(getBaseContext(), "下载失败");
                    }
                });
            }
        });
    }

    private void findView() {
        progressView = (ProgressView) findViewById(R.id.progressView);
        tvMsg = (TextView) findViewById(R.id.tv_msg);
        tvBtn2 = (TextView) findViewById(R.id.tv_btn2);
        llProgress = findViewById(R.id.ll_progress);
        llDownload = findViewById(R.id.ll_download);
        tvTitle = findViewById(R.id.tv_title);
    }

    @Override
    protected void onDestroy() {
        HttpUtil.getInstance().cancelDownload();
        super.onDestroy();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }

}
