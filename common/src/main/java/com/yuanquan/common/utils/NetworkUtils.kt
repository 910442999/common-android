package com.yuanquan.common.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.RouteInfo
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import java.math.BigInteger
import java.net.Inet4Address
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder
import java.util.Locale


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
                LogUtil.e("getCurrentSsid：获取失败")
                return ""
            } else {
                LogUtil.e("getCurrentSsid：请求未完成")
                return ""
            }
        }
        LogUtil.e("getCurrentSsid：未启用")
        return ""
    }

    /**
     * 获取当前WIFI名称
     *
     * @param context 上下文
     * @return 当前WIFI名称
     */

    @JvmStatic
    fun getCurrentSsid2(context: Context): String {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        if (wifiManager != null && wifiManager.isWifiEnabled) {
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
                LogUtil.e("getCurrentSsid：获取失败")
                return ""
            } else {
                LogUtil.e("getCurrentSsid：请求未完成")
                return ""
            }
        }
        LogUtil.e("getCurrentSsid：未启用")
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
    fun getIPAddress(context: Context): String {
//        val info =
//            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
//        if (info != null && info.isConnected) {
//            if (info.type == ConnectivityManager.TYPE_MOBILE) { //当前使用2G/3G/4G网络
//                try {
//                    val en = NetworkInterface.getNetworkInterfaces()
//                    while (en.hasMoreElements()) {
//                        val intf = en.nextElement()
//                        val enumIpAddr = intf.inetAddresses
//                        while (enumIpAddr.hasMoreElements()) {
//                            val inetAddress = enumIpAddr.nextElement()
//                            if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
//                                return inetAddress.getHostAddress()
//                            }
//                        }
//                    }
//                } catch (e: SocketException) {
//                    e.printStackTrace()
//                }
//            } else if (info.type == ConnectivityManager.TYPE_WIFI) { //当前使用无线网络
//                val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
//                val wifiInfo = wifiManager.connectionInfo
//                return intIP2StringIP(wifiInfo.ipAddress)
//            }
//        } else {
//            //当前无网络连接,请在设置中打开网络
//        }
//        return null
        var address = getIPv4Address(context)
        if (address.isNullOrBlank()) {
            return getHotspotAddress(context)
        } else {
            return address
        }
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    fun intIP2StringIP(ip: Int): String {
        return (ip and 0xFF).toString() + "." + (ip shr 8 and 0xFF) + "." + (ip shr 16 and 0xFF) + "." + (ip shr 24 and 0xFF)
    }

    /**
     * 校验是否是ip地址
     */
    fun isValidIPAddress(ipAddress: String): Boolean {
        val regex = Regex(
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
        )

        return ipAddress.matches(regex)
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getIPv4Address(context: Context): String? {
        val connectivityManager = ContextCompat.getSystemService(context, ConnectivityManager::class.java)
        var network = connectivityManager?.boundNetworkForProcess
        if (network == null) {
            network = connectivityManager?.activeNetwork
        }
        if (network == null) return null
        val linkProperties = connectivityManager?.getLinkProperties(network) ?: return null
        linkProperties.linkAddresses.forEach { address ->
            if (address.address is Inet4Address) {
                return address.address.hostAddress
            }
        }
        return null
    }

    fun getHotspotAddress(context: Context): String {
        var wifiManager: WifiManager =
            context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcp = wifiManager.dhcpInfo
        var ipAddress = dhcp.ipAddress
        ipAddress = if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            Integer.reverseBytes(ipAddress)
        } else {
            ipAddress
        }
        val ipAddressByte = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
        try {
            return InetAddress.getByAddress(ipAddressByte).hostAddress ?: ""
        } catch (e: UnknownHostException) {
            Log.e("NetworkUtils", "Error getting Hotspot IP address ", e)
        }
        return ""
    }

    /**
     * 获取网关地址
     *
     * @return 获取失败则返回空字符
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getGateway(context: Context): String {
        var address = getIPv4GatewayAddress(context)
        if (address.isNullOrBlank()) {
            return getHotspotGatewayAddress(context)
        } else {
            return address
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getIPv4GatewayAddress(context: Context): String? {
        val connectivityManager = ContextCompat.getSystemService(context, ConnectivityManager::class.java)
        var network = connectivityManager?.boundNetworkForProcess
        if (network == null) {
            network = connectivityManager?.activeNetwork
        }
        if (network == null) return null
        val linkProperties = connectivityManager?.getLinkProperties(network) ?: return null
        val routes: List<RouteInfo> = linkProperties.routes
        for (route in routes) {
            if (route.isDefaultRoute) {
                val gateway: InetAddress? = route.gateway
                if (gateway is Inet4Address) {
                    var hostAddress = gateway.getHostAddress()
                    return hostAddress
                }
            }
        }
        return null
    }

    fun getHotspotGatewayAddress(context: Context): String {
        var wifiManager: WifiManager =
            context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcp = wifiManager.dhcpInfo
        var ipAddress = dhcp.gateway
        ipAddress = if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            Integer.reverseBytes(ipAddress)
        } else {
            ipAddress
        }
        val ipAddressByte = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
        try {
            return InetAddress.getByAddress(ipAddressByte).hostAddress ?: ""
        } catch (e: UnknownHostException) {
            Log.e("NetworkUtils", "Error getting Hotspot IP address ", e)
        }
        return ""
    }


    // 获取以太网网关
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getEthGateway(context: Context): String? {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networks = connectivityManager.allNetworks
        for (network in networks) {
            val linkProperties = connectivityManager.getLinkProperties(network)
            // 检查网络类型是否为以太网（需 API 23+）
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true) {
                linkProperties?.routes?.forEach { route ->
                    // 默认网关的路由目的地址为 0.0.0.0/0
                    if (route.isDefaultRoute) {
                        val gateway: InetAddress? = route.gateway
                        if (gateway is Inet4Address) {
                            var hostAddress = gateway.getHostAddress()
                            return hostAddress
                        }
                    }
                }
            }
        }
        return null
    }

    // 获取以太网网关
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_NETWORK_STATE])
    fun getEthGateway2(context: Context): String? {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return null
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        // 检查是否为以太网且具备互联网能力
        if (capabilities != null &&
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) {
            // 使用 LinkProperties 的获取方式适配 API 31+
            val linkProperties = connectivityManager.getLinkProperties(network)
            linkProperties?.routes?.forEach { route ->
                val gateway: InetAddress? = route.gateway
                if (route.isDefaultRoute && gateway is Inet4Address) {
                    (route.gateway as? Inet4Address)?.hostAddress?.let {
                        return it
                    }
                }
            }
        }
        return null
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
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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

    @JvmStatic
    fun openWifiSettings(context: Context) {
        val intent = Intent()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                intent.action = Settings.ACTION_WIFI_SETTINGS
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                intent.action = Settings.ACTION_WIFI_SETTINGS
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            else -> {
                intent.action = Settings.ACTION_WIRELESS_SETTINGS
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
        context.startActivity(intent)
    }

    @JvmStatic
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @JvmStatic
    fun openLocationSettings(context: Context) {
        val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(settingsIntent)
    }
}