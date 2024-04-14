package com.yuanquan.common.interfaces

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.annotation.RequiresPermission
import com.yuanquan.common.interfaces.NetworkChangeReceiver.NetStateChangeObserver

class WifiConnectionReceiver : BroadcastReceiver() {
    private val mObservers = mutableListOf<WifiConnectionListener>()
    private var mType = false

    @RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (WifiManager.WIFI_STATE_CHANGED_ACTION == action) {
            // Wi-Fi状态变化
            val wifiState =
                intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
            when (wifiState) {
                WifiManager.WIFI_STATE_ENABLED -> {
                    // Wi-Fi已启用
                    notifyEnableObservers(true)
                }

                WifiManager.WIFI_STATE_DISABLED -> {
                    // Wi-Fi已禁用
                    notifyEnableObservers(false)
                }
            }
        } else if (ConnectivityManager.CONNECTIVITY_ACTION == action) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo

            if (networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                // Wi-Fi连接状态变化
                var connected = networkInfo.isConnected
                notifyConnectObservers(connected)
            } else {
                notifyConnectObservers(false)
            }
        }
    }

    fun registerReceiver(context: Context) {
        val filter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }
        context.registerReceiver(this, filter)
    }

    fun unRegisterReceiver(context: Context) {
        context.unregisterReceiver(this)
    }

    fun registerObserver(observer: WifiConnectionListener) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer)
        }
    }

    fun unRegisterObserver(observer: WifiConnectionListener) {
        mObservers.remove(observer)
    }

    private fun notifyEnableObservers(enableType: Boolean) {
        if (enableType) {
            for (observer in mObservers) {
                observer.onWifiEnabled()
            }
        } else {
            for (observer in mObservers) {
                observer.onWifiDisabled()
            }
        }
    }

    private fun notifyConnectObservers(networkType: Boolean) {
        if (mType == networkType) {
            return
        }
        mType = networkType
        if (mType) {
            for (observer in mObservers) {
                observer.onWifiConnected()
            }
        } else {
            for (observer in mObservers) {
                observer.onWifiDisconnected()
            }
        }
    }

    interface WifiConnectionListener {
        fun onWifiEnabled()
        fun onWifiDisabled()
        fun onWifiConnected()
        fun onWifiDisconnected()
    }
}