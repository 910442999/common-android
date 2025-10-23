package com.yuanquan.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yuanquan.common.utils.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 修复版文本中显示网络图片加载器 - 解决点击位置不准确问题
 * 修复点：
 * 1. 优化点击事件绑定机制，直接使用URL字符串
 * 2. 改进Span管理，确保点击位置准确
 * 3. 增强错误处理和日志输出
 */
public class GlideImageGetter implements Html.ImageGetter {
    private final Context mContext;
    private final TextView mTextView;
    private int mMaxWidth;
    private int mMaxHeight;
    private final Map<String, UrlDrawable> mDrawableMap = new HashMap<>();
    private OnImageClickListener mImageClickListener;

    private int mPlaceholderResId = 0;
    private int mErrorResId = 0;

    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }

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
        mContext = context.getApplicationContext();
        mTextView = textView;
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
        mImageClickListener = listener;

        if (listener != null) {
            mTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
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
            GlideImageGetter getter = new GlideImageGetter(context, textView, maxWidth, maxHeight, listener);
            if (placeholderResId != 0) {
                getter.setPlaceholderResource(placeholderResId);
            }
            if (errorResId != 0) {
                getter.setErrorResource(errorResId);
            }
            return getter;
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
            return createMinimalPlaceholder();
        }

        if (mDrawableMap.containsKey(source)) {
            UrlDrawable cachedDrawable = mDrawableMap.get(source);
            if (cachedDrawable.getDrawable() != null) {
                LogUtil.d("GlideImageGetter - 使用缓存的Drawable: " + source);
                return cachedDrawable;
            }
        }

        final UrlDrawable urlDrawable = new UrlDrawable(source);
        mDrawableMap.put(source, urlDrawable);

        configureGlideRequest(source, urlDrawable);

        return urlDrawable;
    }

    private Drawable createMinimalPlaceholder() {
        Drawable placeholder = new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
            }

            @Override
            public void setAlpha(int alpha) {
            }

            @Override
            public void setColorFilter(@Nullable android.graphics.ColorFilter colorFilter) {
            }

            @Override
            public int getOpacity() {
                return android.graphics.PixelFormat.TRANSPARENT;
            }
        };
        placeholder.setBounds(0, 0, 1, 1);
        return placeholder;
    }

    private void configureGlideRequest(String source, UrlDrawable urlDrawable) {
        LogUtil.d("GlideImageGetter - 开始加载图片: " + source);

        RequestBuilder<Drawable> requestBuilder = Glide.with(mContext)
                .asDrawable()
                .load(source);

        applyGlideConfig(requestBuilder);

        requestBuilder.into(createOptimizedTarget(urlDrawable, source));
    }

    private void applyGlideConfig(RequestBuilder<Drawable> builder) {
        if (mPlaceholderResId != 0) {
            builder.placeholder(mPlaceholderResId);
        }
        if (mErrorResId != 0) {
            builder.error(mErrorResId);
        }
        builder.dontAnimate();
//                .override(800, 600);
    }

    private CustomTarget<Drawable> createOptimizedTarget(UrlDrawable urlDrawable, String imageUrl) {
        return new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                LogUtil.d("GlideImageGetter - 图片加载成功: " + imageUrl);
                handleImageLoaded(resource, urlDrawable, imageUrl);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                LogUtil.e("GlideImageGetter - 图片加载失败: " + imageUrl);
                if (errorDrawable != null) {
                    handleImageLoaded(errorDrawable, urlDrawable, imageUrl);
                } else {
                    handleImageLoaded(createMinimalPlaceholder(), urlDrawable, imageUrl);
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                urlDrawable.setDrawable(null);
            }
        };
    }

    private void handleImageLoaded(Drawable resource, UrlDrawable urlDrawable, String imageUrl) {
        mTextView.post(() -> {
            try {
                int[] dimensions = calculateImageDimensions(resource);
                int width = dimensions[0];
                int height = dimensions[1];

                LogUtil.d("GlideImageGetter - 设置图片尺寸: " + width + "x" + height);

                resource.setBounds(0, 0, width, height);
                urlDrawable.setDrawable(resource);
                urlDrawable.setBounds(0, 0, width, height);

                applyClickableSpans();

                LogUtil.i("GlideImageGetter - 图片处理完成: " + imageUrl + ", 尺寸: " + width + "x" + height);
            } catch (Exception e) {
                LogUtil.e("GlideImageGetter - handleImageLoaded error: " + e.getMessage());
                urlDrawable.setDrawable(createMinimalPlaceholder());
                applyClickableSpans();
            }
        });
    }

    private int[] calculateImageDimensions(Drawable drawable) {
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();

        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            int defaultWidth = getDefaultMaxWidth();
            return new int[]{defaultWidth, (int) (defaultWidth * 0.75f)};
        }

        int maxWidth = getEffectiveMaxWidth();
        int maxHeight = getEffectiveMaxHeight();

        int finalWidth, finalHeight;

        if (maxHeight > 0 && intrinsicHeight > maxHeight) {
            finalHeight = maxHeight;
            finalWidth = (int) (intrinsicWidth * ((float) maxHeight / intrinsicHeight));

            if (maxWidth > 0 && finalWidth > maxWidth) {
                finalWidth = maxWidth;
                finalHeight = (int) (intrinsicHeight * ((float) maxWidth / intrinsicWidth));
            }
        } else {
            finalWidth = Math.min(intrinsicWidth, maxWidth > 0 ? maxWidth : intrinsicWidth);
            finalHeight = (int) (intrinsicHeight * ((float) finalWidth / intrinsicWidth));

            if (maxHeight > 0 && finalHeight > maxHeight) {
                finalHeight = maxHeight;
                finalWidth = (int) (intrinsicWidth * ((float) maxHeight / intrinsicHeight));
            }
        }

        finalWidth = Math.max(finalWidth, dpToPx(20));
        finalHeight = Math.max(finalHeight, dpToPx(20));

        return new int[]{finalWidth, finalHeight};
    }

    private int getEffectiveMaxWidth() {
        if (mMaxWidth > 0) return mMaxWidth;
        int textViewWidth = mTextView.getWidth();
        if (textViewWidth > 0) return textViewWidth;
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        return metrics.widthPixels - dpToPx(32);
    }

    private int getEffectiveMaxHeight() {
        if (mMaxHeight > 0) return mMaxHeight;
        int textViewHeight = mTextView.getHeight();
        if (textViewHeight > 0) return (int) (textViewHeight * 0.7f);
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        return (int) (metrics.heightPixels * 0.4f);
    }

    private int getDefaultMaxWidth() {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        return metrics.widthPixels - dpToPx(32);
    }

    private int dpToPx(int dp) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * 修复点：优化点击Span应用逻辑，直接使用URL字符串
     */
    private void applyClickableSpans() {
        CharSequence text = mTextView.getText();
        if (!(text instanceof Spannable)) {
            return;
        }

        Spannable spannable = (Spannable) text;

        // 移除所有旧的ClickableImageSpan
        ClickableImageSpan[] oldSpans = spannable.getSpans(0, text.length(), ClickableImageSpan.class);
        for (ClickableImageSpan oldSpan : oldSpans) {
            spannable.removeSpan(oldSpan);
        }

        // 获取所有ImageSpan并重新添加点击Span
        ImageSpan[] imageSpans = spannable.getSpans(0, text.length(), ImageSpan.class);
        for (ImageSpan imageSpan : imageSpans) {
            Drawable drawable = imageSpan.getDrawable();
            if (drawable instanceof UrlDrawable) {
                UrlDrawable urlDrawable = (UrlDrawable) drawable;
                String imageUrl = urlDrawable.getImageUrl();

                int start = spannable.getSpanStart(imageSpan);
                int end = spannable.getSpanEnd(imageSpan);
                int flags = spannable.getSpanFlags(imageSpan);

                // 修复点：直接使用URL字符串创建ClickableImageSpan
                ClickableImageSpan clickableSpan = new ClickableImageSpan(imageUrl);
                spannable.setSpan(clickableSpan, start, end, flags);

                LogUtil.d("GlideImageGetter - 添加点击Span: " + imageUrl + ", 位置: " + start + "-" + end);
            }
        }

        mTextView.setText(spannable);
    }

    public void setHtmlText(String htmlText) {
        if (TextUtils.isEmpty(htmlText)) {
            mTextView.setText("");
            return;
        }

        Spanned spanned = Html.fromHtml(htmlText, this, null);
        mTextView.setText(spanned);
        applyClickableSpans();
    }

    public void addImage(String imageUrl, String altText) {
        CharSequence currentText = mTextView.getText();
        SpannableStringBuilder builder = currentText instanceof SpannableStringBuilder
                ? (SpannableStringBuilder) currentText
                : new SpannableStringBuilder(currentText);

        if (builder.length() > 0) builder.append("\n");

        String imagePlaceholder = "[图片]";
        int start = builder.length();
        builder.append(imagePlaceholder);
        int end = builder.length();

        final UrlDrawable urlDrawable = new UrlDrawable(imageUrl);
        mDrawableMap.put(imageUrl, urlDrawable);

        CenteredImageSpan imageSpan = new CenteredImageSpan(urlDrawable);
        builder.setSpan(imageSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 修复点：直接使用URL字符串创建ClickableImageSpan
        ClickableImageSpan clickableSpan = new ClickableImageSpan(imageUrl);
        builder.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mTextView.setText(builder);
        mTextView.setMovementMethod(LinkMovementMethod.getInstance());

        configureGlideRequest(imageUrl, urlDrawable);
    }

    public void clear() {
        for (UrlDrawable drawable : mDrawableMap.values()) {
            drawable.setDrawable(null);
        }
        mDrawableMap.clear();
    }

    public void preloadImage(String imageUrl) {
        RequestBuilder<Drawable> requestBuilder = Glide.with(mContext)
                .asDrawable()
                .load(imageUrl);
        applyGlideConfig(requestBuilder);
        requestBuilder.preload();
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
            return drawable;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        @Override
        public void setAlpha(int alpha) {
            if (drawable != null) drawable.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable android.graphics.ColorFilter colorFilter) {
            if (drawable != null) drawable.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return drawable != null ? drawable.getOpacity() : android.graphics.PixelFormat.TRANSPARENT;
        }
    }

    /**
     * 修复点：ClickableImageSpan直接存储URL字符串，避免引用问题
     */
    private class ClickableImageSpan extends android.text.style.ClickableSpan {
        private final String imageUrl;

        public ClickableImageSpan(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        @Override
        public void onClick(View widget) {
            if (mImageClickListener != null) {
                LogUtil.d("GlideImageGetter - 图片点击: " + imageUrl);
                mImageClickListener.onImageClick(imageUrl);
            }
        }
    }

    private static class CenteredImageSpan extends ImageSpan {
        public CenteredImageSpan(Drawable drawable) {
            super(drawable);
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            Drawable drawable = getDrawable();
            Rect bounds = drawable.getBounds();
            if (fm != null) {
                Paint.FontMetricsInt pfm = paint.getFontMetricsInt();
                if (fm.ascent == 0) fm.ascent = pfm.ascent;
                if (fm.descent == 0) fm.descent = pfm.descent;
                if (fm.top == 0) fm.top = pfm.top;
                if (fm.bottom == 0) fm.bottom = pfm.bottom;
            }
            return bounds.right;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end,
                         float x, int top, int y, int bottom, Paint paint) {
            Drawable drawable = getDrawable();
            canvas.save();
            int transY = bottom - drawable.getBounds().bottom;
            transY -= paint.getFontMetricsInt().descent / 2;
            canvas.translate(x, transY);
            drawable.draw(canvas);
            canvas.restore();
        }
    }
}
