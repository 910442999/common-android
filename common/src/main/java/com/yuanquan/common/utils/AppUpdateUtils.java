package com.yuanquan.common.utils;

import android.app.Application;

import com.cretin.www.cretinautoupdatelibrary.interfaces.AppUpdateInfoListener;
import com.cretin.www.cretinautoupdatelibrary.model.DownloadInfo;
import com.cretin.www.cretinautoupdatelibrary.model.TypeConfig;
import com.cretin.www.cretinautoupdatelibrary.model.UpdateConfig;
import com.cretin.www.cretinautoupdatelibrary.utils.SSLUtils;
import com.yuanquan.common.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class AppUpdateUtils {
    /**
     * 全局初始化
     *
     * @param context
     */
    public static void init(Application context, Class customActivityClass) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30_000, TimeUnit.SECONDS).readTimeout(30_000, TimeUnit.SECONDS).writeTimeout(30_000, TimeUnit.SECONDS)
                //如果你需要信任所有的证书，可解决根证书不被信任导致无法下载的问题 start
                .sslSocketFactory(SSLUtils.createSSLSocketFactory()).hostnameVerifier(new SSLUtils.TrustAllHostnameVerifier())
                //如果你需要信任所有的证书，可解决根证书不被信任导致无法下载的问题 end
                .retryOnConnectionFailure(true);
        //更新库配置
        UpdateConfig updateConfig = new UpdateConfig().setDebug(BuildConfig.DEBUG)
                .setDataSourceType(TypeConfig.DATA_SOURCE_TYPE_MODEL)
                .setUiThemeType(TypeConfig.UI_THEME_CUSTOM)
                .setCustomActivityClass(customActivityClass)
                .setShowNotification(false)//配置更新的过程中是否在通知栏显示进度
                .setCustomDownloadConnectionCreator(new OkHttp3Connection.Creator(builder));
        com.cretin.www.cretinautoupdatelibrary.utils.AppUpdateUtils.init(context, updateConfig);
    }

    public static void checkUpdate(String apkUrl, int prodVersionCode, String prodVersionName, int forceUpdateFlag, String updateLog, AppUpdateInfoListener appUpdateInfoListener) {
        DownloadInfo info = new DownloadInfo().setApkUrl(apkUrl).setProdVersionCode(prodVersionCode).setProdVersionName(prodVersionName).setForceUpdateFlag(forceUpdateFlag).setUpdateLog(updateLog);
        com.cretin.www.cretinautoupdatelibrary.utils.AppUpdateUtils.getInstance().addAppUpdateInfoListener(appUpdateInfoListener).checkUpdate(info);
    }

    //清除缓存数据
    public static void clearAllData() {
        com.cretin.www.cretinautoupdatelibrary.utils.AppUpdateUtils.getInstance().clearAllData();
    }
}
