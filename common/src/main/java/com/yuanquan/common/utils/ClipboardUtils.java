package com.yuanquan.common.utils;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

/**
 * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
 * <p>
 * <!-- Android 13 (API 33) 使用以下权限 -->
 * <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
 */

public class ClipboardUtils {

    /**
     * 获取剪贴板中的文本内容
     *
     * @param context          上下文对象
     * @param clearAfterAccess 是否在获取后清空剪贴板
     * @return 剪贴板文本（若无文本则返回 null）
     */
    @Nullable
    public static String getText(@NonNull Context context, boolean clearAfterAccess) {
        String result = getText(context);
        if (clearAfterAccess && result != null) {
            clearClipboard(context);
        }
        return result;
    }

    /**
     * 获取剪贴板中的文本内容（默认不清空）
     *
     * @param context 上下文对象
     * @return 剪贴板文本
     */
    @Nullable
    public static String getText(@NonNull Context context) {
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);

        if (!clipboard.hasPrimaryClip()) {
            return null;
        }

        ClipDescription description = clipboard.getPrimaryClipDescription();
        if (description == null) {
            return null;
        }

        // 支持多种文本类型
        boolean isText = description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
                description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML) ||
                description.hasMimeType(ClipDescription.MIMETYPE_TEXT_URILIST);

        if (!isText) {
            return null;
        }

        try {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).coerceToText(context);
                return text != null ? text.toString() : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取剪贴板中的图片
     *
     * @param context          上下文对象
     * @param clearAfterAccess 是否在获取后清空剪贴板
     * @return 剪贴板图片 Bitmap
     */
    @Nullable
    public static Uri getImageUri(@NonNull Context context, boolean clearAfterAccess) {
        Uri result = getImageUri(context);
        if (clearAfterAccess && result != null) {
            clearClipboard(context);
        }
        return result;
    }

    @Nullable
    public static Bitmap getImageBitmap(@NonNull Context context, boolean clearAfterAccess) throws IOException {
        Uri result = getImageUri(context);
        if (clearAfterAccess && result != null) {
            clearClipboard(context);
        }
        if (result != null) {
            Bitmap bitmap = BitmapUtil.getBitmapForUri(context, result);
            return bitmap;
        }
        return null;
    }

    /**
     * 获取剪贴板中的图片（默认不清空）
     *
     * @param context 上下文对象
     * @return 剪贴板图片 Uri
     */
    @Nullable
    public static Uri getImageUri(@NonNull Context context) {
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);

        if (!clipboard.hasPrimaryClip()) {
            return null;
        }
        ClipDescription description = clipboard.getPrimaryClipDescription();
        if (description == null) {
            return null;
        }
// 方法1：检查MIME类型是否为图片
        boolean isImage = false;
        for (int i = 0; i < description.getMimeTypeCount(); i++) {
            String mimeType = description.getMimeType(i);
            if (mimeType != null && mimeType.startsWith("image/")) {
                isImage = true;
            }
        }
        if (!isImage) {
            return null;
        }
        ClipData clip = clipboard.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            return null;
        }
        ClipData.Item item = clip.getItemAt(0);
        // 首先尝试通过URI获取
        Uri uri = item.getUri();
        return uri;
    }

    /**
     * 清空剪贴板内容
     *
     * @param context 上下文对象
     */
    public static void clearClipboard(@NonNull Context context) {
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        try {
            // 使用空ClipData覆盖剪贴板
            ClipData clip = ClipData.newPlainText("", "");
            clipboard.setPrimaryClip(clip);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                clipboard.clearPrimaryClip();
            }
        } catch (Exception e) {
            // 处理可能的安全异常
            e.printStackTrace();
        }
    }
}
