package com.yuanquan.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yuanquan.common.utils.LogUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GlideImageGetter implements Html.ImageGetter {
    private final Context mContext;
    private final TextView mTextView;
    private int mMaxWidth;
    private int mMaxHeight;
    private final Map<String, UrlDrawable> mDrawableMap;
    private OnImageClickListener mImageClickListener;
    private int mPlaceholderResId;
    private int mErrorResId;

    // 核心修复：限制图片最大像素，彻底解决绘制过大
    private static final int MAX_IMAGE_SIZE_PX = 2000;

    public GlideImageGetter(Context context, TextView textView) {
        this(context, textView, 0, 0, null);
    }

    public GlideImageGetter(Context context, TextView textView, OnImageClickListener listener) {
        this(context, textView, 0, 0, listener);
    }

    public GlideImageGetter(Context context, TextView textView, int maxWidth, OnImageClickListener listener) {
        this(context, textView, maxWidth, 0, listener);
    }

    public GlideImageGetter(Context context, TextView textView, int maxWidth, int maxHeight, OnImageClickListener listener) {
        this.mDrawableMap = new HashMap();
        this.mPlaceholderResId = 0;
        this.mErrorResId = 0;
        this.mContext = context.getApplicationContext();
        this.mTextView = textView;
        this.mMaxWidth = maxWidth;
        this.mMaxHeight = maxHeight;
        this.mImageClickListener = listener;
        if (listener != null) {
            this.mTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public void setPlaceholderResource(@DrawableRes int resId) {
        this.mPlaceholderResId = resId;
    }

    public void setErrorResource(@DrawableRes int resId) {
        this.mErrorResId = resId;
    }

    @Override
    public Drawable getDrawable(String source) {
        if (TextUtils.isEmpty(source)) {
            LogUtil.e("GlideImageGetter - 图片URL为空");
            return this.createMinimalPlaceholder();
        } else {
            if (this.mDrawableMap.containsKey(source)) {
                UrlDrawable cachedDrawable = this.mDrawableMap.get(source);
                if (cachedDrawable.getDrawable() != null) {
                    LogUtil.d("GlideImageGetter - 使用缓存的Drawable: " + source);
                    return cachedDrawable;
                }
            }

            UrlDrawable urlDrawable = new UrlDrawable(source);
            this.mDrawableMap.put(source, urlDrawable);
            this.configureGlideRequest(source, urlDrawable);
            return urlDrawable;
        }
    }

    private Drawable createMinimalPlaceholder() {
        Drawable placeholder = new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {}
            @Override
            public void setAlpha(int alpha) {}
            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) {}
            @Override
            public int getOpacity() {
                return -2;
            }
        };
        placeholder.setBounds(0, 0, 1, 1);
        return placeholder;
    }

    private void configureGlideRequest(String source, UrlDrawable urlDrawable) {
        LogUtil.d("GlideImageGetter - 开始加载图片: " + source);
        boolean isGif = isGifSource(source);

        // 修复核心：强制限制图片大小，防止超大Bitmap
        RequestBuilder<Drawable> requestBuilder = Glide.with(this.mContext)
                .asDrawable()
                .load(source)
                .override(MAX_IMAGE_SIZE_PX, MAX_IMAGE_SIZE_PX)
                .centerInside()
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        this.applyGlideConfig(requestBuilder, isGif);
        requestBuilder.into(this.createOptimizedTarget(urlDrawable, source));
    }

    private void applyGlideConfig(RequestBuilder<Drawable> builder, boolean isGif) {
        if (this.mPlaceholderResId != 0) {
            builder.placeholder(this.mPlaceholderResId);
        }
        if (this.mErrorResId != 0) {
            builder.error(this.mErrorResId);
        }
        if (!isGif) {
            builder.dontAnimate();
        }
    }

    private boolean isGifSource(String source) {
        if (TextUtils.isEmpty(source)) {
            return false;
        }
        String normalized = source.toLowerCase(Locale.ROOT);
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }
        int hashIndex = normalized.indexOf('#');
        if (hashIndex >= 0) {
            normalized = normalized.substring(0, hashIndex);
        }
        return normalized.endsWith(".gif");
    }

    private CustomTarget<Drawable> createOptimizedTarget(final UrlDrawable urlDrawable, final String imageUrl) {
        return new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                LogUtil.d("GlideImageGetter - 图片加载成功: " + imageUrl);
                GlideImageGetter.this.handleImageLoaded(resource, urlDrawable, imageUrl);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                LogUtil.e("GlideImageGetter - 图片加载失败: " + imageUrl);
                if (errorDrawable != null) {
                    GlideImageGetter.this.handleImageLoaded(errorDrawable, urlDrawable, imageUrl);
                } else {
                    GlideImageGetter.this.handleImageLoaded(GlideImageGetter.this.createMinimalPlaceholder(), urlDrawable, imageUrl);
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                urlDrawable.setDrawable(null);
            }
        };
    }

    private void handleImageLoaded(Drawable resource, UrlDrawable urlDrawable, String imageUrl) {
        this.mTextView.post(() -> {
            try {
                int[] dimensions = this.calculateImageDimensions(resource);
                int width = dimensions[0];
                int height = dimensions[1];
                LogUtil.d("GlideImageGetter - 设置图片尺寸: " + width + "x" + height);
                resource.setBounds(0, 0, width, height);
                urlDrawable.setDrawable(resource);
                urlDrawable.setBounds(0, 0, width, height);
                this.applyClickableSpans();
                LogUtil.i("GlideImageGetter - 图片处理完成: " + imageUrl + ", 尺寸: " + width + "x" + height);
            } catch (Exception e) {
                LogUtil.e("GlideImageGetter - handleImageLoaded error: " + e.getMessage());
                urlDrawable.setDrawable(this.createMinimalPlaceholder());
                this.applyClickableSpans();
            }
        });
    }

    private int[] calculateImageDimensions(Drawable drawable) {
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        if (intrinsicWidth > 0 && intrinsicHeight > 0) {
            int maxWidth = this.getEffectiveMaxWidth();
            int maxHeight = this.getEffectiveMaxHeight();
            int finalWidth;
            int finalHeight;

            if (maxHeight > 0 && intrinsicHeight > maxHeight) {
                finalHeight = maxHeight;
                finalWidth = (int)((float)intrinsicWidth * ((float)maxHeight / intrinsicHeight));
                if (maxWidth > 0 && finalWidth > maxWidth) {
                    finalWidth = maxWidth;
                    finalHeight = (int)((float)intrinsicHeight * ((float)maxWidth / intrinsicWidth));
                }
            } else {
                finalWidth = Math.min(intrinsicWidth, maxWidth > 0 ? maxWidth : intrinsicWidth);
                finalHeight = (int)((float)intrinsicHeight * ((float)finalWidth / intrinsicWidth));
                if (maxHeight > 0 && finalHeight > maxHeight) {
                    finalHeight = maxHeight;
                    finalWidth = (int)((float)intrinsicWidth * ((float)maxHeight / intrinsicHeight));
                }
            }

            finalWidth = Math.max(finalWidth, this.dpToPx(20));
            finalHeight = Math.max(finalHeight, this.dpToPx(20));
            return new int[]{finalWidth, finalHeight};
        } else {
            int defaultWidth = this.getDefaultMaxWidth();
            return new int[]{defaultWidth, (int)((float)defaultWidth * 0.75F)};
        }
    }

    private int getEffectiveMaxWidth() {
        if (this.mMaxWidth > 0) {
            return this.mMaxWidth;
        } else {
            int textViewWidth = this.mTextView.getWidth();
            if (textViewWidth > 0) {
                return textViewWidth;
            } else {
                DisplayMetrics metrics = this.mContext.getResources().getDisplayMetrics();
                return metrics.widthPixels - this.dpToPx(32);
            }
        }
    }

    private int getEffectiveMaxHeight() {
        if (this.mMaxHeight > 0) {
            return this.mMaxHeight;
        } else {
            int textViewHeight = this.mTextView.getHeight();
            if (textViewHeight > 0) {
                return (int)((float)textViewHeight * 0.7F);
            } else {
                DisplayMetrics metrics = this.mContext.getResources().getDisplayMetrics();
                return (int)((float)metrics.heightPixels * 0.4F);
            }
        }
    }

    private int getDefaultMaxWidth() {
        DisplayMetrics metrics = this.mContext.getResources().getDisplayMetrics();
        return metrics.widthPixels - this.dpToPx(32);
    }

    private int dpToPx(int dp) {
        float density = this.mContext.getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }

    private void applyClickableSpans() {
        CharSequence text = this.mTextView.getText();
        if (text instanceof Spannable) {
            Spannable spannable = (Spannable) text;
            ClickableImageSpan[] oldSpans = spannable.getSpans(0, text.length(), ClickableImageSpan.class);
            for (ClickableImageSpan oldSpan : oldSpans) {
                spannable.removeSpan(oldSpan);
            }

            ImageSpan[] imageSpans = spannable.getSpans(0, text.length(), ImageSpan.class);
            for (ImageSpan imageSpan : imageSpans) {
                Drawable drawable = imageSpan.getDrawable();
                if (drawable instanceof UrlDrawable) {
                    UrlDrawable urlDrawable = (UrlDrawable) drawable;
                    String imageUrl = urlDrawable.getImageUrl();
                    int start = spannable.getSpanStart(imageSpan);
                    int end = spannable.getSpanEnd(imageSpan);
                    int flags = spannable.getSpanFlags(imageSpan);
                    ClickableImageSpan clickableSpan = new ClickableImageSpan(imageUrl);
                    spannable.setSpan(clickableSpan, start, end, flags);
                    LogUtil.d("GlideImageGetter - 添加点击Span: " + imageUrl + ", 位置: " + start + "-" + end);
                }
            }
            this.mTextView.setText(spannable);
        }
    }

    public void setHtmlText(String htmlText) {
        if (TextUtils.isEmpty(htmlText)) {
            this.mTextView.setText("");
        } else {
            Spanned spanned = Html.fromHtml(htmlText, this, null);
            this.mTextView.setText(spanned);
            this.applyClickableSpans();
        }
    }

    public void addImage(String imageUrl, String altText) {
        CharSequence currentText = this.mTextView.getText();
        SpannableStringBuilder builder = currentText instanceof SpannableStringBuilder ?
                (SpannableStringBuilder) currentText : new SpannableStringBuilder(currentText);
        if (builder.length() > 0) {
            builder.append("\n");
        }
        String imagePlaceholder = "[图片]";
        int start = builder.length();
        builder.append(imagePlaceholder);
        int end = builder.length();
        UrlDrawable urlDrawable = new UrlDrawable(imageUrl);
        this.mDrawableMap.put(imageUrl, urlDrawable);
        CenteredImageSpan imageSpan = new CenteredImageSpan(urlDrawable);
        builder.setSpan(imageSpan, start, end, 33);
        ClickableImageSpan clickableSpan = new ClickableImageSpan(imageUrl);
        builder.setSpan(clickableSpan, start, end, 33);
        this.mTextView.setText(builder);
        this.mTextView.setMovementMethod(LinkMovementMethod.getInstance());
        this.configureGlideRequest(imageUrl, urlDrawable);
    }

    public void clear() {
        for (UrlDrawable drawable : this.mDrawableMap.values()) {
            drawable.setDrawable(null);
        }
        this.mDrawableMap.clear();
    }

    public void preloadImage(String imageUrl) {
        RequestBuilder<Drawable> requestBuilder = Glide.with(this.mContext)
                .asDrawable()
                .load(imageUrl)
                .override(MAX_IMAGE_SIZE_PX, MAX_IMAGE_SIZE_PX)
                .centerInside();
        this.applyGlideConfig(requestBuilder);
        requestBuilder.preload();
    }

    public static class Builder {
        private final Context context;
        private final TextView textView;
        private int maxWidth;
        private int maxHeight;
        private OnImageClickListener listener;
        private int placeholderResId = 0;
        private int errorResId = 0;

        public Builder(Context context, TextView textView) {
            this.context = context;
            this.textView = textView;
        }

        public Builder setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        public Builder setMaxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }

        public Builder setOnImageClickListener(OnImageClickListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setPlaceholderResource(@DrawableRes int resId) {
            this.placeholderResId = resId;
            return this;
        }

        public Builder setErrorResource(@DrawableRes int resId) {
            this.errorResId = resId;
            return this;
        }

        public GlideImageGetter build() {
            GlideImageGetter getter = new GlideImageGetter(this.context, this.textView, this.maxWidth, this.maxHeight, this.listener);
            if (this.placeholderResId != 0) {
                getter.setPlaceholderResource(this.placeholderResId);
            }
            if (this.errorResId != 0) {
                getter.setErrorResource(this.errorResId);
            }
            return getter;
        }
    }

    private static class UrlDrawable extends Drawable {
        private Drawable drawable;
        private final String imageUrl;

        public UrlDrawable(String url) {
            this.imageUrl = url;
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

        public Drawable getDrawable() {
            return this.drawable;
        }

        public String getImageUrl() {
            return this.imageUrl;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (this.drawable != null) {
                this.drawable.draw(canvas);
            }
        }

        @Override
        public void setAlpha(int alpha) {
            if (this.drawable != null) {
                this.drawable.setAlpha(alpha);
            }
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            if (this.drawable != null) {
                this.drawable.setColorFilter(colorFilter);
            }
        }

        @Override
        public int getOpacity() {
            return this.drawable != null ? this.drawable.getOpacity() : -2;
        }
    }

    private class ClickableImageSpan extends ClickableSpan {
        private final String imageUrl;

        public ClickableImageSpan(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        @Override
        public void onClick(View widget) {
            if (GlideImageGetter.this.mImageClickListener != null) {
                LogUtil.d("GlideImageGetter - 图片点击: " + this.imageUrl);
                GlideImageGetter.this.mImageClickListener.onImageClick(this.imageUrl);
            }
        }
    }

    private static class CenteredImageSpan extends ImageSpan {
        public CenteredImageSpan(Drawable drawable) {
            super(drawable);
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            Drawable drawable = this.getDrawable();
            Rect bounds = drawable.getBounds();
            if (fm != null) {
                Paint.FontMetricsInt pfm = paint.getFontMetricsInt();
                fm.ascent = pfm.ascent;
                fm.descent = pfm.descent;
                fm.top = pfm.top;
                fm.bottom = pfm.bottom;
            }
            return bounds.right;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            Drawable drawable = this.getDrawable();
            canvas.save();
            int transY = bottom - drawable.getBounds().bottom;
            transY -= paint.getFontMetricsInt().descent / 2;
            canvas.translate(x, transY);
            drawable.draw(canvas);
            canvas.restore();
        }
    }

    public interface OnImageClickListener {
        void onImageClick(String var1);
    }
}
