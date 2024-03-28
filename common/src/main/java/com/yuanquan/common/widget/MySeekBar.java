package com.yuanquan.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.yuanquan.common.R;

public class MySeekBar extends View {

    //背景颜色
    private int bgColor;
    //进度颜色
    private int progressColor;
    //当前进度
    private float progress = 10;
    //总进度
    private float maxProgress = 100;
    //当前UI高度与view高度的比例
    private double progressRate = 0;
    //记录按压时手指相对于组件view的高度
    private float downY;
    //手指移动的距离，视为亮度调整
    private float moveDistance;

    private Paint mPaint;//画笔

    //是否画亮度路标
    private boolean showSun = true;
    //太阳颜色
    private int sunColor;
    //小太阳半径
    private float circleRadius = 15;
    //矩形圆角
    private float radiusXY = 40;
    //设置是否画亮度文字
    private boolean showText = true;
    //文字位置
    private int textHeight = -1;
    //字体大写
    private float textSize = 15;
    //字体颜色
    private int textColor = Color.BLACK;
    //显示内容
    private String textContent = "";
    //是否缩放小太阳
    private boolean isSunZoom = true;

    //亮度图标margin
    private int margin = 10;

    public MySeekBar(Context context) {
        this(context,null);
    }

    public MySeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MySeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        textSize = sp2px(context,textSize);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MySeekBar);
        bgColor = typedArray.getColor(R.styleable.MySeekBar_bgColor, ContextCompat.getColor(getContext(),R.color.bg_color));
        progressColor = typedArray.getColor(R.styleable.MySeekBar_progressColor,ContextCompat.getColor(getContext(),R.color.progress_color));
        progress = typedArray.getFloat(R.styleable.MySeekBar_progress, 10);
        maxProgress = typedArray.getFloat(R.styleable.MySeekBar_maxProgress, 100);
        showSun = typedArray.getBoolean(R.styleable.MySeekBar_showSun,showSun);
        isSunZoom = typedArray.getBoolean(R.styleable.MySeekBar_zoomSun,isSunZoom);
        sunColor = typedArray.getColor(R.styleable.MySeekBar_sunColor,ContextCompat.getColor(getContext(),R.color.sun_color));
        circleRadius = typedArray.getDimension(R.styleable.MySeekBar_circleRadius, circleRadius);
        radiusXY = typedArray.getDimension(R.styleable.MySeekBar_radiusXY, radiusXY);
        showText = typedArray.getBoolean(R.styleable.MySeekBar_showText,showText);
        textColor = typedArray.getColor(R.styleable.MySeekBar_setTextColor,textColor);
        textHeight = typedArray.getInt(R.styleable.MySeekBar_setTextHeight,textHeight);
        textSize = typedArray.getDimension(R.styleable.MySeekBar_setTextSize,textSize);
        typedArray.recycle();

        initPaint();
    }

    private void initPaint() {
        //获取当前百分比率
        progressRate = getProgressRate();
        //背景画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//抗锯齿
        mPaint.setDither(true);//设置防抖动
        mPaint.setColor(bgColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(0);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTextSize(textSize);
    }

    public int getProgress(){
        return (int)progress;
    }
    public void setProgress(int mProgress){
        progress = mProgress;
        progressRate = getProgressRate();
        invalidate();
    }

    /**
     * 计算亮度比例
     */
    private double getProgressRate(){
        return (double) progress / maxProgress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //保存
        int layerId = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
        onDrawBackground(canvas); //画背景
        onDrawProgress(canvas); //画进度
        onDrawText(canvas); //画文字
        onDrawSunCircle(canvas);//画小太郎
        //恢复到特定的保存点
        canvas.restoreToCount(layerId);
    }

    /**
     * 画圆弧背景
     * @param canvas
     */
    private void onDrawBackground(Canvas canvas){
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(bgColor);
        int with = getWidth();
        int height = getHeight();

//        int  colors [] = new int[2];
//        //colors[0] = Color.parseColor("#FFB660");
//        //colors[1] = Color.parseColor("#FFA757");
//        colors[0] = setColorTempToColor("6500K");
//        colors[1] = setColorTempToColor("2700K");
//        //线性变色
//        float heightkkk = (canvas.getHeight()-(int)(canvas.getHeight() * progressRate));
//        LinearGradient linearGradient = new LinearGradient((canvas.getWidth()/2),heightkkk,(canvas.getWidth()/2),0,colors,null, Shader.TileMode.CLAMP);
//        //new float[]{},中的数据表示相对位置，将150,50,150,300，划分10个单位，.3，.6，.9表示它的绝对位置。300到400，将直接画出rgb（0,232,210）
//        mPaint.setShader(linearGradient);

        RectF rectF = new RectF(0,0,with,height);
        canvas.drawRoundRect(rectF,radiusXY,radiusXY,mPaint);
    }

    /**
     * 画亮度背景-方形-随手势上下滑动而变化用来显示亮度大小
     * @param canvas
     */
    private void onDrawProgress(Canvas canvas){
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        int with = getWidth();
        int height = getHeight();

//        int  colors [] = new int[2];
//        //colors[0] = Color.parseColor("#FFB660");
//        //colors[1] = Color.parseColor("#FFA757");
//        colors[0] = setColorTempToColor("6500K");
//        colors[1] = setColorTempToColor("2700K");
//        //线性变色
//        float heightkkk = (canvas.getHeight()-(int)(canvas.getHeight() * progressRate));
//        LinearGradient linearGradient = new LinearGradient((canvas.getWidth()/2),heightkkk,(canvas.getWidth()/2),height,colors,null, Shader.TileMode.CLAMP);
//        //new float[]{},中的数据表示相对位置，将150,50,150,300，划分10个单位，.3，.6，.9表示它的绝对位置。300到400，将直接画出rgb（0,232,210）
//        mPaint.setShader(linearGradient);

        mPaint.setColor(progressColor);

        Log.i("打印比率：","progressRate = "+progressRate);
        float progressHeight = (canvas.getHeight()-(int)(canvas.getHeight() * progressRate));
        canvas.drawRect(0,progressHeight,with,height,mPaint);
        mPaint.setXfermode(null);
    }
    /**
     * 画文字-展示当前大小
     * @param canvas
     */
    private void onDrawText(Canvas canvas){
        if(showText) { //如果开启了则开始绘制
            mPaint.setStyle(Paint.Style.FILL);
            textContent = "" + (int) (progressRate * 100);
            mPaint.setColor(textColor);
            canvas.drawText(textContent, (canvas.getWidth() / 2 - mPaint.measureText(textContent) / 2), textHeight >= 0 ? textHeight : getHeight() / 6, mPaint);
        }
    }
    /**
     * 画亮度图标-太阳圆心
     */
    private void onDrawSunCircle(Canvas canvas){
        if(showSun){ //如果开启了则开始绘制
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setStrokeWidth(4);
            mPaint.setColor(sunColor);
            if (isSunZoom){//是否缩放太阳圆点
                float circleMaxRadius = (float) (Math.sqrt(canvas.getWidth()) * 1.5);
                float circleMinRadius = (float) (Math.sqrt(canvas.getWidth()) * 1);
                //当前圆半径
                circleRadius = (float) progressRate * (circleMaxRadius - circleMinRadius) + circleMinRadius;
                canvas.drawCircle(canvas.getWidth()/2,(float) (canvas.getHeight() * 0.85 - margin), circleRadius,mPaint);
                onDrawSunRays(canvas,canvas.getWidth()/2,(float) (canvas.getHeight() * 0.85 - margin));
            }else {
                canvas.drawCircle(canvas.getWidth()/2,(float) (canvas.getHeight() * 0.85), circleRadius,mPaint);
                onDrawSunRays(canvas,canvas.getWidth()/2,(float) (canvas.getHeight() * 0.85));
            }

        }
    }

    /**
     * 画亮度图标-太阳光芒
     */
    private void onDrawSunRays(Canvas canvas,float cx,float cy){
        mPaint.setStrokeCap(Paint.Cap.ROUND); // 定义线段断电形状为圆头
        //绘制时刻度
        canvas.translate(cx,cy);
        for (int i = 0; i < 10; i++) {
            if (isSunZoom){//是否缩放
                canvas.drawLine(circleRadius, circleRadius, (float)(circleRadius + 5 * progressRate),(float)( circleRadius + 5* progressRate), mPaint);
            }else {
                canvas.drawLine(circleRadius, circleRadius, (float)(circleRadius + 5),(float)( circleRadius + 5), mPaint);
            }
            canvas.rotate(36);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveDistance = downY - event.getY();
                //计算手指移动后亮度UI占比大小
                calculateLoudRate();
                downY = event.getY();
                if (listener != null) {
                    listener.onProgressChange((int)(progressRate*100));
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        invalidate();
        return true;
    }

    /**
     * 计算手指移动后亮度UI占比大小，视其为亮度大小
     */
    private void calculateLoudRate(){
        progressRate = ( getHeight() * progressRate + moveDistance) /  getHeight();
        if(progressRate >= 1){
            progressRate = 1;
        }
        if(progressRate <= 0){
            progressRate = 0;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        margin = MeasureSpec.getSize(widthMeasureSpec)/10;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    //附加
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    //分离，拆卸
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    public int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    //进度发生变化的回调接口
    interface OnProgressChangedListener {
        void onProgressChange(int progress);
    }

    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        this.listener = listener;
    }

    //进度移动监听
    private OnProgressChangedListener listener  = null;

//    private int setColorTempToColor(String color){
//
//        String zhi = color.replace("K", "");
//        Log.i("打印色温值：",""+zhi);
//        int rgb [] = new int[3];
//        rgb = MyColorUtils.getRgbFromTemperature(Double.valueOf(zhi),false);
//        Log.i("打印色温值转颜色：","R=${rgb[0]} ,G=${rgb[1]} ,B=${rgb[2]}");
//        int c = Color.argb(255,rgb[0], rgb[1], rgb[2]);
//        Log.i("打印选择的值3","c=${c}");
//        //返回颜色
//        return c;
//    }
}