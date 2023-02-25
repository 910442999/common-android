package com.yuanquan.common.ui.common;

import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cretin.www.cretinautoupdatelibrary.interfaces.AppDownloadListener;
import com.cretin.www.cretinautoupdatelibrary.interfaces.AppUpdateInfoListener;
import com.cretin.www.cretinautoupdatelibrary.model.DownloadInfo;
import com.cretin.www.cretinautoupdatelibrary.utils.LogUtils;
import com.cretin.www.cretinautoupdatelibrary.utils.RootActivity;
import com.cretin.www.cretinautoupdatelibrary.view.ProgressView;
import com.yuanquan.common.LanguageUtils;
import com.yuanquan.common.R;
import com.yuanquan.common.utils.SPUtils;
import com.yuanquan.common.utils.ToastUtils;

/**
 * 绿色样式并且在当前展示进度的样式
 */
public class UpdateActivity extends RootActivity {

    //view
    private ImageView ivClose;
    private ProgressView progressView;
    private LinearLayout llProgress, llDownload;
    private TextView tvMsg, tvBtn1, tvBtn2, tvVersion, tvTitle, tvText1;
    private CheckBox cbCheck;
    private String appUpdate = "appUpdate"; //app当前版本是否需要更新

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_download_dialog);

        findView();
        tvTitle.setText(LanguageUtils.optString("发现更新版本"));
        tvBtn2.setText(LanguageUtils.optString("立即升级"));
        tvBtn1.setText(LanguageUtils.optString("稍后再说"));
        tvText1.setText(LanguageUtils.optString("本次更新不再提醒"));
        appUpdate = appUpdate + downloadInfo.getProdVersionName();
        if (SPUtils.getInstance().getBoolean(appUpdate)) {
            cbCheck.setChecked(true);
        } else {
            cbCheck.setChecked(false);
        }
        setDataAndListener();
    }

    private void setDataAndListener() {
        tvMsg.setText(downloadInfo.getUpdateLog());
        tvMsg.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvVersion.setText("v" + downloadInfo.getProdVersionName());

        if (downloadInfo.isForceUpdateFlag()) {
            tvBtn1.setVisibility(View.GONE);
            ivClose.setVisibility(View.GONE);
//            tvBtn2.setBackground(ResUtils.getDrawable(R.drawable.dialog_item_bg_selector_white_left_right_bottom));
        } else {
            tvBtn1.setVisibility(View.VISIBLE);
        }

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTask();
                if (cbCheck.isChecked()) {
                    SPUtils.getInstance().put(appUpdate, true);
                } else {
                    SPUtils.getInstance().put(appUpdate, false);
                }
                finish();
            }
        });

        tvBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //左边的按钮
                cancelTask();
                if (cbCheck.isChecked()) {
                    SPUtils.getInstance().put(appUpdate, true);
                } else {
                    SPUtils.getInstance().put(appUpdate, false);
                }
                finish();
            }
        });

        tvBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //右边的按钮
                download();
            }
        });
    }

    @Override
    public AppUpdateInfoListener obtainAppUpdateInfoListener() {
        return new AppUpdateInfoListener() {
            @Override
            public void isLatestVersion(boolean isLatest) {
//                ToastUtils.show(,LanguageUtils.optString("本次更新不再提醒"));
            }
        };
    }

    @Override
    public AppDownloadListener obtainDownloadListener() {
        return new AppDownloadListener() {
            @Override
            public void downloading(int progress) {
                progressView.setProgress(progress);
//                tvBtn2.setText(ResUtils.getString(R.string.downloading));
            }

            @Override
            public void downloadFail(String msg) {
                llProgress.setVisibility(View.GONE);
                llDownload.setVisibility(View.VISIBLE);
//                tvMsg.setVisibility(View.VISIBLE);
//                tvBtn2.setText(ResUtils.getString(R.string.btn_update_now));
//                Toast.makeText(UpdateActivity.this, ResUtils.getString(R.string.apk_file_download_fail), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void downloadComplete(String path) {
                llProgress.setVisibility(View.GONE);
                llDownload.setVisibility(View.VISIBLE);
//                tvMsg.setVisibility(View.VISIBLE);
//                tvBtn2.setText(ResUtils.getString(R.string.btn_update_now));
            }

            @Override
            public void downloadStart() {
                llProgress.setVisibility(View.VISIBLE);
                llDownload.setVisibility(View.GONE);
//                tvMsg.setVisibility(View.GONE);
//                tvBtn2.setText(ResUtils.getString(R.string.downloading));
            }

            @Override
            public void reDownload() {
                LogUtils.log("下载失败后点击重试");
            }

            @Override
            public void pause() {

            }
        };
    }


    private void findView() {
        ivClose = (ImageView) findViewById(R.id.iv_close);
        progressView = (ProgressView) findViewById(R.id.progressView);
        tvMsg = (TextView) findViewById(R.id.tv_msg);
        tvBtn1 = (TextView) findViewById(R.id.tv_btn1);
        tvBtn2 = (TextView) findViewById(R.id.tv_btn2);
        llProgress = findViewById(R.id.ll_progress);
        llDownload = findViewById(R.id.ll_download);
        tvVersion = findViewById(R.id.tv_version);
        tvTitle = findViewById(R.id.tv_title);
        tvText1 = findViewById(R.id.tv_text1);
        cbCheck = findViewById(R.id.cb_check);
    }

    /**
     * 启动Activity
     *
     * @param context
     * @param info
     */
    public static void launch(Context context, DownloadInfo info) {
        launchActivity(context, info, UpdateActivity.class);
    }

}
