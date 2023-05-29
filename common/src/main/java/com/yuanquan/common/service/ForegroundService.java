package com.yuanquan.common.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.yuanquan.common.utils.NotificationUtils;

/**
 * 前台服务
 * <p>
 * 使用方式
 * <p>
 * 在清单文件中 添加
 * <p>
 * 前台通知权限  配合 ForegroundService 使用
 * <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
 * <service
 * android:name=".service.ForegroundService"
 * android:enabled="true"
 * android:foregroundServiceType="microphone" />
 * <p>
 * 在代码中
 */
public class ForegroundService extends Service {
    private static final String TAG = ForegroundService.class.getSimpleName();
    /**
     * 标记服务是否启动
     */
//    private static boolean serviceIsLive = false;

    /**
     * 唯一前台通知ID
     * 前台服务会在状态栏显示一个通知，就算休眠也不会被杀，如果不想显示通知，只要把参数里的int设为0即可。
     */
    private static final int NOTIFICATION_ID = 1;
    private static String CHANNEL_ID = "channel_id";
    private static String CHANNEL_NAME = "channel_name";
    private static String CHANNEL_TITLE = "显示的标题";
    private static String CHANNEL_CONTENT = "显示的内容";
    private static int CHANNEL_ICON = 0;//应用图标

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        // 获取服务通知
        NotificationUtils notificationUtils = NotificationUtils.getInstance();
        notificationUtils.init(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 8.0以上创建通道
            // 当然这里是具体按照项目需要设定的通道类型创建
            notificationUtils.createNotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        }
        Notification notification = notificationUtils.createNotification(CHANNEL_ID, CHANNEL_TITLE, CHANNEL_CONTENT, CHANNEL_ICON);
        //将服务置于启动状态 ,NOTIFICATION_ID指的是创建的通知的ID
        startForeground(NOTIFICATION_ID, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        // 标记服务启动
//        serviceIsLive = true;
        // 数据获取
        //        String data = intent.getStringExtra("Foreground");
        //        Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        // 标记服务关闭
//        serviceIsLive = false;
        // 移除通知
        try {
            stopForeground(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * start foreground service
     *
     * @param context
     */
    public static void startForegroundService(Context context, String channelId, String channelName, String title, String content, int icon) {
        CHANNEL_ID = channelId;
        CHANNEL_NAME = channelName;
        CHANNEL_TITLE = title;
        CHANNEL_CONTENT = content;
        CHANNEL_ICON = icon;
        try {
//            if (!serviceIsLive) {
            Intent intent = new Intent(context, ForegroundService.class);
            Log.e(TAG, "startForegroundService");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
//            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "startForegroundService" + e.getMessage());
        }
    }

    /**
     * stop foreground service
     *
     * @param context
     */
    public static void stopService(Context context) {
        try {
//            if (serviceIsLive) {
            Intent intent = new Intent(context, ForegroundService.class);
            Log.e(TAG, "stopService");
            context.stopService(intent);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "stopService" + e.getMessage());
        }
    }
}