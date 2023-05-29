package com.yuanquan.common.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * 单例通知类
 * 适配Android O
 */
public class NotificationUtils {

    private Context context;
    private NotificationManager notificationManager;
    private int NOTIFICATION_ID = 0;

    private static class NotificationUtilsHolder {
        public static final NotificationUtils notificationUtils = new NotificationUtils();
    }

    private NotificationUtils() {
    }

    public static NotificationUtils getInstance() {
        return NotificationUtilsHolder.notificationUtils;
    }

    /**
     * 初始化
     *
     * @param context 引用全局上下文
     */
    public void init(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * 创建通知通道
     *
     * @param channelId   通道id
     * @param channelName 通道名称
     * @param importance  通道级别
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotificationChannel(String channelId, String channelName, int importance) {

        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription(channelName);
        //LED灯
        channel.enableLights(true);
        channel.setLightColor(Color.RED);
        //震动
        //        channel.vibrationPattern = longArrayOf(0,1000,500,1000)
        //        channel.enableVibration(true)
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * 创建通知
     *
     * @param channelId 通道id
     * @param title     标题
     * @param content   内容
     *                  //     * @param intent    意图
     */
    public Notification createNotification(String channelId, String title, String content, int icon) {

//        PendingIntent pendingIntent = null;
//        if (intent != null) {
//             pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
////            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);
//        }

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(icon)
                //                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
//                .setContentIntent(pendingIntent)
                //        .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .build();
        return notification;
    }

    /**
     * 发送通知
     */
    public void showNotification(int id, Notification notification) {
        NOTIFICATION_ID = id;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void hideNotification() {
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    /**
     * 判断通知是否开启（非单个消息渠道）
     *
     * @param context 上下文
     * @return true 开启
     * API19 以上可用
     */
    public static boolean checkNotificationsEnabled(Context context) {
        try {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return notificationManagerCompat.getImportance() != NotificationManager.IMPORTANCE_NONE;
            }
            return notificationManagerCompat.areNotificationsEnabled();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断通知渠道是否开启（单个消息渠道）
     *
     * @param context   上下文
     * @param channelID 渠道 id
     * @return true 开启
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean checkNotificationsChannelEnabled(Context context, String channelID) {
        try {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) {
                return false;
            }
            NotificationChannel channel = manager.getNotificationChannel(channelID);
            return !(channel.getImportance() == NotificationManager.IMPORTANCE_NONE);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void openPermissionSetting(Activity context) {
        openPermissionSetting(context, 0);
    }

    public static void openPermissionSetting(Activity context, int requestCode) {
        try {
            Intent localIntent = new Intent();
            if (requestCode == 0)
                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //直接跳转到应用通知设置的代码：
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                localIntent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                localIntent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                if (requestCode > 0) {
                    context.startActivityForResult(localIntent, requestCode);
                } else {
                    context.startActivity(localIntent);
                }

                return;
            }

            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" cxx   pushPermission 有问题");
        }
    }
}
//        MyApplication中初始化并创建通道
//
//        // 初始化通知类
//        NotificationUtils notificationUtils = NotificationUtils.getInstance();
//        notificationUtils.init(getApplicationContext());
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        // 8.0以上创建通道
//        // 当然这里是具体按照项目需要设定的通道类型创建
//        notificationUtils.createNotificationChannel("chat", "聊天", NotificationManager.IMPORTANCE_HIGH);
//        }
//        在需要发送通知的地方调用
//
//        NotificationUtils notificationUtils = NotificationUtils.getInstance();
//        // 设置Intent相关数据
//        Intent intent = new Intent(this, MainActivity.class);
//        notificationUtils.sendNotification("chat","无线电通知", "长江长江，我是黄河", intent);
//
//
//        // 判断通知是否开启
//        boolean isOpen = NotificationUtils.checkNotificationsEnabled(context);
//
//        // 判断某个通知渠道是否开启
//        boolean isOpen = NotificationUtils.checkNotificationsChannelEnabled(context, "chat");
//
//        // 跳转设置开启
//        if(!isOpen) {
//
//        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
//        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
//        intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel.getId());
//        startActivity(intent);
//
//        }