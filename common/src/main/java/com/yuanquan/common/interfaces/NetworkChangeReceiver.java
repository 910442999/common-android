//package com.yuanquan.common.interfaces;
//
//import android.annotation.SuppressLint;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.net.ConnectivityManager;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.yuanquan.common.utils.NetworkUtils;
//
//public class NetworkChangeReceiver extends BroadcastReceiver {
//
//    private List<NetStateChangeObserver> mObservers = new ArrayList<>();
//    private boolean mType = false;
//    private static boolean isRegister = false;
//
//    private static class InstanceHolder {
//        private static final NetworkChangeReceiver INSTANCE = new NetworkChangeReceiver();
//    }
//
//    @Override
//    @SuppressLint("MissingPermission")
//    public void onReceive(Context context, Intent intent) {
//        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
//            boolean connectivityStatus = NetworkUtils.isConnected(context);
//            notifyObservers(connectivityStatus);
//        }
//    }
//
//    public static void registerReceiver(Context context) {
//        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        context.registerReceiver(InstanceHolder.INSTANCE, intentFilter);
//        isRegister = true;
//    }
//
//    public static void unRegisterReceiver(Context context) {
//        if (isRegister) {
//            context.unregisterReceiver(InstanceHolder.INSTANCE);
//        }
//    }
//
//    public static void registerObserver(NetStateChangeObserver observer) {
//        if (observer == null) {
//            return;
//        }
//        if (!InstanceHolder.INSTANCE.mObservers.contains(observer)) {
//            InstanceHolder.INSTANCE.mObservers.add(observer);
//        }
//    }
//
//    public static void unRegisterObserver(NetStateChangeObserver observer) {
//        if (observer == null) {
//            return;
//        }
//        if (InstanceHolder.INSTANCE.mObservers == null) {
//            return;
//        }
//        InstanceHolder.INSTANCE.mObservers.remove(observer);
//    }
//
//    private void notifyObservers(boolean networkType) {
//        if (mType == networkType) {
//            return;
//        }
//        mType = networkType;
//        if (mType) {
//            for (NetStateChangeObserver observer : mObservers) {
//                observer.onConnect();
//            }
//        } else {
//            for (NetStateChangeObserver observer : mObservers) {
//                observer.onDisconnect();
//            }
//        }
//    }
//
//    public interface NetStateChangeObserver {
//        void onConnect();
//
//        void onDisconnect();
//    }
//}