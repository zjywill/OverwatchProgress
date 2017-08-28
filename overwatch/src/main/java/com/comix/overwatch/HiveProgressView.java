package com.comix.overwatch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class HiveProgressView extends View {

    private static final int[] rainbowColor = {
        0xFF0000, 0xFF7F00, 0xFFFF00, 0x00FF00, 0x0000FF, 0x4B0082, 0x9400D3
    };

    private static final int MAX_PROGRESS_VALUE = 1450;
    private static final int PROGRESS_TIME = 2000;
    private static final int MAX_ALPHA = 70;

    private Paint paint = new Paint();
    private int hexHeight;
    private int hexWidth;
    private int hexPadding = 0;
    private float actualProgress = 0;
    private int maxAlpha = MAX_ALPHA;
    private int animationTime = PROGRESS_TIME;
    private int color;
    private boolean rainbow;
    private int cornerRadius;
    private boolean shrink;

    private AnimatorSet indeterminateAnimator;

    public HiveProgressView(Context context) {
        super(context);
    }

    public HiveProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HiveProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttributes(attrs, defStyle);
        initPaint();
    }

    private void initAttributes(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs,
                                                                 R.styleable.HiveProgressView,
                                                                 defStyle, 0);
        animationTime = a.getInteger(R.styleable.HiveProgressView_hive_animDuration, PROGRESS_TIME);
        maxAlpha = a.getInteger(R.styleable.HiveProgressView_hive_maxAlpha, MAX_ALPHA);
        if (a.hasValue(R.styleable.HiveProgressView_hive_color)) {
            color = a.getColor(R.styleable.HiveProgressView_hive_color, Color.BLACK);
        }
        rainbow = a.getBoolean(R.styleable.HiveProgressView_hive_rainbow, false);
        shrink = a.getBoolean(R.styleable.HiveProgressView_hive_shrink, false);
        cornerRadius = a.getInteger(R.styleable.HiveProgressView_hive_cornerRadius, 0);
        a.recycle();
    }

    public int getMaxAlpha() {
        return maxAlpha;
    }

    public void setMaxAlpha(int maxAlpha) {
        this.maxAlpha = maxAlpha;
    }

    public int getAnimationTime() {
        return animationTime;
    }

    public void setAnimationTime(int animationTime) {
        this.animationTime = animationTime;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    public boolean isShrink() {
        return shrink;
    }

    public void setShrink(boolean shrink) {
        this.shrink = shrink;
    }

    private void initPaint() {
        paint.setAlpha(0);
        paint.setPathEffect(new CornerPathEffect(cornerRadius));
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    @Override
    public void setVisibility(int visibility) {
        int currentVisibility = getVisibility();
        super.setVisibility(visibility);
        if (visibility != currentVisibility) {
            if (visibility == View.VISIBLE) {
                resetAnimator();
            } else if (visibility == View.GONE || visibility == View.INVISIBLE) {
                stopAnimation();
            }
        }
    }

    private void startAnimation() {
        resetAnimator();
    }

    private void stopAnimation() {
        actualProgress = 0;
        if (indeterminateAnimator != null) {
            indeterminateAnimator.cancel();
            indeterminateAnimator = null;
        }
    }

    private void resetAnimator() {
        if (indeterminateAnimator != null && indeterminateAnimator.isRunning()) {
            indeterminateAnimator.cancel();
        }
        ValueAnimator progressAnimator = ValueAnimator.ofFloat(0, MAX_PROGRESS_VALUE);
        progressAnimator.setDuration(animationTime);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                actualProgress = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
        indeterminateAnimator = new AnimatorSet();
        indeterminateAnimator.play(progressAnimator);
        indeterminateAnimator.addListener(new AnimatorListenerAdapter() {
            boolean wasCancelled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                wasCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!wasCancelled) {
                    resetAnimator();
                }
            }
        });
        indeterminateAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int viewHeight = viewWidth;
        hexWidth = viewWidth / 3;
        hexHeight = viewHeight * 2 / 5;
        hexPadding = viewHeight / 23;
        setMeasuredDimension(viewWidth, viewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int alpha = getAlpha(1, actualProgress);
        paint.setColor(getHexagonColor(1));
        paint.setAlpha(alpha);
        Path hexPath = hiveRect(hexWidth / 2, hexPadding, hexWidth * 3 / 2, hexHeight + hexPadding,
                                (float) alpha / maxAlpha);
        canvas.drawPath(hexPath, paint);

        alpha = getAlpha(2, actualProgress);
        paint.setColor(getHexagonColor(2));
        paint.setAlpha(alpha);
        hexPath = hiveRect(hexWidth * 3 / 2, hexPadding, hexWidth * 5 / 2, hexHeight + hexPadding,
                           (float) alpha / maxAlpha);
        canvas.drawPath(hexPath, paint);

        alpha = getAlpha(6, actualProgress);
        paint.setColor(getHexagonColor(6));
        paint.setAlpha(alpha);
        hexPath = hiveRect(0, hexHeight * 3 / 4 + hexPadding, hexWidth,
                           hexHeight * 7 / 4 + hexPadding, (float) alpha / maxAlpha);
        canvas.drawPath(hexPath, paint);

        alpha = getAlpha(7, actualProgress);
        paint.setColor(getHexagonColor(7));
        paint.setAlpha(alpha);
        hexPath = hiveRect(hexWidth, hexHeight * 3 / 4 + hexPadding, hexWidth * 2,
                           hexHeight * 7 / 4 + hexPadding, (float) alpha / maxAlpha);
        canvas.drawPath(hexPath, paint);

        alpha = getAlpha(3, actualProgress);
        paint.setColor(getHexagonColor(3));
        paint.setAlpha(alpha);
        hexPath = hiveRect(hexWidth * 2, hexHeight * 3 / 4 + hexPadding, hexWidth * 3,
                           hexHeight * 7 / 4 + hexPadding, (float) alpha / maxAlpha);
        canvas.drawPath(hexPath, paint);

        alpha = getAlpha(5, actualProgress);
        paint.setColor(getHexagonColor(5));
        paint.setAlpha(alpha);
        hexPath = hiveRect(hexWidth / 2, hexHeight * 6 / 4 + hexPadding, hexWidth * 3 / 2,
                           hexHeight * 10 / 4 + hexPadding, (float) alpha / maxAlpha);
        canvas.drawPath(hexPath, paint);

        alpha = getAlpha(4, actualProgress);
        paint.setColor(getHexagonColor(4));
        paint.setAlpha(alpha);
        hexPath = hiveRect(hexWidth * 3 / 2, hexHeight * 6 / 4 + hexPadding, hexWidth * 5 / 2,
                           hexHeight * 10 / 4 + hexPadding, (float) alpha / maxAlpha);
        canvas.drawPath(hexPath, paint);
    }

    private int getHexagonColor(int position) {
        if (rainbow && position <= rainbowColor.length) {
            return rainbowColor[position - 1];
        } else {
            return color;
        }
    }

    private int getAlpha(int num, float progress) {
        float alpha;
        if (progress > num * 100) {
            alpha = maxAlpha;
        } else {
            int min = (num - 1) * 100;
            alpha = (progress - min) > 0 ? progress - min : 0;
            alpha = alpha * maxAlpha / 100;
        }
        if (progress > 700) {
            float fadeProgress = progress - 700;
            if (fadeProgress > num * 100) {
                alpha = 0;
            } else {
                int min = (num - 1) * 100;
                alpha = (fadeProgress - min) > 0 ? fadeProgress - min : 0;
                alpha = maxAlpha - alpha * maxAlpha / 100;
            }
        }
        if (progress > 1400) {
            alpha = 0;
        }
        return (int) alpha;
    }

    private Path hiveRect(int left, int top, int right, int bottom, float percentage) {
        Path path = new Path();
        int height = Math.abs(bottom - top);
        int width = Math.abs(right - left);
        int r = width > height ? height : width;
        r = r / 2;
        int y = top;
        if (shrink) {
            y = top + (int) (r - percentage * r);
            r = (int) (percentage * r);
        }
        int x = (right - left) / 2 + left;
        int edge = (int) (r * Math.sqrt(3) / 2);
        path.moveTo(x, y);
        x = x + edge;
        y = y + r / 2;
        path.lineTo(x, y);
        y = y + r;
        path.lineTo(x, y);
        x = x - edge;
        y = y + r / 2;
        path.lineTo(x, y);
        x = x - edge;
        y = y - r / 2;
        path.lineTo(x, y);
        y = y - r;
        path.lineTo(x, y);
        path.close();
        return path;
    }
}
