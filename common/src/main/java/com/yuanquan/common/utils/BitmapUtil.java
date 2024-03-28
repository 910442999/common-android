package com.yuanquan.common.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.text.TextUtils;
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
