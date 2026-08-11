package com.comix.overwatch;

/**
 * Pure geometry and timing of the 2-3-2 hexagon comb, with no Android dependencies, so the View and
 * the Compose implementations stay in lockstep instead of each carrying their own copy of the
 * trigonometry.
 *
 * <p>Cells hold <em>relative</em> comb coordinates. A gap propagates through the horizontal pitch
 * and the row step in a way that keeps all six neighbour gaps equal: for a comb of circumradius
 * {@code r} with gap {@code g}, every neighbour centre sits {@code √3·r + g} away.
 */
public final class HiveGeometry {

  /** Number of hexagons in the comb. */
  public static final int HEX_COUNT = 7;

  public static final float SQRT_3 = (float) Math.sqrt(3);

  /** Progress value at which one full fade-in/fade-out cycle completes. */
  public static final int MAX_PROGRESS = 1450;

  /**
   * Fraction of the content box used as the gap when no explicit spacing is supplied. Chosen to
   * reproduce the gap this comb had before spacing became configurable.
   */
  public static final float AUTO_SPACING_RATIO = 1f / 23f;

  /** Progress value at which every hexagon is fully faded in. */
  public static final float FULLY_VISIBLE_PROGRESS = 700f;

  private static final int FADE_OUT_START = 700;
  private static final int FULLY_FADED = 1400;

  /** Column offset of each cell, counted in <em>half</em> a horizontal pitch. */
  private static final int[] CELL_HALF_COLUMN = {-1, 1, -2, 0, 2, -1, 1};

  /** Row index of each cell, 0 (top) to 2 (bottom). */
  private static final int[] CELL_ROW = {0, 0, 1, 1, 1, 2, 2};

  /** Position of each cell in the fade wave, preserving the original Overwatch ordering. */
  private static final int[] CELL_WAVE_ORDER = {1, 2, 6, 7, 3, 5, 4};

  /** Per-cell tilt applied in image mode, in degrees. */
  private static final float[] CELL_IMAGE_TILT = {10, -10, -10, 5, -10, 20, 10};

  private HiveGeometry() {
  }

  /** Size-proportional gap used when the caller does not pin one. */
  public static float autoSpacing(float contentWidth, float contentHeight) {
    return Math.min(contentWidth, contentHeight) * AUTO_SPACING_RATIO;
  }

  /**
   * Solves the hexagon circumradius from the space left over once the gaps are taken out. The comb
   * occupies {@code 3√3·r + 2g} horizontally and {@code 5r + √3·g} vertically, so the radius is
   * whichever of those two constraints binds first.
   */
  public static float radiusFor(float contentWidth, float contentHeight, float gap) {
    float fromWidth = (contentWidth - 2 * gap) / (3 * SQRT_3);
    float fromHeight = (contentHeight - SQRT_3 * gap) / 5f;
    return Math.max(0f, Math.min(fromWidth, fromHeight));
  }

  /** Centre-to-centre distance between two hexagons in the same row. */
  public static float pitch(float radius, float gap) {
    return SQRT_3 * radius + gap;
  }

  /** Vertical distance between two adjacent rows. */
  public static float rowStep(float radius, float gap) {
    return 1.5f * radius + SQRT_3 * gap / 2f;
  }

  /** Half the width of a pointy-top hexagon, i.e. the distance from centre to a vertical edge. */
  public static float halfWidth(float radius) {
    return radius * SQRT_3 / 2f;
  }

  public static float cellCenterX(int index, float originX, float radius, float gap) {
    return originX + CELL_HALF_COLUMN[index] * pitch(radius, gap) / 2f;
  }

  public static float cellCenterY(int index, float originY, float radius, float gap) {
    return originY + (CELL_ROW[index] - 1) * rowStep(radius, gap);
  }

  public static int waveOrder(int index) {
    return CELL_WAVE_ORDER[index];
  }

  public static float imageTilt(int index) {
    return CELL_IMAGE_TILT[index];
  }

  /**
   * Opacity of a cell at a point in the wave, as a fraction in {@code [0, 1]}. Each cell fades in
   * 100 progress units after the previous one, then the whole comb fades out in the same order.
   *
   * @param wave the cell's position in the wave, from {@link #waveOrder(int)}
   */
  public static float alphaFractionAt(int wave, float progress) {
    float fraction;
    if (progress > wave * 100) {
      fraction = 1f;
    } else {
      int min = (wave - 1) * 100;
      fraction = Math.max(0f, progress - min) / 100f;
    }
    if (progress > FADE_OUT_START) {
      float fadeProgress = progress - FADE_OUT_START;
      if (fadeProgress > wave * 100) {
        fraction = 0f;
      } else {
        int min = (wave - 1) * 100;
        fraction = 1f - Math.max(0f, fadeProgress - min) / 100f;
      }
    }
    if (progress > FULLY_FADED) {
      fraction = 0f;
    }
    return fraction;
  }

  /** Opacity of a cell scaled into the 0-255 range a {@code Paint} expects. */
  public static int alphaAt(int wave, float progress, int maxAlpha) {
    return (int) (alphaFractionAt(wave, progress) * maxAlpha);
  }

  /**
   * Writes the six vertices of a pointy-top hexagon into {@code out}, as x,y pairs starting from
   * the top vertex and running clockwise.
   *
   * @param out an array of at least 12 floats, overwritten in place
   */
  public static void hexagonVertices(float cx, float cy, float r, float[] out) {
    float edge = halfWidth(r);
    out[0] = cx;
    out[1] = cy - r;
    out[2] = cx + edge;
    out[3] = cy - r / 2f;
    out[4] = cx + edge;
    out[5] = cy + r / 2f;
    out[6] = cx;
    out[7] = cy + r;
    out[8] = cx - edge;
    out[9] = cy + r / 2f;
    out[10] = cx - edge;
    out[11] = cy - r / 2f;
  }
}
