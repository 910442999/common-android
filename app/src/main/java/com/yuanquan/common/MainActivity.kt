package com.yuanquan.common

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.NetworkSpecifier
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.os.PatternMatcher
import android.util.Log
import android.widget.TextView
import com.yuanquan.common.utils.NetworkUtils
import com.yuanquan.common.utils.permissions.PermissionResultCallback
import com.yuanquan.common.utils.permissions.PermissionUtils


class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var tv_text1 = findViewById<TextView>(R.id.tv_text1)
        tv_text1.setOnClickListener {
            PermissionUtils.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                object : PermissionResultCallback {
                    override fun onGranted() {
//                        addNetwork()
                        var wifiName = "CMCC-dTMg-5G"
                        var wifPassword = "ZDAWEIqaz"
                        NetworkUtils.connectWifi(this@MainActivity,wifiName,wifPassword)
                    }

                    override fun onDenied() {

                    }

                })
        }

    }

    @SuppressLint("MissingPermission")

    fun addNetwork() {
//        val wifiAdmin = WifiAdmin(this)
//        wifiAdmin.openWifi()
//        wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo("", "", 3))
        var wifiName = "CMCC-dTMg-5G"
        var wifPassword = "ZDAWEIqaz"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val specifier: NetworkSpecifier = WifiNetworkSpecifier.Builder()
                .setSsidPattern(PatternMatcher(wifiName, PatternMatcher.PATTERN_PREFIX))
                .setWpa2Passphrase(wifPassword)
                .build()
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifier)
                .build()
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCallback: NetworkCallback = object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.e("TAG", "onAvailable: ")
                    // do success processing here..
//                    when (upOrDown) {
//                        "up" -> {
//                            intoActivityWithBundle(ActivityHolder.SELECT_PERSONNEL, Bundle().apply {
//                                putString("type", "upload")
//                                putString("meetingId", meetingId)
//                            })
//                        }
//
//                        "down" -> {
//                            intoActivityWithBundle(ActivityHolder.SELECT_PERSONNEL, Bundle().apply {
//                                putString("type", "download")
//                                putString("meetingId", meetingId)
//                            })
//                        }
//                    }
                }

                override fun onUnavailable() {
                    Log.e("TAG", "onUnavailable: ")
                    // do failure processing here..
                }
            }
            connectivityManager.requestNetwork(request, networkCallback)
            // Release the request when done.
            // connectivityManager.unregisterNetworkCallback(networkCallback);
        } else {//小于Android Q的版本
            val wifiSSID = "\"" + wifiName + "\""
            val wifiConfiguration = WifiConfiguration().apply {
                SSID = wifiSSID
                preSharedKey = "\"" + wifPassword + "\""
                hiddenSSID = true
                allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                status = WifiConfiguration.Status.ENABLED
//                wepKeys[0] = "\"" +psd+ "\""
//                wepTxKeyIndex = 0
//                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
//                allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            }
            val wifiManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getSystemService(WifiManager::class.java)
            } else {
                applicationContext.getSystemService(Context.WIFI_SERVICE)
                    ?.let {
                        it as WifiManager
                    }

            }
            wifiManager?.run {
                addNetwork(wifiConfiguration)
                val config = configuredNetworks.first {
                    it.SSID != null && it.SSID == wifiSSID
                }
                disconnect()
                Log.e("===", "==networkId==${config.networkId}")
                val enableNetwork = enableNetwork(config.networkId, true)
                Log.e("===", "==enableNetwork==$enableNetwork")
                val b = reconnect()
                Log.e("==", "==reconnect=====$b")
            }

        }

    }
}