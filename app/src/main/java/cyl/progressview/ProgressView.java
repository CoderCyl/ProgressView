package cyl.progressview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by CC on 2017/8/21.
 */

public class ProgressView extends View {
    private float progressWidth; // 进度条宽度
    private Paint mBgPaint, mProgressPaint, mProPaint, mTipPaint;
    private int width, height;

    private int bgColor, progressColor, tipTextColor, proTextColor;
    private int max, current, animCurrent; // 最大值、需要绘制的进度值和显示动画时的动态递增进度值
    private float sweepAngle, totalAngle = 240;

    private int startColor, endColor; // 渐变色的开始颜色和结束颜色

    private boolean showAnim; // 是否显示动画
    private float tipTextSize, proTextSize;// 文字大小
    private String tipText;

    public ProgressView(Context context) {
        this(context, null);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);

        mBgPaint = getPaint(Paint.Style.STROKE);
        mProgressPaint = getPaint(Paint.Style.STROKE);
        mProPaint = getPaint(Paint.Style.FILL);
        mTipPaint = getPaint(Paint.Style.FILL);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ProgressView);
        progressWidth = ta.getDimension(R.styleable.ProgressView_progressWidth, 20);
        bgColor = ta.getColor(R.styleable.ProgressView_bgColor, Color.GRAY);
        progressColor = ta.getColor(R.styleable.ProgressView_progressColor, Color.YELLOW);
        max = ta.getInteger(R.styleable.ProgressView_max, 100);
        current = ta.getInteger(R.styleable.ProgressView_current, 0);
        showAnim = ta.getBoolean(R.styleable.ProgressView_showAnim, false);
        startColor = ta.getColor(R.styleable.ProgressView_startColor, 0xFE7200);
        endColor = ta.getColor(R.styleable.ProgressView_endColor, Color.YELLOW);
        tipTextColor = ta.getColor(R.styleable.ProgressView_tipTextColor, Color.YELLOW);
        tipTextSize = ta.getDimension(R.styleable.ProgressView_tipTextSize, 150);
        proTextColor = ta.getColor(R.styleable.ProgressView_proTextColor, Color.YELLOW);
        proTextSize = ta.getDimension(R.styleable.ProgressView_proTextSize, 250);
        tipText = ta.getString(R.styleable.ProgressView_tipText);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            width = 900;
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            height = width;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private Paint getPaint(Paint.Style style) {
        Paint paint = new Paint();
        paint.setStyle(style);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeCap(Paint.Cap.ROUND); // 圆角
        return paint;
    }

    // 设置画笔属性值
    private void setPaintProperty() {
        mBgPaint.setColor(bgColor);
        mProgressPaint.setColor(progressColor);

        mTipPaint.setColor(tipTextColor);
        mTipPaint.setTextSize(tipTextSize);

        mProPaint.setColor(proTextColor);
        mProPaint.setTextSize(proTextSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mBgPaint.setStrokeWidth(progressWidth);
        mProgressPaint.setStrokeWidth(progressWidth);
        setPaintProperty();

        if (!showAnim) { // 不显示动画时，绘制进度就会穿进来的进度值
            animCurrent = current;
        }
        drawBg(canvas);
        drawProgress(canvas);
        Log.i("onDraw", animCurrent + "/" + current);
        if (animCurrent < current) {
            animCurrent++;
            invalidate();
        }

        drawTipText(canvas); // 绘制提示文字
        drawProText(canvas); // 绘制进度值文字
    }

    // 绘制进度文字
    private void drawProText(Canvas canvas) {
        mProPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = mProPaint.getFontMetrics();
        float fontHeight = fontMetrics.bottom - fontMetrics.top;
        canvas.drawText(String.valueOf(current), width / 2, height / 2 - fontHeight / 4, mProPaint);
    }

    // 绘制提示文字
    private void drawTipText(Canvas canvas) {
        mTipPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = mTipPaint.getFontMetrics();
        float fontHeight = fontMetrics.bottom - fontMetrics.top;
        canvas.drawText(tipText, width / 2, 3 * height / 4 - fontHeight / 2, mTipPaint);
    }

    private void drawProgress(Canvas canvas) {
        if (animCurrent > max) animCurrent = max; // 输入值超过最大值也按最大值算
        float percent = (float) animCurrent / max;
        sweepAngle = percent * totalAngle;
        Log.i("drawProgress", sweepAngle + "/");
        for (int i = 0; i < sweepAngle; i++) {
            mProgressPaint.setColor(getGradient(i / sweepAngle, startColor, endColor));
            RectF rectF = new RectF(progressWidth / 2, progressWidth / 2, width - progressWidth / 2, height - progressWidth / 2);
            // 一次扫描一度
            canvas.drawArc(rectF, 150 + i, 1, false, mProgressPaint);
        }
    }

    private void drawBg(Canvas canvas) {
        RectF rectF = new RectF(progressWidth / 2, progressWidth / 2, width - progressWidth / 2, height - progressWidth / 2);
        canvas.drawArc(rectF, 150, totalAngle, false, mBgPaint);
    }

    public int getGradient(float fraction, int startColor, int endColor) {
        if (fraction > 1) fraction = 1;
        int alphaStart = Color.alpha(startColor);
        int redStart = Color.red(startColor);
        int blueStart = Color.blue(startColor);
        int greenStart = Color.green(startColor);
        int alphaEnd = Color.alpha(endColor);
        int redEnd = Color.red(endColor);
        int blueEnd = Color.blue(endColor);
        int greenEnd = Color.green(endColor);
        int alphaDifference = alphaEnd - alphaStart;
        int redDifference = redEnd - redStart;
        int blueDifference = blueEnd - blueStart;
        int greenDifference = greenEnd - greenStart;
        int alphaCurrent = (int) (alphaStart + fraction * alphaDifference);
        int redCurrent = (int) (redStart + fraction * redDifference);
        int blueCurrent = (int) (blueStart + fraction * blueDifference);
        int greenCurrent = (int) (greenStart + fraction * greenDifference);
        return Color.argb(alphaCurrent, redCurrent, greenCurrent, blueCurrent);
    }

    /**
     * 设置进度值
     *
     * @param progress
     * @return the ProgressView
     */
    public ProgressView setProgress(int progress) {
        current = progress;
        return this;
    }

    /**
     * 设置最大刻度值
     *
     * @param max
     * @return the ProgressView
     */
    public ProgressView setMax(int max) {
        this.max = max;
        return this;
    }

    /**
     * 设置渐变色开始颜色值
     *
     * @param startColor
     * @return the ProgressView
     */
    public ProgressView setStartColor(int startColor) {
        this.startColor = startColor;
        return this;
    }

    /**
     * 设置渐变色结束颜色值
     *
     * @param endColor
     * @return the ProgressView
     */
    public ProgressView setEndColor(int endColor) {
        this.endColor = endColor;
        return this;
    }

    /**
     * 设置进度条宽度
     *
     * @param progressWidth
     * @return the ProgressView
     */
    public ProgressView setProgressWidth(float progressWidth) {
        this.progressWidth = progressWidth;
        return this;
    }

    /**
     * 设置进度条背景色
     *
     * @param bgColor
     * @return the ProgressView
     */
    public ProgressView setBgColor(int bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    /**
     * 设置进度滚动背景色
     *
     * @param progressColor
     * @return the ProgressView
     */
    public ProgressView setProgressColor(int progressColor) {
        this.progressColor = progressColor;
        return this;
    }

    /**
     * 设置是否显示动画
     *
     * @param showAnim
     * @return the ProgressView
     */
    public ProgressView setShowAnim(boolean showAnim) {
        this.showAnim = showAnim;
        return this;
    }

    /**
     * 设置提示文本
     *
     * @param tipText
     * @return the ProgressView
     */
    public ProgressView setTipText(String tipText) {
        this.tipText = tipText;
        return this;
    }

    /**
     * 设置提示文本颜色
     *
     * @param tipTextColor
     * @return the ProgressView
     */
    public ProgressView setTipTextColor(int tipTextColor) {
        this.tipTextColor = tipTextColor;
        return this;
    }

    /**
     * 设置提示文本字体大小
     *
     * @param tipTextSize
     * @return the ProgressView
     */
    public ProgressView setTipTextSize(float tipTextSize) {
        this.tipTextSize = tipTextSize;
        return this;
    }

    /**
     * 设置进度文本颜色
     *
     * @param progressColor
     * @return the ProgressView
     */
    public ProgressView setProTextColor(int progressColor) {
        this.progressColor = progressColor;
        return this;
    }

    /**
     * 设置进度文本字体大小
     *
     * @param proTextSize
     * @return the ProgressView
     */
    public ProgressView setProTextSize(float proTextSize) {
        this.proTextSize = proTextSize;
        return this;
    }


    /**
     * 获取最大刻度值
     *
     * @return max
     */
    public int getMax() {
        return max;
    }

    /**
     * 获取设置的进度值
     *
     * @return current
     */
    public int getProgress() {
        return current;
    }

    /**
     * 获取渐变色开始颜色值
     *
     * @return startColor
     */
    public int getStartColor() {
        return startColor;
    }

    /**
     * 获取渐变色结束颜色值
     *
     * @return endColor
     */
    public int getEndColor() {
        return endColor;
    }

    /**
     * 获取进度条宽度
     *
     * @return progressWidth
     */
    public float getProgressWidth() {
        return progressWidth;
    }

    /**
     * 获取进度条背景颜色值
     *
     * @return bgColor
     */
    public int getBgColor() {
        return bgColor;
    }

    /**
     * 获取进度条滚动颜色值
     *
     * @return progressColor
     */
    public int getProgressColor() {
        return progressColor;
    }

    /**
     * 是否显示动画
     *
     * @return showAnim
     */
    public boolean isShowAnim() {
        return showAnim;
    }

    /**
     * 获取提示文本颜色值
     *
     * @return tipTextColor
     */
    public int getTipTextColor() {
        return tipTextColor;
    }

    /**
     * 获取进度文本颜色值
     *
     * @return proTextColor
     */
    public int getProTextColor() {
        return proTextColor;
    }

    /**
     * 获取提示文本字体大小
     *
     * @return tipTextSize
     */
    public float getTipTextSize() {
        return tipTextSize;
    }

    /**
     * 获取进度文本字体大小
     *
     * @return proTextSize
     */
    public float getProTextSize() {
        return proTextSize;
    }

    /**
     * 获取提示文本
     *
     * @return tipText
     */
    public String getTipText() {
        return tipText;
    }
}