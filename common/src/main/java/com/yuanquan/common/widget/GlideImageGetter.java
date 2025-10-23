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
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yuanquan.common.utils.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 增强版文本中显示网络图片加载器 - 支持自定义占位图和错误图片
 * 功能特性：
 * 1. 支持自定义占位图和错误图片
 * 2. 支持最大宽度和最大高度限制
 * 3. 完整的图片点击事件支持
 * 4. 智能尺寸计算，保持宽高比
 */
public class GlideImageGetter implements Html.ImageGetter {
    private final Context mContext;
    private final TextView mTextView;
    private int mMaxWidth;
    private int mMaxHeight;
    private final Map<String, UrlDrawable> mDrawableMap = new HashMap<>();
    private OnImageClickListener mImageClickListener;

    // 自定义占位图和错误图片
    private Drawable mCustomPlaceholderDrawable;
    private Drawable mCustomErrorDrawable;
    private int mPlaceholderResId = 0;
    private int mErrorResId = 0;
    private int mPlaceholderSize = 40; // dp
    private int mErrorSize = 40; // dp

    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }

    // 构造方法
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

    // Builder模式，用于链式配置
    public static class Builder {
        private final Context context;
        private final TextView textView;
        private int maxWidth;
        private int maxHeight;
        private OnImageClickListener listener;
        private Drawable placeholderDrawable;
        private Drawable errorDrawable;
        private int placeholderResId = -1;
        private int errorResId = -1;
        private int placeholderSize = 40;
        private int errorSize = 40;

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

        public Builder setPlaceholderDrawable(Drawable placeholderDrawable) {
            this.placeholderDrawable = placeholderDrawable;
            return this;
        }

        public Builder setErrorDrawable(Drawable errorDrawable) {
            this.errorDrawable = errorDrawable;
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

        public Builder setPlaceholderSize(int sizeInDp) {
            this.placeholderSize = sizeInDp;
            return this;
        }

        public Builder setErrorSize(int sizeInDp) {
            this.errorSize = sizeInDp;
            return this;
        }

        public GlideImageGetter build() {
            GlideImageGetter getter = new GlideImageGetter(context, textView, maxWidth, maxHeight, listener);
            if (placeholderDrawable != null) {
                getter.setCustomPlaceholderDrawable(placeholderDrawable);
            }
            if (errorDrawable != null) {
                getter.setCustomErrorDrawable(errorDrawable);
            }
            if (placeholderResId != -1) {
                getter.setPlaceholderResource(placeholderResId);
            }
            if (errorResId != -1) {
                getter.setErrorResource(errorResId);
            }
            getter.setPlaceholderSize(placeholderSize);
            getter.setErrorSize(errorSize);
            return getter;
        }
    }

    // 设置自定义占位图
    public void setCustomPlaceholderDrawable(Drawable placeholderDrawable) {
        this.mCustomPlaceholderDrawable = placeholderDrawable;
    }

    // 设置自定义错误图片
    public void setCustomErrorDrawable(Drawable errorDrawable) {
        this.mCustomErrorDrawable = errorDrawable;
    }

    // 设置占位图资源ID
    public void setPlaceholderResource(@DrawableRes int resId) {
        this.mPlaceholderResId = resId;
    }

    // 设置错误图片资源ID
    public void setErrorResource(@DrawableRes int resId) {
        this.mErrorResId = resId;
    }

    // 设置占位图尺寸
    public void setPlaceholderSize(int sizeInDp) {
        this.mPlaceholderSize = sizeInDp;
    }

    // 设置错误图片尺寸
    public void setErrorSize(int sizeInDp) {
        this.mErrorSize = sizeInDp;
    }
    @Override
    public Drawable getDrawable(String source) {
        if (TextUtils.isEmpty(source)) {
            return createPlaceholderDrawable();
        }

        if (mDrawableMap.containsKey(source)) {
            return mDrawableMap.get(source);
        }

        final UrlDrawable urlDrawable = new UrlDrawable(source);
        mDrawableMap.put(source, urlDrawable);

        // 优化后的Glide配置方式
        configureGlideRequest(source, urlDrawable);

        return urlDrawable;
    }

    /**
     * 优化后的Glide请求配置方法
     */
    private void configureGlideRequest(String source, UrlDrawable urlDrawable) {
        // 创建基础请求构建器
        RequestBuilder<Drawable> requestBuilder = Glide.with(mContext)
                .asDrawable()
                .load(source);

        // 应用占位符配置（优先级：自定义Drawable > 资源ID）
        applyPlaceholderConfig(requestBuilder);

        // 应用错误图片配置（优先级：自定义Drawable > 资源ID）
        applyErrorConfig(requestBuilder);

        // 可选：添加其他Glide配置
        applyAdditionalConfig(requestBuilder);

        // 执行请求
        requestBuilder.into(createCustomTarget(urlDrawable, source));
    }

    /**
     * 应用占位符配置
     */
    private void applyPlaceholderConfig(RequestBuilder<Drawable> builder) {
        // 优先级1：自定义Drawable
        if (mCustomPlaceholderDrawable != null) {
            builder.placeholder(mCustomPlaceholderDrawable);
            return;
        }

        // 优先级2：资源ID
        if (mPlaceholderResId != 0) {
            builder.placeholder(mPlaceholderResId);
            return;
        }

        // 优先级3：使用默认占位符（可选）
        // 这里不设置，让Glide使用默认行为
    }

    /**
     * 应用错误图片配置
     */
    private void applyErrorConfig(RequestBuilder<Drawable> builder) {
        // 优先级1：自定义Drawable
        if (mCustomErrorDrawable != null) {
            builder.error(mCustomErrorDrawable);
            return;
        }

        // 优先级2：资源ID
        if (mErrorResId != 0) {
            builder.error(mErrorResId);
            return;
        }

        // 优先级3：使用默认错误图片（可选）
    }

    /**
     * 应用其他Glide配置（可选）
     */
    private void applyAdditionalConfig(RequestBuilder<Drawable> builder) {
        // 示例：添加通用的Glide配置
        builder
                .dontAnimate(); // 禁用动画以获得更快的加载
//                .override(400, 300); // 可选：限制图片尺寸
//                .centerCrop(); // 可选：图片裁剪方式
    }

    /**
     * 创建自定义Target - 更优雅的实现
     */
    private CustomTarget<Drawable> createCustomTarget(UrlDrawable urlDrawable, String imageUrl) {
        return new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                handleImageLoaded(resource, urlDrawable, imageUrl);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                // 使用Glide返回的错误图片，或者回退到自定义错误处理
//                if (errorDrawable != null) {
//                    // 使用Glide提供的错误图片
//                    handleImageLoaded(errorDrawable, urlDrawable, imageUrl);
//                } else {
                    // 回退到自定义错误处理
                    handleImageLoadFailed(urlDrawable, imageUrl);
//                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                urlDrawable.setDrawable(null);
            }
        };
    }

    /**
     * 增强的图片加载完成处理
     */
    private void handleImageLoaded(Drawable resource, UrlDrawable urlDrawable, String imageUrl) {
        mTextView.post(() -> {
            try {
                // 检查是否为错误图片
                boolean isErrorImage = isErrorDrawable(resource);

                int[] dimensions = calculateImageDimensions(resource);
                int width = dimensions[0];
                int height = dimensions[1];

                resource.setBounds(0, 0, width, height);
                urlDrawable.setDrawable(resource);
                urlDrawable.setBounds(0, 0, width, height);
                urlDrawable.setIsError(isErrorImage); // 标记是否为错误状态

                applyClickableSpans();

                if (isErrorImage) {
                    LogUtil.e("GlideImageGetter - 图片加载失败，显示错误图片: " + imageUrl);
                } else {
                    LogUtil.i("GlideImageGetter - 图片加载成功: " + imageUrl +
                            ", 尺寸: " + width + "x" + height);
                }
            } catch (Exception e) {
                LogUtil.e("GlideImageGetter - handleImageLoaded error: " + e.getMessage());
                handleImageLoadFailed(urlDrawable, imageUrl);
            }
        });
    }

    /**
     * 判断是否为错误图片（简单实现）
     */
    private boolean isErrorDrawable(Drawable drawable) {
        // 这里可以根据实际需求实现更复杂的判断逻辑
        // 例如：比较Drawable的固有属性或标记
        return drawable == mCustomErrorDrawable ||
                (mErrorResId != 0 && isDrawableFromResource(drawable, mErrorResId));
    }

    /**
     * 判断Drawable是否来自指定资源（简化实现）
     */
    private boolean isDrawableFromResource(Drawable drawable, @DrawableRes int resId) {
        // 实际项目中可能需要更复杂的判断逻辑
        // 这里返回false，在实际使用中可以根据需求完善
        return false;
    }

    /**
     * 创建带有配置的Glide请求（公共方法，可供外部调用）
     */
    public RequestBuilder<Drawable> createGlideRequest(String imageUrl) {
        RequestBuilder<Drawable> builder = Glide.with(mContext)
                .asDrawable()
                .load(imageUrl);

        applyPlaceholderConfig(builder);
        applyErrorConfig(builder);
        applyAdditionalConfig(builder);

        return builder;
    }

    /**
     * 预加载图片（性能优化）
     */
    public void preloadImage(String imageUrl) {
        createGlideRequest(imageUrl).preload();
    }

    /**
     * 清除Glide缓存（可选）
     */
    public void clearGlideCache() {
        new Thread(() -> {
            try {
                Glide.get(mContext).clearDiskCache();
                mTextView.post(() -> Glide.get(mContext).clearMemory());
            } catch (Exception e) {
                LogUtil.e("GlideImageGetter - 清除缓存失败: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 计算图片显示尺寸
     */
    private int[] calculateImageDimensions(Drawable drawable) {
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();

        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            int defaultWidth = getDefaultMaxWidth();
            return new int[]{defaultWidth, (int) (defaultWidth * 0.75f)};
        }

        int maxWidth = getEffectiveMaxWidth();
        int maxHeight = getEffectiveMaxHeight();

//        float widthRatio = (float) maxWidth / intrinsicWidth;
//        float heightRatio = (float) maxHeight / intrinsicHeight;

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

    /**
     * 创建占位图
     */
    private Drawable createPlaceholderDrawable() {
        // 优先级：自定义Drawable > 资源ID > 默认占位图
        if (mCustomPlaceholderDrawable != null) {
            Drawable placeholder = mCustomPlaceholderDrawable.mutate();
            int size = dpToPx(mPlaceholderSize);
            placeholder.setBounds(0, 0, size, size);
            return placeholder;
        }

        if (mPlaceholderResId != 0) {
            try {
                Drawable placeholder = ContextCompat.getDrawable(mContext, mPlaceholderResId);
                if (placeholder != null) {
                    int size = dpToPx(mPlaceholderSize);
                    placeholder.setBounds(0, 0, size, size);
                    return placeholder;
                }
            } catch (Exception e) {
                LogUtil.e("GlideImageGetter - 加载占位图资源失败: " + e.getMessage());
            }
        }

        // 默认占位图
        Drawable placeholder = ContextCompat.getDrawable(mContext, android.R.drawable.ic_menu_gallery);
        if (placeholder == null) {
            // 备用方案
            placeholder = createColorDrawable(0xFFE0E0E0);
        }
        int size = dpToPx(mPlaceholderSize);
        placeholder.setBounds(0, 0, size, size);
        return placeholder;
    }

    /**
     * 创建错误图片
     */
    private Drawable createErrorDrawable() {
        // 优先级：自定义Drawable > 资源ID > 默认错误图
        if (mCustomErrorDrawable != null) {
            Drawable error = mCustomErrorDrawable.mutate();
            int size = dpToPx(mErrorSize);
            error.setBounds(0, 0, size, size);
            return error;
        }

        if (mErrorResId != 0) {
            try {
                Drawable error = ContextCompat.getDrawable(mContext, mErrorResId);
                if (error != null) {
                    int size = dpToPx(mErrorSize);
                    error.setBounds(0, 0, size, size);
                    return error;
                }
            } catch (Exception e) {
                LogUtil.e("GlideImageGetter - 加载错误图资源失败: " + e.getMessage());
            }
        }

        // 默认错误图
        Drawable error = ContextCompat.getDrawable(mContext, android.R.drawable.ic_menu_report_image);
        if (error == null) {
            // 备用方案
            error = createColorDrawable(0xFFF44336);
        }
        int size = dpToPx(mErrorSize);
        error.setBounds(0, 0, size, size);
        return error;
    }

    /**
     * 创建颜色Drawable（备用）
     */
    private Drawable createColorDrawable(int color) {
        return new Drawable() {
            private final Paint paint = new Paint();

            {
                paint.setColor(color);
                paint.setStyle(Paint.Style.FILL);
            }

            @Override
            public void draw(Canvas canvas) {
                Rect bounds = getBounds();
                canvas.drawRect(bounds, paint);
            }

            @Override
            public void setAlpha(int alpha) {
                paint.setAlpha(alpha);
            }

            @Override
            public void setColorFilter(android.graphics.ColorFilter colorFilter) {
                paint.setColorFilter(colorFilter);
            }

            @Override
            public int getOpacity() {
                return android.graphics.PixelFormat.TRANSLUCENT;
            }
        };
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

    /**
     * 应用可点击的Span到所有图片
     */
    private void applyClickableSpans() {
        CharSequence text = mTextView.getText();
        if (!(text instanceof Spannable)) return;

        Spannable spannable = (Spannable) text;
        ImageSpan[] imageSpans = spannable.getSpans(0, text.length(), ImageSpan.class);

        ClickableImageSpan[] oldSpans = spannable.getSpans(0, text.length(), ClickableImageSpan.class);
        for (ClickableImageSpan oldSpan : oldSpans) {
            spannable.removeSpan(oldSpan);
        }

        for (ImageSpan imageSpan : imageSpans) {
            Drawable drawable = imageSpan.getDrawable();
            if (drawable instanceof UrlDrawable) {
                UrlDrawable urlDrawable = (UrlDrawable) drawable;
                int start = spannable.getSpanStart(imageSpan);
                int end = spannable.getSpanEnd(imageSpan);
                int flags = spannable.getSpanFlags(imageSpan);

                ClickableImageSpan clickableSpan = new ClickableImageSpan(urlDrawable);
                spannable.setSpan(clickableSpan, start, end, flags);
            }
        }

        mTextView.setText(spannable);
    }

    /**
     * 设置HTML文本内容
     */
    public void setHtmlText(String htmlText) {
        if (TextUtils.isEmpty(htmlText)) {
            mTextView.setText("");
            return;
        }

        Spanned spanned = Html.fromHtml(htmlText, this, null);
        mTextView.setText(spanned);
        applyClickableSpans();
    }

    /**
     * 动态添加图片到文本末尾
     */
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

        ClickableImageSpan clickableSpan = new ClickableImageSpan(urlDrawable);
        builder.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mTextView.setText(builder);
        mTextView.setMovementMethod(LinkMovementMethod.getInstance());

        loadImage(imageUrl, urlDrawable);
    }

    private void loadImage(String imageUrl, UrlDrawable urlDrawable) {
        Glide.with(mContext)
                .asDrawable()
                .load(imageUrl)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        handleImageLoaded(resource, urlDrawable, imageUrl);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        handleImageLoadFailed(urlDrawable, imageUrl);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    private void handleImageLoadFailed(UrlDrawable urlDrawable, String imageUrl) {
        Drawable errorDrawable = createErrorDrawable();
        int size = dpToPx(mErrorSize);
        errorDrawable.setBounds(0, 0, size, size);
        urlDrawable.setDrawable(errorDrawable);

        applyClickableSpans();
        LogUtil.e("GlideImageGetter - 图片加载失败: " + imageUrl);
    }

    private int dpToPx(int dp) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * 设置最大宽度
     */
    public void setMaxWidth(int maxWidth) {
        this.mMaxWidth = maxWidth;
    }

    /**
     * 设置最大高度
     */
    public void setMaxHeight(int maxHeight) {
        this.mMaxHeight = maxHeight;
    }

    /**
     * 清理资源
     */
    public void clear() {
        for (UrlDrawable drawable : mDrawableMap.values()) {
            drawable.setDrawable(null);
        }
        mDrawableMap.clear();
    }

    /**
     * 自定义Drawable类
     */
    private class UrlDrawable extends Drawable {
        private Drawable drawable;
        private final String imageUrl;
        private boolean isError = false;
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
        public void setIsError(boolean isError) {
            this.isError = isError;
        }

        public boolean isError() {
            return isError;
        }

        @Override
        public void draw(Canvas canvas) {
            if (drawable != null) {
                // 如果是错误状态，可以添加特殊效果
                if (isError) {
                    canvas.save();
                    // 可以添加灰度效果或其他视觉提示
                    // Paint paint = new Paint();
                    // paint.setColorFilter(new ColorMatrixColorFilter(createErrorColorMatrix()));
                    // canvas.drawBitmap(bitmap, 0, 0, paint);
                    // canvas.restore();
                }
                drawable.draw(canvas);
            }
        }

        @Override
        public void setAlpha(int alpha) {
            if (drawable != null) drawable.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(android.graphics.ColorFilter colorFilter) {
            if (drawable != null) drawable.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return drawable != null ? drawable.getOpacity() : android.graphics.PixelFormat.TRANSPARENT;
        }
    }

    /**
     * 可点击的ImageSpan
     */
    private class ClickableImageSpan extends android.text.style.ClickableSpan {
        private final UrlDrawable urlDrawable;

        public ClickableImageSpan(UrlDrawable drawable) {
            this.urlDrawable = drawable;
        }

        @Override
        public void onClick(View widget) {
            if (mImageClickListener != null && urlDrawable != null) {
                mImageClickListener.onImageClick(urlDrawable.getImageUrl());
            }
        }
    }

    /**
     * 居中的ImageSpan
     */
    private class CenteredImageSpan extends ImageSpan {
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