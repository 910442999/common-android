package com.yuanquan.common.utils.permissions;

/**
 * @author：luck
 * @date：2021/11/18 10:15 上午
 * @describe：PermissionResultCallback
 */
public interface PermissionResultCallback {

    void onGranted();

    void onDenied();
}
