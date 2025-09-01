package com.yuanquan.common.utils

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager


class AudioRouter(private val context: Context) {
    private val audioManager: AudioManager

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    // 切换到听筒
    fun setEarpieceMode() {
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)
        audioManager.setSpeakerphoneOn(false)
        audioManager.setBluetoothScoOn(false)
    }

    // 切换到扬声器
    fun setSpeakerMode() {
        audioManager.setMode(AudioManager.MODE_NORMAL)
        audioManager.setSpeakerphoneOn(true)
    }

    // 切换到蓝牙耳机
    fun setBluetoothMode() {
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)
        audioManager.setBluetoothScoOn(true)
        audioManager.startBluetoothSco()
    }

    // 监听音频设备变化
    fun registerDeviceCallback() {
        audioManager.registerAudioDeviceCallback(object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo?>?) {
                checkPreferredDevice()
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo?>?) {
                checkPreferredDevice()
            }
        }, null)
    }

    private fun checkPreferredDevice() {
        // 检测当前最佳设备并自动切换
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (device in devices) {
            if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE) {
                setEarpieceMode()
            }
            // 添加其他设备检测...
        }
    }
}