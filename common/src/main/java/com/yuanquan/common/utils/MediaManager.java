package com.yuanquan.common.utils;

import android.content.Context;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import com.yuanquan.common.interfaces.AudioDeviceChangeListener;

public class MediaManager {
    private static final String TAG = "MediaManager";
    private final AudioManager audioManager;
    private boolean isSpeakerOn = true;
    private boolean isAudioOnly;
    public AudioMode currentMode = AudioMode.NORMAL;
    private int audioSessionId = AudioManager.ERROR; // 初始化为无效值
    private AudioType audioType = AudioType.VOICE; // 默认语音类型
    private AudioDeviceCallback audioDeviceCallback;

    // 音频类型枚举
    public enum AudioType {
        VOICE,      // 语音通话
        MUSIC       // 音乐播放
    }

    // 音频模式枚举
    public enum AudioMode {
        EARPIECE,   // 听筒模式
        SPEAKER,    // 扬声器模式
        BLUETOOTH,  // 蓝牙模式
        HEADPHONES, // 有线耳机模式
        NORMAL      // 默认模式
    }

    public MediaManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        registerDeviceCallback();
    }

    private AudioDeviceChangeListener audioDeviceChangeListener;

    public void setAudioDeviceChangeListener(AudioDeviceChangeListener audioDeviceChangeListener) {
        this.audioDeviceChangeListener = audioDeviceChangeListener;
    }

    // 新增：获取当前连接的音频设备名称
    public String getCurrentAudioDeviceName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            for (AudioDeviceInfo device : devices) {
                if (isDeviceActive(device)) {
                    return device.getProductName().toString();
                }
            }
        }
        return "";
    }

    // 检查设备是否处于活动状态
    private boolean isDeviceActive(AudioDeviceInfo device) {
        switch (currentMode) {
            case EARPIECE:
                return device.getType() == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE;
            case SPEAKER:
                return device.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER;
            case BLUETOOTH:
                return device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO;
            case HEADPHONES:
                return device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                        device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET;
            default:
                return false;
        }
    }

    // 新增：设置音频类型
    public void setAudioType(AudioType type) {
        this.audioType = type;
//        applyOptimalAudioMode();
    }

    // 新增：设置音频会话ID
    public void setAudioSessionId(int sessionId) {
        // 只接受有效的会话ID
        if (sessionId != AudioManager.ERROR) {
            this.audioSessionId = sessionId;
        }
    }

    // 新增：获取音频会话ID
    public int getAudioSessionId() {
        return audioSessionId;
    }

    // 新增：获取AudioManager实例
    public AudioManager getAudioManager() {
        return audioManager;
    }

    /**
     * 设置音频模式偏好
     *
     * @param audioOnly true为语音通话，false为音频播放
     */
    public void configureAudio(boolean audioOnly) {
        isAudioOnly = audioOnly;
//        applyOptimalAudioMode();
    }

    /**
     * 应用最佳音频模式（基于连接设备和用户偏好）
     */
    public void applyOptimalAudioMode() {
        if (isHeadphonesPlugged()) {
            audioDeviceChangeListener.onAudioMode(AudioMode.HEADPHONES);
        } else if (isBluetoothHeadsetConnected()) {
            audioDeviceChangeListener.onAudioMode(AudioMode.BLUETOOTH);
        } else if (audioType == AudioType.VOICE) {
            // 在语音模式下根据设备自动选择听筒或扬声器
            if (isAudioOnly) {
                boolean currentSpeakerOn = isSpeakerphoneOn();
                if (currentSpeakerOn) {
                    audioDeviceChangeListener.onAudioMode(AudioMode.SPEAKER);
                } else {
                    audioDeviceChangeListener.onAudioMode(AudioMode.EARPIECE);
                }
            } else {
                audioDeviceChangeListener.onAudioMode(AudioMode.EARPIECE);
            }
        } else {
            audioDeviceChangeListener.onAudioMode(AudioMode.NORMAL);
        }
    }

    /**
     * 设置音频模式
     */
    public void setAudioMode(AudioMode mode) {
        if (audioManager == null) return;
        currentMode = mode;
        if (audioDeviceChangeListener != null) {
            audioDeviceChangeListener.onAudioMode(mode);
            audioDeviceChangeListener.onAudioDeviceChanged(getCurrentAudioDeviceName());
        }
        try {
            switch (mode) {
                case EARPIECE:
                    setEarpieceMode();
                    break;
                case SPEAKER:
                    setSpeakerMode(true);
                    break;
                case BLUETOOTH:
                    setBluetoothMode();
                    break;
                case HEADPHONES:
                    setHeadphonesMode();
                    break;
                case NORMAL:
                default:
                    setNormalMode();
                    break;
            }
            Log.d(TAG, "Audio mode set to: " + mode);
        } catch (Exception e) {
            Log.e(TAG, "Error setting audio mode: " + e.getMessage(), e);
        }
    }

    /**
     * 切换到听筒模式
     */
    private void setEarpieceMode() {
        // 音乐播放不支持听筒模式
        if (audioType == AudioType.MUSIC) {
            Log.w(TAG, "Music playback does not support earpiece mode");
            setSpeakerMode(true);
            return;
        }
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        // 现代设备使用setCommunicationDevice
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            for (AudioDeviceInfo device : audioManager.getAvailableCommunicationDevices()) {
                if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE) {
                    audioManager.setCommunicationDevice(device);
                    break;
                }
            }
        }
        // 旧设备使用传统方法
        else {
            audioManager.setSpeakerphoneOn(false);
            audioManager.setBluetoothScoOn(false);
        }
    }

    /**
     * 切换到扬声器模式
     */
    public void setSpeakerMode(boolean enable) {
        if (audioManager == null) return;

        isSpeakerOn = enable;
        // 根据音频类型设置不同的音频模式
        if (audioType == AudioType.VOICE) {
            audioManager.setMode(enable ? AudioManager.MODE_NORMAL : AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
        audioManager.setSpeakerphoneOn(enable);

        if (!enable) {
            // 确保关闭其他音频路由
            audioManager.setBluetoothScoOn(false);
        }

        // 语音通话才设置通话音量
        if (audioType == AudioType.VOICE) {
            setVoiceCallVolume();
        }
    }

    /**
     * 新增：音乐播放模式
     */
    private void setMusicMode() {
        audioManager.setMode(AudioManager.MODE_NORMAL);

        // 现代设备使用setCommunicationDevice
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 尝试使用内置扬声器
            for (AudioDeviceInfo device : audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
                if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                    audioManager.setCommunicationDevice(device);
                    break;
                }
            }
        } else {
            // 传统方法：启用扬声器
            audioManager.setSpeakerphoneOn(true);
            audioManager.setBluetoothScoOn(false);
        }

        // 设置音乐音量
        setMusicVolume();
    }

    /**
     * 切换到蓝牙耳机模式
     */
    public void setBluetoothMode() {
        if (audioManager == null || !isBluetoothHeadsetConnected()) return;

        // 根据音频类型设置不同的音频模式
        if (audioType == AudioType.VOICE) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }

        // 现代设备使用setCommunicationDevice
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            for (AudioDeviceInfo device : audioManager.getAvailableCommunicationDevices()) {
                if (device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                    audioManager.setCommunicationDevice(device);
                    break;
                }
            }
        }
        // 旧设备使用传统方法
        else {
            audioManager.startBluetoothSco();
            audioManager.setBluetoothScoOn(true);
            audioManager.setSpeakerphoneOn(false);
        }
    }

    // 新增：设置有线耳机模式
    private void setHeadphonesMode() {
        this.setNormalMode();
    }

    /**
     * 设置默认音频模式
     */
    private void setNormalMode() {
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(false);
        audioManager.setBluetoothScoOn(false);
    }

    /**
     * 设置通话流音量到合理水平
     */
    private void setVoiceCallVolume() {
        if (audioManager == null || audioType != AudioType.VOICE) return;

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);

        // 如果音量太低（低于最大音量的25%）或静音，设置到中间水平
        if (currentVolume < maxVolume * 0.25) {
            int targetVolume = (int) (maxVolume * 0.5);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, targetVolume, 0);
        }
    }

    /**
     * 新增：设置音乐音量
     */
    private void setMusicVolume() {
        if (audioManager == null) return;

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (currentVolume < maxVolume * 0.3) {
            int targetVolume = (int) (maxVolume * 0.7);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0);
        }
    }

    /**
     * 检查是否有耳机插入（有线）
     */
    public boolean isHeadphonesPlugged() {
        if (audioManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (AudioDeviceInfo device : audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
                int type = device.getType();
                if (type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                        type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    return true;
                }
            }
            return false;
        } else {
            return audioManager.isWiredHeadsetOn();
        }
    }

    /**
     * 检查蓝牙耳机是否连接
     */
    public boolean isBluetoothHeadsetConnected() {
        if (audioManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (AudioDeviceInfo device : audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
                int type = device.getType();
                if (type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                    return true;
                }
            }
            return false;
        } else {
            return audioManager.isBluetoothA2dpOn() || audioManager.isBluetoothScoOn();
        }
    }

    /**
     * 注册音频设备变化回调
     */
    public void registerDeviceCallback() {
        if (audioManager == null) return;
        audioDeviceCallback = new AudioDeviceCallback() {
            @Override
            public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
                handleAudioDeviceChange();
            }

            @Override
            public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
                handleAudioDeviceChange();
            }
        };
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null);
    }

    public void unregisterAudioDeviceCallback() {
        if (audioManager != null && audioDeviceCallback != null) {
            audioManager.unregisterAudioDeviceCallback(audioDeviceCallback);
        }
    }

    /**
     * 处理音频设备变化
     */
    private void handleAudioDeviceChange() {
        Log.d(TAG, "Audio devices changed. Reevaluating audio mode...");
        applyOptimalAudioMode(); // 重新应用最佳音频模式

        // 通知设备变化
//        if (audioDeviceChangeListener != null) {
//            audioDeviceChangeListener.onAudioMode(currentMode);
//            audioDeviceChangeListener.onAudioDeviceChanged(getCurrentAudioDeviceName());
//        }
    }

    /**
     * 获取当前是否蓝牙模式
     */
    public boolean isBluetoothScoOn() {
        return audioManager != null && audioManager.isBluetoothScoOn();
    }

    /**
     * 停止蓝牙Sco模式
     */
    public void stopBluetoothSco() {
        if (audioManager != null) {
            audioManager.stopBluetoothSco();
            audioManager.setBluetoothScoOn(false);
        }
    }

    /**
     * 获取当前是否扬声器开启
     */
    public boolean isSpeakerphoneOn() {
        return audioManager != null && audioManager.isSpeakerphoneOn();
    }

    /**
     * 设置特定音频模式
     */
    public void setMode(int mode) {
        if (audioManager != null) {
            audioManager.setMode(mode);
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        if (audioManager != null) {
            // 重置到默认状态
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(false);
            audioManager.setBluetoothScoOn(false);
        }
        audioSessionId = AudioManager.ERROR;
    }
}