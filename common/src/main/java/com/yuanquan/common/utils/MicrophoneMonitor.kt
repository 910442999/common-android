package com.yuanquan.common.utils

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi

class MicrophoneMonitor(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var callback: MicrophoneChangeCallback? = null

    interface MicrophoneChangeCallback {
        fun onMicrophoneAdded(microphone: MicrophoneUtils.MicrophoneInfo)
        fun onMicrophoneRemoved(microphone: MicrophoneUtils.MicrophoneInfo)
    }

    fun setCallback(callback: MicrophoneChangeCallback) {
        this.callback = callback
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun startMonitoring() {
        audioManager.registerAudioDeviceCallback(deviceCallback, null)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun stopMonitoring() {
        audioManager.unregisterAudioDeviceCallback(deviceCallback)
    }

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            super.onAudioDevicesAdded(addedDevices)
            for (device in addedDevices) {
                if (MicrophoneUtils.isMicrophoneDevice(device.type) || MicrophoneUtils.isBluetoothMicrophoneDevice(
                        device.type
                    ) || MicrophoneUtils.isUSBMicrophoneDevice(
                        device.type
                    )
                ) {
                    callback?.onMicrophoneAdded(
                        MicrophoneUtils.MicrophoneInfo(
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
                    )
                }
            }
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            super.onAudioDevicesRemoved(removedDevices)
            for (device in removedDevices) {
                if (MicrophoneUtils.isMicrophoneDevice(device.type) || MicrophoneUtils.isBluetoothMicrophoneDevice(
                        device.type
                    ) || MicrophoneUtils.isUSBMicrophoneDevice(
                        device.type
                    )
                ) {
                    callback?.onMicrophoneRemoved(
                        MicrophoneUtils.MicrophoneInfo(
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
                    )
                }
            }
        }
    }
}