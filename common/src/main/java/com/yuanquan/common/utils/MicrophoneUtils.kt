package com.yuanquan.common.utils

import android.Manifest
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.hardware.usb.UsbManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import androidx.annotation.RequiresPermission
import com.yuanquan.common.utils.MicrophoneUtils.MicrophoneInfo

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
    fun getAudioInputDevices(context: Context): List<MicrophoneInfo> {
        val microphones = mutableListOf<MicrophoneInfo>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
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
        }

        return microphones
    }

    /**
     * 获取USB麦克风
     */
    fun getUsbMicrophones(context: Context): List<MicrophoneInfo> {
        val usbMicrophones = mutableListOf<MicrophoneInfo>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            for (device in devices) {
                if (device.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                    device.type == AudioDeviceInfo.TYPE_USB_DEVICE
                ) {
                    usbMicrophones.add(
//                        MicrophoneInfo(
//                            id = "audio_${device.id}",
//                            name = "USB麦克风",
//                            type = AudioDeviceInfo.TYPE_USB_DEVICE,
//                            isBuiltIn = false,
//                            productName = device.productName ?: "USB麦克风"
//                        )
                        MicrophoneInfo(
                            id = "audio_${device.id}",
                            name = getDeviceTypeName(device.type),
                            type = device.type,
                            isBuiltIn = device.type == AudioDeviceInfo.TYPE_BUILTIN_MIC,
                            productName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                device.productName?.toString() ?: ""
                            } else {
                                "USB麦克风"
                            }
                        )
                    )
                }
            }
        }

        return usbMicrophones
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