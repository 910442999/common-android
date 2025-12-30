package com.yuanquan.common.utils

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission

object MicrophoneUtils {

    data class MicrophoneInfo(
        val id: String,
        val name: String,
        val type: Int,
        val isBuiltIn: Boolean,
        val productName: String
    )

    /**
     * 获取所有麦克风设备
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getAllMicrophones(context: Context): List<MicrophoneInfo> {
        val microphones = mutableListOf<MicrophoneInfo>()

        // 添加内置和有线麦克风
        microphones.addAll(getAudioInputDevices(context))

        // 添加USB麦克风
        microphones.addAll(getUsbMicrophones(context))

        // 添加蓝牙麦克风
        microphones.addAll(getBluetoothMicrophones(context))

        return microphones.distinctBy { it.id }
    }

    /**
     * 获取音频输入设备（内置、有线耳机等）
     */
    /**
     * 安全地获取音频输入设备（兼容低版本API）
     */
    fun getAudioInputDevices(context: Context): List<MicrophoneInfo> {
        val microphones = mutableListOf<MicrophoneInfo>()

        // 方法1：更严格的API级别检查
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                // 使用反射进行二次验证（可选，但更安全）
                val getDevicesMethod =
                    audioManager.javaClass.getMethod("getDevices", Int::class.javaPrimitiveType)

                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
                for (device in devices) {
                    if (isMicrophoneDevice(device.type)) {
                        microphones.add(
                            MicrophoneInfo(
                                id = "audio_${device.id}",
                                name = getDeviceTypeName(device.type),
                                type = device.type,
                                isBuiltIn = device.type == AudioDeviceInfo.TYPE_BUILTIN_MIC,
                                productName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    device.productName?.toString() ?: ""
                                } else {
                                    ""
                                }
                            )
                        )
                    }
                }
            } catch (e: NoSuchMethodError) {
                // 明确捕获方法不存在错误，并记录或降级处理
                Log.e(
                    "MicrophoneUtils",
                    "getDevices method not available on this device API ${Build.VERSION.SDK_INT}",
                    e
                )
                // 可以在这里调用针对旧版本的备用方案
                microphones.addAll(getAudioInputDevicesLegacy(context))
            } catch (e: Exception) {
                // 捕获其他可能的异常
                Log.e("MicrophoneUtils", "Error getting audio input devices", e)
            }
        } else {
            // 如果系统版本低于 Android M (6.0)，直接使用备用方案
            microphones.addAll(getAudioInputDevicesLegacy(context))
        }
        return microphones
    }

    /**
     * 用于 API < 23 的备用方案：检测音频路由状态
     */
    @SuppressLint("NewApi") // 在低版本上，我们不会调用高API方法，但编译器可能警告，此注解可消除警告
    private fun getAudioInputDevicesLegacy(context: Context): List<MicrophoneInfo> {
        val legacyMicrophones = mutableListOf<MicrophoneInfo>()
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (hasBuiltInMicrophone(context)) {
            // 内置麦克风始终假定存在
            legacyMicrophones.add(
                MicrophoneInfo(
                    id = "audio_builtin_mic",
                    name = "内置麦克风",
                    type = AudioDeviceInfo.TYPE_BUILTIN_MIC,
                    isBuiltIn = true,
                    productName = "Built-in Microphone"
                )
            )
        }
        // 方案1：检测是否插入了有线耳机（常用且相对可靠）
        val isWiredHeadsetConnected = audioManager.isWiredHeadsetOn
        // 如果有线耳机已连接，假定它带有麦克风
        if (isWiredHeadsetConnected) {
            legacyMicrophones.add(
                MicrophoneInfo(
                    id = "audio_wired_headset",
                    name = "有线耳机麦克风",
                    type = AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    isBuiltIn = false,
                    productName = "Wired Headset"
                )
            )
        }

        // 方案2：通过广播监听（更复杂，但更动态）
        // 您需要注册监听 ACTION_HEADSET_PLUG 等广播来动态更新设备列表
        // 这里只返回静态检测结果

        Log.i("MicrophoneUtils", "Using legacy method, detected ${legacyMicrophones.size} devices.")
        return legacyMicrophones
    }

    fun hasBuiltInMicrophone(context: Context): Boolean {
        val packageManager = context.packageManager
        // 判断设备是否声明了麦克风特性
        return packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    }

    /**
     * 获取USB麦克风
     */
    fun getUsbMicrophones(context: Context): List<MicrophoneInfo> {
        val usbMicrophones = mutableListOf<MicrophoneInfo>()

        // 方案1A：使用高版本API（API 23+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getUsbMicrophonesApi23Plus(context, usbMicrophones)
        }
        // 方案1B：低版本降级方案（API < 23）
        else {
            getUsbMicrophonesLegacy(context, usbMicrophones)
        }

        return usbMicrophones
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getUsbMicrophonesApi23Plus(
        context: Context,
        resultList: MutableList<MicrophoneInfo>
    ) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)

            for (device in devices) {
                if (device.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                    device.type == AudioDeviceInfo.TYPE_USB_DEVICE
                ) {

                    resultList.add(
                        MicrophoneInfo(
                            id = "audio_${device.id}",
                            name = getDeviceTypeName(device.type),
                            type = device.type,
                            isBuiltIn = false,
                            productName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                device.productName?.toString() ?: "USB麦克风"
                            } else {
                                "USB麦克风"
                            }
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // 降级到传统方案
            getUsbMicrophonesLegacy(context, resultList)
        }
    }

    /**
     * 低版本Android的降级实现
     */
    private fun getUsbMicrophonesLegacy(context: Context, resultList: MutableList<MicrophoneInfo>) {
        // 方案1：检测USB音频设备连接状态
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
        usbManager?.deviceList?.values?.forEach { usbDevice ->
            // 检查是否为音频设备（class code = 1）
            if (usbDevice.deviceClass == 1 || hasAudioInterface(usbDevice)) {
                resultList.add(
                    MicrophoneInfo(
                        id = "usb_${usbDevice.deviceId}",
                        name = "USB音频设备",
                        type = -1, // 低版本无对应类型常量
                        isBuiltIn = false,
                        productName = usbDevice.productName ?: "USB麦克风"
                    )
                )
            }
        }
    }

    /**
     * 检查USB设备是否包含音频接口
     */
    private fun hasAudioInterface(usbDevice: UsbDevice): Boolean {
        for (i in 0 until usbDevice.interfaceCount) {
            val usbInterface = usbDevice.getInterface(i)
            if (usbInterface.interfaceClass == 1) { // 音频设备类
                return true
            }
        }
        return false
    }

    /**
     * 获取蓝牙麦克风
     * 注意：此方法部分设备需要需要蓝牙权限，否则报
     *     java.lang.SecurityException: Need android.permission.BLUETOOTH_CONNECT permission for android.content.AttributionSource@f5a2f3b5: AdapterService getBondedDevices
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun getBluetoothMicrophones(context: Context): List<MicrophoneInfo> {
        val bluetoothMicrophones = mutableListOf<MicrophoneInfo>()

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            return bluetoothMicrophones
        }

        val pairedDevices = bluetoothAdapter.bondedDevices
        for (device in pairedDevices) {
            if (isBluetoothMicrophone(device)) {
                bluetoothMicrophones.add(
                    MicrophoneInfo(
                        id = "bt_${device.address}",
                        name = device.name ?: "蓝牙麦克风",
                        type = AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                        isBuiltIn = false,
                        productName = device.name ?: "蓝牙麦克风"
                    )
                )
            }
        }

        return bluetoothMicrophones
    }

    /**
     * 检查设备类型是否为麦克风
     */
    fun isMicrophoneDevice(type: Int): Boolean {
        return type == AudioDeviceInfo.TYPE_BUILTIN_MIC ||
                type == AudioDeviceInfo.TYPE_WIRED_HEADSET
    }

    fun isBluetoothMicrophoneDevice(type: Int): Boolean {
        return type == AudioDeviceInfo.TYPE_BUILTIN_MIC ||
                type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
    }

    fun isUSBMicrophoneDevice(type: Int): Boolean {
        return (type == AudioDeviceInfo.TYPE_USB_HEADSET || type == AudioDeviceInfo.TYPE_USB_DEVICE)
    }

    /**
     * 获取设备类型名称
     */
    fun getDeviceTypeName(type: Int): String {
        return when (type) {
            AudioDeviceInfo.TYPE_BUILTIN_MIC -> "内置麦克风"
            AudioDeviceInfo.TYPE_WIRED_HEADSET -> "有线耳机麦克风"
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "蓝牙麦克风"
            AudioDeviceInfo.TYPE_USB_HEADSET -> "USB耳机麦克风"
            AudioDeviceInfo.TYPE_USB_DEVICE -> "USB设备麦克风"
            else -> "未知麦克风"
        }
    }

    /**
     * 检查USB设备是否为麦克风
     */
    private fun isUsbMicrophone(device: android.hardware.usb.UsbDevice): Boolean {
        if (device.deviceClass == 0x00 && device.deviceSubclass == 0x00) {
            for (i in 0 until device.interfaceCount) {
                val usbInterface = device.getInterface(i)
                if (usbInterface.interfaceClass == 1) { // 音频类
                    return true
                }
            }
        }
        return false
    }

    /**
     * 检查蓝牙设备是否为麦克风
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun isBluetoothMicrophone(device: BluetoothDevice): Boolean {
        return device.bluetoothClass.majorDeviceClass ==
                BluetoothClass.Device.Major.AUDIO_VIDEO &&
                (device.bluetoothClass.hasService(BluetoothProfile.HEADSET)/* ||
                        device.bluetoothClass.hasService(android.bluetooth.BluetoothProfile.HANDSFREE)*/)
    }
}