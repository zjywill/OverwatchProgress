package com.comix.overwatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HiveGeometryTest {

  private static final float EPSILON = 0.01f;

  /** Index of the cell in the middle of the comb, which borders all six others. */
  private static final int CENTRE_CELL = 3;

  /**
   * The property the spacing parameter is supposed to guarantee: one scalar gap widens the comb by
   * the same amount in every direction. Without this, spacing would only look right along one axis.
   */
  @Test
  public void everyNeighbourOfTheCentreCellIsEquidistant() {
    float box = 480f;
    float gap = 12f;
    float radius = HiveGeometry.radiusFor(box, box, gap);
    float centreX = HiveGeometry.cellCenterX(CENTRE_CELL, box / 2f, radius, gap);
    float centreY = HiveGeometry.cellCenterY(CENTRE_CELL, box / 2f, radius, gap);
    float expected = HiveGeometry.SQRT_3 * radius + gap;

    for (int i = 0; i < HiveGeometry.HEX_COUNT; i++) {
      if (i == CENTRE_CELL) {
        continue;
      }
      float dx = HiveGeometry.cellCenterX(i, box / 2f, radius, gap) - centreX;
      float dy = HiveGeometry.cellCenterY(i, box / 2f, radius, gap) - centreY;
      assertEquals("cell " + i, expected, (float) Math.hypot(dx, dy), EPSILON);
    }
  }

  /** At zero gap the hexagons must tile exactly: touching, with no overlap. */
  @Test
  public void zeroGapTilesExactly() {
    float box = 300f;
    float radius = HiveGeometry.radiusFor(box, box, 0f);
    float centreX = HiveGeometry.cellCenterX(CENTRE_CELL, box / 2f, radius, 0f);
    float centreY = HiveGeometry.cellCenterY(CENTRE_CELL, box / 2f, radius, 0f);

    for (int i = 0; i < HiveGeometry.HEX_COUNT; i++) {
      if (i == CENTRE_CELL) {
        continue;
      }
      float dx = HiveGeometry.cellCenterX(i, box / 2f, radius, 0f) - centreX;
      float dy = HiveGeometry.cellCenterY(i, box / 2f, radius, 0f) - centreY;
      assertEquals(HiveGeometry.SQRT_3 * radius, (float) Math.hypot(dx, dy), EPSILON);
    }
  }

  /** The radius is solved so that the comb always fits, whatever the gap. */
  @Test
  public void combStaysInsideTheContentBox() {
    for (float gap : new float[] {0f, 8f, 40f, 90f}) {
      float width = 300f;
      float height = 260f;
      float radius = HiveGeometry.radiusFor(width, height, gap);
      float halfWidth = HiveGeometry.halfWidth(radius);

      for (int i = 0; i < HiveGeometry.HEX_COUNT; i++) {
        float x = HiveGeometry.cellCenterX(i, width / 2f, radius, gap);
        float y = HiveGeometry.cellCenterY(i, height / 2f, radius, gap);
        String cell = "gap " + gap + ", cell " + i;
        assertTrue(cell, x - halfWidth >= -EPSILON);
        assertTrue(cell, x + halfWidth <= width + EPSILON);
        assertTrue(cell, y - radius >= -EPSILON);
        assertTrue(cell, y + radius <= height + EPSILON);
      }
    }
  }

  @Test
  public void radiusCollapsesToZeroRatherThanGoingNegative() {
    assertEquals(0f, HiveGeometry.radiusFor(100f, 100f, 500f), EPSILON);
    assertEquals(0f, HiveGeometry.radiusFor(0f, 0f, 0f), EPSILON);
  }

  @Test
  public void cellsFadeInOneAfterAnother() {
    assertEquals(0f, HiveGeometry.alphaFractionAt(1, 0f), EPSILON);
    assertEquals(1f, HiveGeometry.alphaFractionAt(1, 100f), EPSILON);
    // The last cell in the wave is still dark when the first one is already lit.
    assertEquals(0f, HiveGeometry.alphaFractionAt(7, 100f), EPSILON);
    assertEquals(1f, HiveGeometry.alphaFractionAt(7, HiveGeometry.FULLY_VISIBLE_PROGRESS), EPSILON);
  }

  @Test
  public void everyCellIsDarkAtBothEndsOfTheCycle() {
    for (int i = 0; i < HiveGeometry.HEX_COUNT; i++) {
      int wave = HiveGeometry.waveOrder(i);
      assertEquals("cell " + i, 0f, HiveGeometry.alphaFractionAt(wave, 0f), EPSILON);
      assertEquals("cell " + i, 0f,
                   HiveGeometry.alphaFractionAt(wave, HiveGeometry.MAX_PROGRESS), EPSILON);
    }
  }

  @Test
  public void alphaStaysWithinRangeAcrossTheWholeCycle() {
    for (float progress = 0f; progress <= HiveGeometry.MAX_PROGRESS; progress += 7f) {
      for (int i = 0; i < HiveGeometry.HEX_COUNT; i++) {
        float fraction = HiveGeometry.alphaFractionAt(HiveGeometry.waveOrder(i), progress);
        assertTrue("progress " + progress, fraction >= 0f && fraction <= 1f);
      }
    }
  }

  /** Every cell appears exactly once in the wave, so no hexagon is skipped or doubled up. */
  @Test
  public void waveOrderIsAPermutation() {
    boolean[] seen = new boolean[HiveGeometry.HEX_COUNT + 1];
    for (int i = 0; i < HiveGeometry.HEX_COUNT; i++) {
      int wave = HiveGeometry.waveOrder(i);
      assertTrue("wave " + wave + " out of range", wave >= 1 && wave <= HiveGeometry.HEX_COUNT);
      assertTrue("wave " + wave + " repeated", !seen[wave]);
      seen[wave] = true;
    }
  }

  @Test
  public void hexagonVerticesLieOnTheCircumcircle() {
    float[] vertices = new float[12];
    HiveGeometry.hexagonVertices(50f, 60f, 20f, vertices);
    for (int v = 0; v < vertices.length; v += 2) {
      double distance = Math.hypot(vertices[v] - 50f, vertices[v + 1] - 60f);
      assertEquals(20f, (float) distance, EPSILON);
    }
  }
}
