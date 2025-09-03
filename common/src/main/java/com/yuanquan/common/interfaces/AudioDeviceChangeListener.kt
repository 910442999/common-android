package com.yuanquan.common.interfaces

import com.yuanquan.common.utils.MediaManager

/**
 * 进度监听接口
 */
interface AudioDeviceChangeListener {
    fun onAudioMode(audioMode: MediaManager.AudioMode)
    fun onAudioDeviceChanged(deviceName: String?)
}