package com.comix.overwatch;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class HiveProgressView extends View {

  private static final int[] RAINBOW_COLOR = {
    0xFFFF0000, 0xFFFF7F00, 0xFFFFFF00, 0xFF00FF00, 0xFF0000FF, 0xFF4B0082, 0xFF9400D3
  };

  private static final int PROGRESS_TIME = 2000;
  private static final int MAX_ALPHA = 70;
  private static final int DEFAULT_SIZE_DP = 96;

  private final Paint paint = new Paint();
  private final Path[] hexPaths = new Path[HiveGeometry.HEX_COUNT];
  private final float[] cellX = new float[HiveGeometry.HEX_COUNT];
  private final float[] cellY = new float[HiveGeometry.HEX_COUNT];
  private final float[] vertices = new float[12];

  private float hexRadius;
  private float actualProgress = 0;
  private int maxAlpha = MAX_ALPHA;
  private int animationTime = PROGRESS_TIME;
  private int color = Color.BLACK;
  private boolean rainbow;
  private int cornerRadius;
  private boolean shrink;
  private int imageResId = 0;

  /** Gap between neighbouring hexagons in pixels; negative means "derive from the view size". */
  private float spacing = -1f;

  private Drawable imageRes;
  private ValueAnimator indeterminateAnimator;

  public HiveProgressView(Context context) {
    this(context, null);
  }

  public HiveProgressView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public HiveProgressView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    for (int i = 0; i < HiveGeometry.HEX_COUNT; i++) {
      hexPaths[i] = new Path();
    }
    initAttributes(attrs, defStyle);
    initPaint();
    initImage();
    if (isInEditMode()) {
      actualProgress = HiveGeometry.FULLY_VISIBLE_PROGRESS;
    }
  }

  private void initAttributes(AttributeSet attrs, int defStyle) {
    final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HiveProgressView,
                                                             defStyle, 0);
    animationTime = a.getInteger(R.styleable.HiveProgressView_hive_animDuration, PROGRESS_TIME);
    maxAlpha = a.getInteger(R.styleable.HiveProgressView_hive_maxAlpha, MAX_ALPHA);
    color = a.getColor(R.styleable.HiveProgressView_hive_color, Color.BLACK);
    rainbow = a.getBoolean(R.styleable.HiveProgressView_hive_rainbow, false);
    shrink = a.getBoolean(R.styleable.HiveProgressView_hive_shrink, false);
    cornerRadius = a.getInteger(R.styleable.HiveProgressView_hive_cornerRadius, 0);
    imageResId = a.getResourceId(R.styleable.HiveProgressView_hive_image, 0);
    spacing = a.getDimension(R.styleable.HiveProgressView_hive_spacing, -1f);
    a.recycle();
  }

  public int getMaxAlpha() {
    return maxAlpha;
  }

  public void setMaxAlpha(int maxAlpha) {
    this.maxAlpha = maxAlpha;
    invalidate();
  }

  public int getAnimationTime() {
    return animationTime;
  }

  public void setAnimationTime(int animationTime) {
    this.animationTime = animationTime;
    if (indeterminateAnimator != null) {
      resetAnimator();
    }
  }

  public int getColor() {
    return color;
  }

  public void setColor(int color) {
    this.color = color;
    invalidate();
  }

  public boolean isRainbow() {
    return rainbow;
  }

  public void setRainbow(boolean rainbow) {
    this.rainbow = rainbow;
    invalidate();
  }

  public int getCornerRadius() {
    return cornerRadius;
  }

  public void setCornerRadius(int cornerRadius) {
    this.cornerRadius = cornerRadius;
    paint.setPathEffect(new CornerPathEffect(cornerRadius));
    invalidate();
  }

  public boolean isShrink() {
    return shrink;
  }

  public void setShrink(boolean shrink) {
    this.shrink = shrink;
    invalidate();
  }

  /**
   * Gap between neighbouring hexagons in pixels, or a negative value when the gap is derived from
   * the view size.
   */
  public float getSpacing() {
    return spacing;
  }

  /**
   * Sets the gap between neighbouring hexagons. The hexagons shrink to absorb the gap, so the comb
   * always fits the view. Pass a negative value to restore the size-proportional default.
   *
   * @param spacingPx gap in pixels
   */
  public void setSpacing(float spacingPx) {
    if (this.spacing == spacingPx) {
      return;
    }
    this.spacing = spacingPx;
    updateGeometry(getWidth(), getHeight());
    invalidate();
  }

  /**
   * Convenience overload taking density-independent pixels.
   *
   * @param spacingDp gap in dp; negative restores the size-proportional default
   */
  public void setSpacingDp(float spacingDp) {
    setSpacing(spacingDp < 0 ? spacingDp
                             : TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, spacingDp,
                                                         getResources().getDisplayMetrics()));
  }

  private void initPaint() {
    paint.setAlpha(0);
    paint.setPathEffect(new CornerPathEffect(cornerRadius));
    paint.setColor(color);
    paint.setStyle(Paint.Style.FILL);
    paint.setAntiAlias(true);
  }

  private void initImage() {
    if (imageResId != 0) {
      imageRes = getContext().getDrawable(imageResId);
    }
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
        startAnimation();
      } else if (visibility == View.GONE || visibility == View.INVISIBLE) {
        stopAnimation();
      }
    }
  }

  private void startAnimation() {
    if (isInEditMode()) {
      actualProgress = HiveGeometry.FULLY_VISIBLE_PROGRESS;
      invalidate();
      return;
    }
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
    stopAnimation();
    ValueAnimator animator = ValueAnimator.ofFloat(0, HiveGeometry.MAX_PROGRESS);
    animator.setDuration(animationTime);
    animator.setInterpolator(new LinearInterpolator());
    // Looping via the animator itself rather than restarting from onAnimationEnd: the old
    // self-recursion blew the stack whenever the animator duration scale was 0.
    animator.setRepeatCount(ValueAnimator.INFINITE);
    animator.setRepeatMode(ValueAnimator.RESTART);
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        actualProgress = (Float) animation.getAnimatedValue();
        invalidate();
      }
    });
    indeterminateAnimator = animator;
    animator.start();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int defaultSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SIZE_DP,
                                                      getResources().getDisplayMetrics());
    int widthSize = resolveSize(defaultSize + getPaddingLeft() + getPaddingRight(),
                                widthMeasureSpec);
    int heightSize = resolveSize(defaultSize + getPaddingTop() + getPaddingBottom(),
                                 heightMeasureSpec);

    final int size;
    if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
      size = heightSize;
    } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
      size = widthSize;
    } else {
      size = Math.min(widthSize, heightSize);
    }
    setMeasuredDimension(size, size);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    updateGeometry(w, h);
  }

  /**
   * Solves the hexagon radius from the space left over once the gaps are taken out, then places the
   * seven cell centres. For a comb of circumradius {@code r} with a uniform gap {@code g}, every
   * neighbour sits {@code √3·r + g} away, which gives a content box of {@code 3√3·r + 2g} wide and
   * {@code 5r + √3·g} tall.
   */
  private void updateGeometry(int viewWidth, int viewHeight) {
    float contentWidth = viewWidth - getPaddingLeft() - getPaddingRight();
    float contentHeight = viewHeight - getPaddingTop() - getPaddingBottom();
    if (contentWidth <= 0 || contentHeight <= 0) {
      hexRadius = 0;
      return;
    }

    float gap = spacing >= 0 ? spacing : HiveGeometry.autoSpacing(contentWidth, contentHeight);
    hexRadius = HiveGeometry.radiusFor(contentWidth, contentHeight, gap);

    float originX = getPaddingLeft() + contentWidth / 2f;
    float originY = getPaddingTop() + contentHeight / 2f;
    for (int i = 0; i < HiveGeometry.HEX_COUNT; i++) {
      cellX[i] = HiveGeometry.cellCenterX(i, originX, hexRadius, gap);
      cellY[i] = HiveGeometry.cellCenterY(i, originY, hexRadius, gap);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (hexRadius <= 0) {
      return;
    }
    if (imageRes != null) {
      drawImages(canvas);
    } else {
      drawHexagons(canvas);
    }
  }

  private void drawHexagons(Canvas canvas) {
    for (int i = 0; i < HiveGeometry.HEX_COUNT; i++) {
      int wave = HiveGeometry.waveOrder(i);
      int alpha = HiveGeometry.alphaAt(wave, actualProgress, maxAlpha);
      if (alpha <= 0) {
        continue;
      }
      float radius = shrink ? hexRadius * alpha / maxAlpha : hexRadius;
      buildHexPath(hexPaths[i], cellX[i], cellY[i], radius);
      paint.setColor(getHexagonColor(wave));
      paint.setAlpha(alpha);
      canvas.drawPath(hexPaths[i], paint);
    }
  }

  private void drawImages(Canvas canvas) {
    float halfWidth = HiveGeometry.halfWidth(hexRadius);
    for (int i = 0; i < HiveGeometry.HEX_COUNT; i++) {
      int alpha = HiveGeometry.alphaAt(HiveGeometry.waveOrder(i), actualProgress, maxAlpha);
      if (alpha <= 0) {
        continue;
      }
      canvas.save();
      canvas.rotate(HiveGeometry.imageTilt(i), cellX[i], cellY[i]);
      imageRes.setBounds(Math.round(cellX[i] - halfWidth), Math.round(cellY[i] - hexRadius),
                         Math.round(cellX[i] + halfWidth), Math.round(cellY[i] + hexRadius));
      imageRes.setAlpha(alpha);
      imageRes.draw(canvas);
      canvas.restore();
    }
  }

  private int getHexagonColor(int wave) {
    if (rainbow && wave <= RAINBOW_COLOR.length) {
      return RAINBOW_COLOR[wave - 1];
    } else {
      return color;
    }
  }

  /** Builds a pointy-top hexagon of circumradius {@code r} centred on ({@code cx}, {@code cy}). */
  private void buildHexPath(Path path, float cx, float cy, float r) {
    HiveGeometry.hexagonVertices(cx, cy, r, vertices);
    path.reset();
    path.moveTo(vertices[0], vertices[1]);
    for (int v = 2; v < vertices.length; v += 2) {
      path.lineTo(vertices[v], vertices[v + 1]);
    }
    path.close();
  }
}
