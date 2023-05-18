package com.yuanquan.common.utils.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.Size;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.yuanquan.common.utils.ActivityCompatHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author：luck
 * @date：2021/11/18 10:12 上午
 * @describe：PermissionUtil
 */
public class PermissionUtils {
    private static final int REQUEST_CODE = 88888;

    private static PermissionUtils mInstance;

    private PermissionUtils() {

    }

    public static PermissionUtils getInstance() {
        if (mInstance == null) {
            synchronized (PermissionChecker.class) {
                if (mInstance == null) {
                    mInstance = new PermissionUtils();
                }
            }
        }
        return mInstance;
    }


    public void requestPermissions(Activity activity, @Size(min = 1) @NonNull String[] permissions, PermissionResultCallback callback) {
        requestPermissions(activity, permissions, REQUEST_CODE, callback);
    }

    private void requestPermissions(Activity activity, @Size(min = 1) @NonNull String[] permissions, final int requestCode, PermissionResultCallback permissionResultCallback) {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return;
        }
        if (Build.VERSION.SDK_INT < 23) {
            if (permissionResultCallback != null) {
                permissionResultCallback.onGranted();
            }
            return;
        }
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        if (permissionList.size() > 0) {
            String[] requestArray = new String[permissionList.size()];
            permissionList.toArray(requestArray);
            ActivityCompat.requestPermissions(activity, requestArray, requestCode);
        } else {
            if (permissionResultCallback != null) {
                permissionResultCallback.onGranted();
            }
        }
    }

    public void onRequestPermissionsResult(int[] grantResults, PermissionResultCallback action) {
        if (PermissionUtils.isAllGranted(grantResults)) {
            action.onGranted();
        } else {
            action.onDenied();
        }
    }


    /**
     * 检查读取图片权限是否存在
     */
    @RequiresApi(api = 33)
    public static boolean isCheckReadImages(Context context) {
        return hasPermissions(context, Manifest.permission.READ_MEDIA_IMAGES);
    }

    /**
     * 检查读取视频权限是否存在
     */
    @RequiresApi(api = 33)
    public static boolean isCheckReadVideo(Context context) {
        return hasPermissions(context, Manifest.permission.READ_MEDIA_VIDEO);
    }

    /**
     * 检查读取音频权限是否存在
     */
    @RequiresApi(api = 33)
    public static boolean isCheckReadAudio(Context context) {
        return hasPermissions(context, Manifest.permission.READ_MEDIA_AUDIO);
    }

    /**
     * 检查写入权限是否存在
     */
    public static boolean isCheckWriteExternalStorage(Context context) {
        return hasPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * 检查读取权限是否存在
     */
    public static boolean isCheckReadExternalStorage(Context context) {
        return hasPermissions(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    }


    /**
     * 检查相机权限是否存在
     */
    public static boolean isCheckCamera(Context context) {
        return hasPermissions(context, Manifest.permission.CAMERA);
    }

    /**
     * 检查是否有某个权限
     *
     * @param context
     * @param permission
     */
    public static boolean hasPermissions(@NonNull Context context, @NonNull String permission) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    /**
     * 检查是否有某个权限
     *
     * @param context
     * @param permissions
     */
    public static boolean hasPermissions(@NonNull Context context, @Size(min = 1) @NonNull String[] permissions) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAllGranted(int[] grantResults) {
        boolean isAllGranted = true;
        if (grantResults.length > 0) {
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
        } else {
            isAllGranted = false;
        }
        return isAllGranted;
    }


    /**
     * 跳转到系统设置页面
     */
    public static void startIntentSetting(Activity activity, int requestCode) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivityForResult(intent, requestCode);
    }
}
