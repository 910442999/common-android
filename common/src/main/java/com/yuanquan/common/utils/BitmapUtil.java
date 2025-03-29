package com.yuanquan.common.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.security.MessageDigest;

public class BitmapUtil {
    /**
     * 质量压缩方法
     *
     * @param image
     * @return
     */
    private static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    /**
     * 将压缩的bitmap保存到SDCard卡临时文件夹，用于上传
     *
     * @param filename
     * @param bit
     * @return
     */

    /**
     * 保存图片到指定路径(用户相册浏览)
     *
     * @param context
     * @param bitmap  要保存的图片
     * @return
     */
    public static Boolean saveBitmapToGallery(Context context, Bitmap bitmap) {
        try {
            //获取要保存的图片的位图
            //创建一个保存的Uri
            ContentValues values = new ContentValues();
            //设置图片名称
            values.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".png");
            //设置图片格式
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            //设置图片路径
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            Uri saveUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (TextUtils.isEmpty(saveUri.toString())) {
                return false;
            }
            OutputStream outputStream = context.getContentResolver().openOutputStream(saveUri);
            //将位图写出到指定的位置
            //第一个参数：格式JPEG 是可以压缩的一个格式 PNG 是一个无损的格式
            //第二个参数：保留原图像90%的品质，压缩10% 这里压缩的是存储大小
            //第三个参数：具体的输出流
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

//    public static String getLubanCompressImage(Context context, String path) {
//        File lubanCompress = getLubanCompress(context, path);
//        if (lubanCompress == null) {
//            return "";
//        }
//        return lubanCompress.getAbsolutePath();
//    }

//    public static File getLubanCompress(Context context, String path) {
//        File file = null;
//        try {
//            file = Luban.with(context).load(path).get(path);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return file;
//    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    /**
     * 递归删除
     */
    private static void RecursionDeleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                RecursionDeleteFile(f);
            }
            file.delete();
        }
    }

    public static String md5(String str) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        char[] charArray = str.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    public static Bitmap convertViewToBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //利用bitmap生成画布
        Canvas canvas = new Canvas(bitmap);
        //把view中的内容绘制在画布上
        view.draw(canvas);
        return bitmap;
    }

    public static Bitmap generateCircularBitmap(String nickname, int color, int size) {
        if (nickname == null || nickname.isEmpty()) {
            nickname = "";
        }
        String newName = nickname.substring(0, 1);
        // 创建一个正方形的 Bitmap
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        // 创建一个画布
        Canvas canvas = new Canvas(bitmap);

        // 设置画布背景颜色
        canvas.drawColor(color);

        // 创建画笔
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 设置文字的样式
        paint.setColor(Color.WHITE);
        paint.setTextSize(size * 0.5f);
        paint.setTextAlign(Paint.Align.CENTER);

        // 计算文字的宽度和高度
        float textWidth = paint.measureText(newName);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float textHeight = fontMetrics.bottom - fontMetrics.top;

        // 计算文字的位置
        float x = size / 2;
        float y = (size - textHeight) / 2 - fontMetrics.top;

        // 在画布上绘制文字
        canvas.drawText(newName, x, y, paint);

        // 创建圆形的 Bitmap
        Bitmap circularBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas circularCanvas = new Canvas(circularBitmap);

        // 创建圆形路径
        Path path = new Path();
        path.addCircle(size / 2, size / 2, size / 2, Path.Direction.CW);

        // 设置绘制区域
        circularCanvas.clipPath(path);

        // 在圆形画布上绘制原始的 Bitmap
        circularCanvas.drawBitmap(bitmap, 0, 0, null);

        // 释放资源
        bitmap.recycle();

        return circularBitmap;
    }

    /**
     * 动态调整分辨率
     */
    public static Bitmap generateCircularBitmap(Context context, String nickname, int color, int size) {
        // 获取设备屏幕密度信息
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        // 根据屏幕密度动态计算缩放系数（优化内存和清晰度平衡）
        final float scaleFactor = calculateScaleFactor(metrics);

        // 计算缩放后的绘制尺寸
        int scaledSize = (int) (size * scaleFactor);

        // --- 创建高分辨率 Bitmap 开始 ---
        Bitmap bitmap = Bitmap.createBitmap(scaledSize, scaledSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(color);

        // 文字绘制配置
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(scaledSize * 0.5f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // 精确计算文字位置
        String displayName = (nickname == null || nickname.isEmpty()) ? "" : nickname.substring(0, 1);
        Rect textBounds = new Rect();
        textPaint.getTextBounds(displayName, 0, 1, textBounds);
        float yPos = (scaledSize - textBounds.height()) / 2f - textBounds.top;

        // 绘制文字
        canvas.drawText(displayName, scaledSize / 2f, yPos, textPaint);
        // --- 创建高分辨率 Bitmap 结束 ---

        // --- 创建圆形遮罩开始 ---
        Bitmap circularBitmap = Bitmap.createBitmap(scaledSize, scaledSize, Bitmap.Config.ARGB_8888);
        Canvas circularCanvas = new Canvas(circularBitmap);

        // 绘制圆形背景
        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circularCanvas.drawCircle(scaledSize / 2f, scaledSize / 2f, scaledSize / 2f, circlePaint);

        // 应用遮罩（SRC_IN模式）
        circlePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        circularCanvas.drawBitmap(bitmap, 0, 0, circlePaint);
        // --- 创建圆形遮罩结束 ---

        // 缩放回目标尺寸（优化不同密度设备显示效果）
        Bitmap finalBitmap = Bitmap.createScaledBitmap(
                circularBitmap,
                size,
                size,
                true // 启用双线性过滤
        );

        // 回收临时Bitmap（API Level 19+ 自动管理，此处显式释放）
        bitmap.recycle();
        circularBitmap.recycle();

        return finalBitmap;
    }

    /**
     * 智能缩放系数计算策略（可根据测试结果调整阈值）
     * <p>
     * 密度分级参考：
     * ldpi    ~120dpi : 0.75x
     * mdpi    ~160dpi : 1x (baseline)
     * hdpi    ~240dpi : 1.5x
     * xhdpi   ~320dpi : 2x
     * xxhdpi  ~480dpi : 2.5x
     * xxxhdpi ~640dpi : 3x
     */
    private static float calculateScaleFactor(DisplayMetrics metrics) {
        final int densityDpi = metrics.densityDpi;
        // 根据屏幕密度设置缩放系数（系数与密度成反比）
        if (densityDpi >= DisplayMetrics.DENSITY_XXXHIGH) { // 640dpi+
            return 1.25f;   // 超高密度设备使用较小缩放
        } else if (densityDpi >= DisplayMetrics.DENSITY_XXHIGH) { // 480dpi
            return 1.5f;
        } else if (densityDpi >= DisplayMetrics.DENSITY_XHIGH) { // 320dpi
            return 1.75f;
        } else if (densityDpi >= DisplayMetrics.DENSITY_HIGH) { // 240dpi
            return 2.0f;
        } else { // 160dpi及以下
            return 2.5f;   // 低密度设备需要更大缩放
        }
    }

    public static Bitmap generateTextBitmap(String nickname, int color, int textSize, int width, int height, int left, int right, int top, int bottom) {
        // 创建 Paint 对象
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(color);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);
        if (nickname == null || nickname.isEmpty()) {
            nickname = "";
        }
        // 计算文字的宽度和高度
        float textWidth = textPaint.measureText(nickname) + left;
        String displayStr = nickname;
//        if (nickname.length() > size) {
//            while (textPaint.measureText(displayStr) >= (textWidth - textPaint.measureText("..."))) {
//                displayStr = displayStr.substring(0, size - 1);
//            }
//            displayStr += "...";
//        }

//        Rect bounds = new Rect();
//        textPaint.getTextBounds(nickname, 0, nickname.length(), bounds);
//        float textWidth1 = bounds.width();
//        Log.d("111","textWidth:"+textWidth1);

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float textHeight = fontMetrics.bottom - fontMetrics.top + top;
        // 创建 Bitmap
        Bitmap bitmap = Bitmap.createBitmap((int) textWidth, height, Bitmap.Config.ARGB_8888);
        // 创建 Canvas
        Canvas canvas = new Canvas(bitmap);
        float y = (height - textHeight) / 2;
        if (textWidth > width) {
            // 计算可以绘制的文本宽度
            float availableWidth = width - textPaint.measureText("...");
            // 计算可以绘制的字符数量
            int visibleCharCount = (int) (availableWidth / textPaint.measureText(nickname, 0, 1));
            // 绘制省略号
//            textPaint.measureText(nickname, 0, visibleCharCount)
            canvas.drawText(displayStr.substring(0, visibleCharCount) + "...", left, textHeight, textPaint);
        } else {
            // 直接绘制全文本
            canvas.drawText(displayStr, left, textHeight, textPaint);
        }

        return bitmap;
    }
}
