package com.yuanquan.common.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi

class MicrophoneMonitor(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val appContext = context.applicationContext
    private var callback: MicrophoneChangeCallback? = null

    // 用于 API 21-22 的广播接收器
    private var headsetReceiver: BroadcastReceiver? = null
    var deviceCallback: AudioDeviceCallback? = null

    interface MicrophoneChangeCallback {
        fun onMicrophoneAdded(microphone: MicrophoneUtils.MicrophoneInfo)
        fun onMicrophoneRemoved(microphone: MicrophoneUtils.MicrophoneInfo)
    }

    fun setCallback(callback: MicrophoneChangeCallback) {
        this.callback = callback
    }

    fun startMonitoring() {
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                // API 23+ 使用 AudioDeviceCallback
                registerDeviceCallback()
            } else {
                // API 21-22 使用广播
                registerLegacyReceiver()
            }
        } catch (e: Exception) {
            LogUtil.e(e)
        }
    }

    fun stopMonitoring() {
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                unregisterDeviceCallback()
            } else {
                unregisterLegacyReceiver()
            }
        } catch (e: Exception) {
            LogUtil.e(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun registerDeviceCallback() {
        deviceCallback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
                for (device in addedDevices) {
                    if (isMicrophoneType(device.type)) {
                        callback?.onMicrophoneAdded(createMicrophoneInfoFromDevice(device))
                    }
                }
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
                for (device in removedDevices) {
                    if (isMicrophoneType(device.type)) {
                        callback?.onMicrophoneRemoved(createMicrophoneInfoFromDevice(device))
                    }
                }
            }
        }
        audioManager.registerAudioDeviceCallback(deviceCallback, null)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun unregisterDeviceCallback() {
        if (deviceCallback != null) audioManager.unregisterAudioDeviceCallback(deviceCallback)
    }

    /**
     * 为 API 21-22 注册广播接收器，主要监听有线耳机的插拔。
     * 注意：此方法无法精确检测蓝牙或USB麦克风。
     */
    private fun registerLegacyReceiver() {
        if (headsetReceiver != null) return

        headsetReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_HEADSET_PLUG) {
                    val state = intent.getIntExtra("state", -1)
                    // 这里简化处理：插入视为增加一个有线麦克风，拔出视为移除。
                    // 由于广播信息有限，无法获取如 device.id 或 productName 等详细信息。
                    val simulatedDeviceInfo = MicrophoneUtils.MicrophoneInfo(
                        id = "legacy_wired_headset",
                        name = "有线耳机麦克风",
                        type = AudioDeviceInfo.TYPE_WIRED_HEADSET, // 注意：此常量在API 21存在
                        isBuiltIn = false,
                        productName = "Legacy Wired Headset"
                    )
                    when (state) {
                        1 -> callback?.onMicrophoneAdded(simulatedDeviceInfo) // 插入
                        0 -> callback?.onMicrophoneRemoved(simulatedDeviceInfo) // 拔出
                    }
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        appContext.registerReceiver(headsetReceiver, filter)
    }

    private fun unregisterLegacyReceiver() {
        headsetReceiver?.let {
            appContext.unregisterReceiver(it)
            headsetReceiver = null
        }
    }

    private fun isMicrophoneType(type: Int): Boolean {
        return (MicrophoneUtils.isMicrophoneDevice(type) ||
                MicrophoneUtils.isBluetoothMicrophoneDevice(type) ||
                MicrophoneUtils.isUSBMicrophoneDevice(type))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createMicrophoneInfoFromDevice(device: AudioDeviceInfo): MicrophoneUtils.MicrophoneInfo {
        return MicrophoneUtils.MicrophoneInfo(
            id = "audio_${device.id}",
            name = MicrophoneUtils.getDeviceTypeName(device.type),
            type = device.type,
            isBuiltIn = device.type == AudioDeviceInfo.TYPE_BUILTIN_MIC,
            productName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                device.productName?.toString() ?: ""
            } else {
                ""
            }
        )
    }
}