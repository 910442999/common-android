package com.yuanquan.common.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.net.wifi.SupplicantState
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.PatternMatcher
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException


/**
 * 操作wifi 需要 如下 权限
 *     <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
 *      <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
 *     <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 *     <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 *     <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 *     <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 */
object NetworkUtils {
    /**
     * 是否已经注册回调
     */
    private var isRegisted = false
    private var connectivityManager: ConnectivityManager? = null

    /**
     * 网络是否可用
     */
    private var isNetWorkAvailable = false

    /**
     * 网络类型
     */
    private var netType: NetType? = null
    private var networkCallbackImpl: NetworkCallbackImpl? = null

    @JvmStatic
    fun connectWifi(
        context: Context,
        ssid: String,
        password: String
    ): Boolean {
        // Android 10 及以上版本需要使用新的 API
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectToWifiApi29(context, ssid, password)
        } else {
            connectToWifiBelowApi29(context, ssid, password)
        }
    }

    /**
     * Android 10 及以上版本连接 WiFi
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun connectToWifiApi29(
        context: Context,
        ssid: String,
        password: String
    ): Boolean {
        val request = getNetworkRequest(ssid, password)

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val future = CompletableFuture<Boolean>()
        val networkCallback: ConnectivityManager.NetworkCallback =
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.e("TAG", "onAvailable: ")
                    // 返回连接结果
                    future.complete(true)
//                    connectivityManager.unregisterNetworkCallback(this)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    Log.e("TAG", "onUnavailable: ")
                    // 返回连接结果
                    future.complete(false)
                    // 取消注册网络回调
//                    connectivityManager.unregisterNetworkCallback(this)
                }
            }

        // 注册网络回调
        connectivityManager.requestNetwork(request, networkCallback)
        var b = try {
            // 等待连接结果
            future.get()
        } catch (e: InterruptedException) {
            e.printStackTrace()
            false
        } catch (e: ExecutionException) {
            e.printStackTrace()
            false
        }

        // 取消注册网络回调
        connectivityManager.unregisterNetworkCallback(networkCallback)
        return b
    }

    public fun getNetworkRequest(
        ssid: String,
        password: String
    ): NetworkRequest {
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()
        return request
    }

    /**
     * Android 10 以下版本连接 WiFi
     */
    private fun connectToWifiBelowApi29(
        context: Context,
        ssid: String,
        password: String
    ): Boolean {
        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = "\"$ssid\""
        wifiConfig.preSharedKey = "\"$password\""
//        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
//        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
//        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        // 添加 WiFi 配置
        val networkId = wifiManager.addNetwork(wifiConfig)
        if (networkId == -1) {
            return false
        }
//        wifiManager.disconnect()
        // 启用 WiFi 配置
        if (!wifiManager.enableNetwork(networkId, true)) {
            return false
        }
        // 重新连接 WiFi
        return wifiManager.reconnect()
    }

    @RequiresPermission(allOf = [Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE])
    fun removeWifi(context: Context, ssid: String, password: String) {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val suggestionsList: MutableList<WifiNetworkSuggestion> = ArrayList()
            val suggestion = WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build()
            suggestionsList.add(suggestion)
            // 删除网络
            val status = wifiManager.removeNetworkSuggestions(suggestionsList)
            if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
                LogUtil.e("Network suggestion removed successfully.");
            } else {
                LogUtil.e("Failed to remove network suggestion.");
            }
        } else {
            val configurations = wifiManager.configuredNetworks
            for (config in configurations) {
                if (config.SSID == "\"" + ssid + "\"") {
                    wifiManager.removeNetwork(config.networkId)
                    wifiManager.saveConfiguration()
                    break
                }
            }
        }
    }

    @JvmStatic
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isWifiEnabled(context: Context): Boolean {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }

    internal class NetworkCallbackImpl : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            isNetWorkAvailable = true
            LogUtil.e("onAvailable")
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)
            isNetWorkAvailable = false
            LogUtil.e("onLosing")
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            isNetWorkAvailable = false
            LogUtil.e("onLost")
        }

        override fun onUnavailable() {
            super.onUnavailable()
            isNetWorkAvailable = false
            LogUtil.e("onUnavailable")
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                isNetWorkAvailable = true
            }
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            ) {
                netType = NetType.WIFI
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {
                netType = NetType.BLUETOOTH
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                netType = NetType.CELLULAR
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                netType = NetType.ETHERNET
            }
            LogUtil.e("onCapabilitiesChanged")
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
            LogUtil.e("onLinkPropertiesChanged")
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            super.onBlockedStatusChanged(network, blocked)
            LogUtil.e("onBlockedStatusChanged")
        }
    }


    /**
     * 注册回调
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun registerCallback(context: Context) {
        if (!isRegisted) {
            connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                isRegisted = true
                networkCallbackImpl = NetworkCallbackImpl()
                connectivityManager!!.registerDefaultNetworkCallback(networkCallbackImpl!!)
            }
        }
    }

    /**
     * 取消注册
     */
    fun unRegisterCallback() {
        if (connectivityManager != null && isRegisted) {
            isRegisted = false
            connectivityManager!!.unregisterNetworkCallback(networkCallbackImpl!!)
        }
    }


    enum class NetType {
        /**
         * wifi
         */
        WIFI,
        BLUETOOTH,
        CELLULAR,
        ETHERNET
    }


    fun isNetWorkAvailable(): Boolean {
        return isNetWorkAvailable
    }

    fun getNetType(): NetType? {
        return netType
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isAvailable(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isNetWorkAvailable
        } else {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            var networkInfo: NetworkInfo? = null
            if (connectivityManager != null) {
                networkInfo = connectivityManager.activeNetworkInfo
            }
            networkInfo != null && networkInfo.isConnected
        }
    }

    /**
     * 获取当前WIFI名称
     *
     * @param context 上下文
     * @return 当前WIFI名称
     */
    @JvmStatic
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE])
    fun getCurrentSsid(context: Context): String {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        if (wifiManager != null && wifiManager.isWifiEnabled) {
//            val connectivityManager =
//                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
//            val networkCapabilities =
//                connectivityManager!!.getNetworkCapabilities(connectivityManager.activeNetwork)
//            if (networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo != null && wifiInfo.supplicantState == SupplicantState.COMPLETED) {
                var ssid = wifiInfo.ssid
                if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    // 如果当前 SSID 以引号开头和结尾，则移除它们
                    ssid = ssid.substring(1, ssid.length - 1)
                }
                if (!TextUtils.isEmpty(ssid) && "<unknown ssid>" != ssid) {
                    LogUtil.e("ssid=$ssid")
                    return ssid
                }
                //部分手机拿不到WiFi名称
                val networkId = wifiInfo.networkId
                LogUtil.e("networkId=$networkId")
                val configuredNetworks = wifiManager.configuredNetworks
                for (config in configuredNetworks) {
                    if (config.networkId == networkId) {
                        ssid = config.SSID
                        return ssid
                    }
                }
                //扫描到的网络
                val scanResults = wifiManager.scanResults
                for (scanResult in scanResults) {
                    ssid = scanResult.SSID
                    return ssid
//                }
                }
                if (!TextUtils.isEmpty(ssid) && "<unknown ssid>" != ssid) {
                    LogUtil.e("ssid=$ssid")
                    return ssid
                }
            }
        }
        return ""
    }

    /**
     * 是否5G网络
     *
     * @param ssid    wifi名称
     * @param context 上下文
     * @return 是否5G网络
     */
    @JvmStatic
    @SuppressLint("ObsoleteSdkInt")
    fun is5GHz(ssid: String, context: Context): Boolean {
        val wifiManger =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                ?: return false
        val wifiInfo = wifiManger.connectionInfo
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val freq = wifiInfo.frequency
            freq in 4901..5899
        } else {
            ssid.uppercase(Locale.getDefault()).endsWith("5G")
        }
    }

    @JvmStatic
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getIPAddress(context: Context): String? {
        val info =
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        if (info != null && info.isConnected) {
            if (info.type == ConnectivityManager.TYPE_MOBILE) { //当前使用2G/3G/4G网络
                try {
                    val en = NetworkInterface.getNetworkInterfaces()
                    while (en.hasMoreElements()) {
                        val intf = en.nextElement()
                        val enumIpAddr = intf.inetAddresses
                        while (enumIpAddr.hasMoreElements()) {
                            val inetAddress = enumIpAddr.nextElement()
                            if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                                return inetAddress.getHostAddress()
                            }
                        }
                    }
                } catch (e: SocketException) {
                    e.printStackTrace()
                }
            } else if (info.type == ConnectivityManager.TYPE_WIFI) { //当前使用无线网络
                val wifiManager =
                    context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                return intIP2StringIP(wifiInfo.ipAddress)
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    fun intIP2StringIP(ip: Int): String {
        return (ip and 0xFF).toString() + "." +
                (ip shr 8 and 0xFF) + "." +
                (ip shr 16 and 0xFF) + "." +
                (ip shr 24 and 0xFF)
    }

    /**
     * 打开网络设置界面
     *
     * 在 Android Q 及以上版本中，需要在 AndroidManifest.xml 中添加以下权限：
     *<uses-permission android:name="android.permission.INTERNET" />
     * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
     * <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
     * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
     * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
     *
     * 在 Android P 及以下版本中，需要在 AndroidManifest.xml 中添加以下权限：
     *
     *<uses-permission android:name="android.permission.INTERNET" />
     * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
     * <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
     * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
     * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
     * <uses-permission android:name="android.permission.WRITE_SETTINGS" />
     *
     *
     * @param context 上下文
     */
    fun openNetworkSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val panelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            try {
                context.startActivity(panelIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val settingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(settingsIntent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                val intent = Intent(Settings.ACTION_SETTINGS)
                try {
                    context.startActivity(intent)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    /**
     * 获取活动网络信息
     *
     * @param context 上下文
     * @return NetworkInfo
     */
    @JvmStatic
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun getActiveNetworkInfo(context: Context): NetworkInfo? {
        val cm = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo
    }

    /**
     * 判断网络是否连接
     *
     * @param context 上下文
     * @return `true`: 是<br></br>`false`: 否
     */
    @JvmStatic
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isConnected(context: Context): Boolean {
        val info = getActiveNetworkInfo(context)
        return info != null && info.isConnected
    }
}