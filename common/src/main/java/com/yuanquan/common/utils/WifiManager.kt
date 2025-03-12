package com.yuanquan.common.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat


/**
 * @Description:����wifi�Ĺ�����,����onCreate��֮�󴴽�
 * @author zhangruiqian
 * @date 2023/4/25 10:26
 */
enum class WifiCapability {
    WIFI_CIPHER_WEP, WIFI_CIPHER_WPA, WIFI_CIPHER_NO_PASS
}

class WifiManager(
    private val context: Context
) {

    //���ӹ�����
    private var connectivityManager: ConnectivityManager =
        context.getSystemService(ComponentActivity.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var wifiManager: WifiManager =
        context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    /**
     * 判断模块是否开启
     */
    fun isEnabled(): Boolean {
        return wifiManager.isWifiEnabled
    }

    /**
     * 开启模块
     */
    fun enable(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.context.startActivity(Intent(Settings.Panel.ACTION_WIFI))
            return true
        }
        return wifiManager.setWifiEnabled(true)
    }

    /**
     * 关闭模块
     */
    fun disable(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.context.startActivity(Intent(Settings.Panel.ACTION_WIFI))
            return true
        }
        return wifiManager.setWifiEnabled(false)
    }

    /**
     * �Զ�����wifi
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE])
    fun connectWifi(ssid: String, password: String, callback: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectByNew(ssid, password, callback)
        } else {
            connectByOld(ssid, password, callback)
        }
    }


    /**
     * ��������levelֵɸѡ��wifi�źŷ���ScanResult����
     */

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE])
    fun getWifiListByLevel(): MutableList<ScanResult> {
        val list = mutableListOf<ScanResult>()

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "û��wifiȨ��")
            return list
        }
        for (scanResult in wifiManager.scanResults) {
            val indexOfElement = list.indexOfElement(scanResult)
            if (indexOfElement != -1) {
                val old = list[indexOfElement]
                val new = maxOf(old, scanResult) { o1, o2 -> o1.level - o2.level }
                list.removeAt(indexOfElement)
                list.add(indexOfElement, new)
            } else {
                list.add(scanResult)
            }
        }
        list.sortWith { o1, o2 -> o1.level - o2.level }
        return list
    }


    /*private*/

    private fun MutableList<ScanResult>.indexOfElement(scanResult: ScanResult): Int {
        this.forEachIndexed { index, element ->
            if (scanResult.SSID == element.SSID) {
                return index
            }
        }
        return -1
    }

    /**
     * ��׿10���������ӷ�ʽ
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun connectByNew(ssid: String, password: String, callback: (Boolean) -> Unit) {

        val wifiNetworkSpecifier =
            WifiNetworkSpecifier.Builder().setSsid(ssid).setWpa2Passphrase(password).build()
        //��������
        val request = NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
            .setNetworkSpecifier(wifiNetworkSpecifier).build()
        //����ص�����
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                bindProcessToNetwork(network)
                LogUtil.e("WifiManager：onAvailable")
                callback(true)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                LogUtil.e("WifiManager：onUnavailable")
                bindProcessToNetwork(null)
                callback(false)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                // 网络已断开
                // TODO: 处理网络连接变化
                LogUtil.e("WifiManager：onLost")
                bindProcessToNetwork(null)
            }

            override fun onCapabilitiesChanged(
                network: Network, networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                LogUtil.e("WifiManager：onCapabilitiesChanged")
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)
                LogUtil.e("WifiManager：onLinkPropertiesChanged")
            }

            override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
                super.onBlockedStatusChanged(network, blocked)
                LogUtil.e("WifiManager：onBlockedStatusChanged")
            }
        }
        if (networkCallback != null) {
            connectivityManager.requestNetwork(request, networkCallback!!)
        }
    }

    fun unregisterNetworkCallback() {
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback!!)
            LogUtil.e("解绑网络回调")
        }
    }

    fun bindProcessToNetwork(network: Network?) {
//        if (network != null) {
//            var processDefaultNetwork = connectivityManager.bindProcessToNetwork(network)
        var processDefaultNetwork = ConnectivityManager.setProcessDefaultNetwork(network)
        LogUtil.e("绑定进程到网络：" + (if (network == null) "解绑" else "绑定") + processDefaultNetwork)
//        }
    }

    /**
     * ��׿10�������ӷ�ʽ
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE])
    private fun connectByOld(
        ssid: String, password: String, callback: (Boolean) -> Unit
    ) {
        var isSuccess = false

        isExist(ssid).elif({
            isSuccess = wifiManager.enableNetwork(it.networkId, true)
        }, {
            val wifiConfiguration =
                createWifiConfig(ssid, password, WifiCapability.WIFI_CIPHER_WPA)
            val netId = wifiManager.addNetwork(wifiConfiguration)
            wifiManager.disconnect()
            isSuccess = wifiManager.enableNetwork(netId, true)
            wifiManager.reconnect()
        })
        callback(isSuccess)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE])
    private fun createWifiConfig(
        ssid: String, password: String, type: WifiCapability
    ): WifiConfiguration {
        val config = WifiConfiguration()
        config.apply {
            allowedAuthAlgorithms.clear()
            allowedGroupCiphers.clear()
            allowedKeyManagement.clear()
            allowedPairwiseCiphers.clear()
            allowedProtocols.clear()
            SSID = "\"$ssid\""
        }
        isExist(ssid).elif({
            wifiManager.removeNetwork(it.networkId)
            wifiManager.saveConfiguration()
        }, {})


        config.apply {
            //����Ҫ����ĳ���
            when (type) {
                WifiCapability.WIFI_CIPHER_NO_PASS -> {
                    allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                    //��WEP���ܵĳ���
                }

                WifiCapability.WIFI_CIPHER_WEP -> {
                    hiddenSSID = true
                    wepKeys[0] = "\"" + password + "\""
                    allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                    allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                    allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                    wepTxKeyIndex = 0
                    //��WPA���ܵĳ������Լ�����ʱ�������ȵ���WPA2����ʱ��ͬ��������������������
                }

                WifiCapability.WIFI_CIPHER_WPA -> {
                    preSharedKey = "\"" + password + "\""
                    hiddenSSID = true
                    allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                    allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                    allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                    allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                    allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                    allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                    status = WifiConfiguration.Status.ENABLED
                }
            }
        }
        return config
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE])
    private fun isExist(ssid: String): WifiConfiguration? {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        val existingConfigs = wifiManager.configuredNetworks
        existingConfigs.forEach {
            if (it.SSID == "\"$ssid\"") {
                return it
            }
        }
        return null
    }

    private fun getCipherType(capabilities: String): WifiCapability {
        return if (capabilities.contains("WEB")) {
            WifiCapability.WIFI_CIPHER_WEP
        } else if (capabilities.contains("PSK")) {
            WifiCapability.WIFI_CIPHER_WPA
        } else if (capabilities.contains("WPS")) {
            WifiCapability.WIFI_CIPHER_NO_PASS
        } else {
            WifiCapability.WIFI_CIPHER_NO_PASS
        }
    }

    /**
     * 获取网关地址
     *
     * @return 获取失败则返回空字符
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getGateway(): String {
        return NetworkUtils.getGateway(context)
    }

    private companion object {
        const val TAG = "WifiManager"
    }

    fun <T, R> T?.elif(block: (T) -> R, block2: () -> R): R {
        return if (this != null) {
            block(this)
        } else {
            block2()
        }
    }
}