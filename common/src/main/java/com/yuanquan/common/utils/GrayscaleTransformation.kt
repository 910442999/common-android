package com.yuanquan.common.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class GrayscaleTransformation extends BitmapTransformation {

    public GrayscaleTransformation() {
        super();
    }

    @Override
    protected Bitmap transform(BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap bitmap = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(toTransform, 0, 0, paint);
        return bitmap;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update("GrayscaleTransformation".getBytes());
    }
}
