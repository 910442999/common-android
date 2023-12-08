package com.yuanquan.common.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;

import java.security.MessageDigest;

public class GrayCircleTransformation extends BitmapTransformation {

    private int BORDER_COLOR = Color.TRANSPARENT;
    private int BORDER_WIDTH;

    public GrayCircleTransformation() {
        super();
    }

    public GrayCircleTransformation(int borderWidth, int borderColor) {
        super();
        BORDER_WIDTH = borderWidth;
        BORDER_COLOR = borderColor;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap squaredBitmap = TransformationUtils.centerCrop(pool, toTransform, outWidth, outHeight);
        Bitmap grayBitmap = toGrayscale(squaredBitmap);

        Bitmap circleBitmap = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888);
        if (circleBitmap == null) {
            circleBitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(circleBitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(grayBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float radius = Math.min(outWidth, outHeight) / 2f;
        canvas.drawCircle(outWidth / 2f, outHeight / 2f, radius, paint);

        if (BORDER_WIDTH > 0) {
            // 绘制边框
            Paint borderPaint = new Paint();
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(BORDER_WIDTH);
            borderPaint.setColor(BORDER_COLOR);
            canvas.drawCircle(outWidth / 2f, outHeight / 2f, radius - BORDER_WIDTH / 2f, borderPaint);
        }
        return circleBitmap;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update("gray_circle_transformation".getBytes());
    }

    private Bitmap toGrayscale(Bitmap bitmap) {
        Bitmap grayBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(grayBitmap);

        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);

        canvas.drawBitmap(bitmap, 0, 0, paint);

        return grayBitmap;
    }
}