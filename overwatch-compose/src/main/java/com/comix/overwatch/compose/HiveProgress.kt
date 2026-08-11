package com.comix.overwatch.compose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.comix.overwatch.HiveGeometry
import kotlin.math.hypot
import kotlin.math.min

/** Defaults shared by the [HiveProgress] overloads. */
object HiveProgressDefaults {

  /** Size the indicator falls back to when the caller does not constrain it. */
  val Size: Dp = 96.dp

  /** Matches the View's default of 70 out of 255. */
  const val MaxAlpha: Float = 70f / 255f

  const val AnimationDurationMillis: Int = 2000

  /** The seven-colour ramp used by the View's `hive_rainbow` mode. */
  val Rainbow: List<Color> = listOf(
    Color(0xFFFF0000),
    Color(0xFFFF7F00),
    Color(0xFFFFFF00),
    Color(0xFF00FF00),
    Color(0xFF0000FF),
    Color(0xFF4B0082),
    Color(0xFF9400D3),
  )
}

/**
 * An indeterminate loading indicator: seven hexagons in a 2-3-2 comb that fade in and out as a
 * wave.
 *
 * The comb is laid out by [HiveGeometry], the same code the `HiveProgressView` uses, so both
 * renderers stay in step.
 *
 * @param color colour of every hexagon; ignored when [colors] is supplied
 * @param colors per-cell colour ramp, e.g. [HiveProgressDefaults.Rainbow]
 * @param spacing gap between neighbouring hexagons; [Dp.Unspecified] derives it from the size
 * @param cornerRadius how far each hexagon corner is rounded
 * @param shrink scale each hexagon with its opacity instead of only fading it
 * @param maxAlpha peak opacity of a hexagon, in `[0, 1]`
 */
@Composable
fun HiveProgress(
  modifier: Modifier = Modifier,
  color: Color = Color.Black,
  colors: List<Color>? = null,
  spacing: Dp = Dp.Unspecified,
  cornerRadius: Dp = 0.dp,
  shrink: Boolean = false,
  maxAlpha: Float = HiveProgressDefaults.MaxAlpha,
  animationDurationMillis: Int = HiveProgressDefaults.AnimationDurationMillis,
) {
  val transition = rememberInfiniteTransition(label = "HiveProgress")
  val progress by transition.animateFloat(
    initialValue = 0f,
    targetValue = HiveGeometry.MAX_PROGRESS.toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = animationDurationMillis, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "progress",
  )
  HiveProgress(
    progress = progress,
    modifier = modifier.progressSemantics(),
    color = color,
    colors = colors,
    spacing = spacing,
    cornerRadius = cornerRadius,
    shrink = shrink,
    maxAlpha = maxAlpha,
  )
}

/**
 * The stateless form of [HiveProgress]: the caller drives the wave, which makes the output a pure
 * function of [progress] and so keeps screenshot tests deterministic.
 *
 * @param progress position in the wave, from 0 to [HiveGeometry.MAX_PROGRESS]
 */
@Composable
fun HiveProgress(
  progress: Float,
  modifier: Modifier = Modifier,
  color: Color = Color.Black,
  colors: List<Color>? = null,
  spacing: Dp = Dp.Unspecified,
  cornerRadius: Dp = 0.dp,
  shrink: Boolean = false,
  maxAlpha: Float = HiveProgressDefaults.MaxAlpha,
) {
  val path = remember { Path() }
  val vertices = remember { FloatArray(12) }

  Canvas(modifier.defaultMinSize(HiveProgressDefaults.Size, HiveProgressDefaults.Size)) {
    val side = min(size.width, size.height)
    if (side <= 0f) return@Canvas

    val gap = if (spacing == Dp.Unspecified) HiveGeometry.autoSpacing(side, side) else spacing.toPx()
    val radius = HiveGeometry.radiusFor(side, side, gap)
    if (radius <= 0f) return@Canvas

    val originX = size.width / 2f
    val originY = size.height / 2f
    val corner = cornerRadius.toPx()

    for (i in 0 until HiveGeometry.HEX_COUNT) {
      val wave = HiveGeometry.waveOrder(i)
      val fraction = HiveGeometry.alphaFractionAt(wave, progress)
      if (fraction <= 0f) continue

      path.buildHexagon(
        cx = HiveGeometry.cellCenterX(i, originX, radius, gap),
        cy = HiveGeometry.cellCenterY(i, originY, radius, gap),
        r = if (shrink) radius * fraction else radius,
        corner = corner,
        vertices = vertices,
      )
      drawPath(
        path = path,
        color = colors?.get((wave - 1) % colors.size) ?: color,
        alpha = fraction * maxAlpha,
      )
    }
  }
}

/**
 * Rewrites [this] as a pointy-top hexagon with rounded corners. Each corner is trimmed back along
 * both adjacent edges and bridged with a quadratic through the original vertex, which is what
 * `CornerPathEffect` does for the View.
 */
private fun Path.buildHexagon(
  cx: Float,
  cy: Float,
  r: Float,
  corner: Float,
  vertices: FloatArray,
) {
  HiveGeometry.hexagonVertices(cx, cy, r, vertices)
  reset()

  if (corner <= 0f) {
    moveTo(vertices[0], vertices[1])
    for (i in 1 until 6) {
      lineTo(vertices[i * 2], vertices[i * 2 + 1])
    }
    close()
    return
  }

  for (i in 0 until 6) {
    val vx = vertices[i * 2]
    val vy = vertices[i * 2 + 1]
    val px = vertices[(i + 5) % 6 * 2]
    val py = vertices[(i + 5) % 6 * 2 + 1]
    val nx = vertices[(i + 1) % 6 * 2]
    val ny = vertices[(i + 1) % 6 * 2 + 1]

    val inLength = hypot(px - vx, py - vy)
    val outLength = hypot(nx - vx, ny - vy)
    if (inLength <= 0f || outLength <= 0f) continue

    val inT = min(corner, inLength / 2f) / inLength
    val outT = min(corner, outLength / 2f) / outLength

    val startX = vx + (px - vx) * inT
    val startY = vy + (py - vy) * inT
    val endX = vx + (nx - vx) * outT
    val endY = vy + (ny - vy) * outT

    if (i == 0) moveTo(startX, startY) else lineTo(startX, startY)
    quadraticTo(vx, vy, endX, endY)
  }
  close()
}

@Preview(name = "Rainbow", showBackground = true)
@Composable
private fun HiveProgressRainbowPreview() {
  HiveProgress(
    progress = HiveGeometry.FULLY_VISIBLE_PROGRESS,
    modifier = Modifier.size(160.dp),
    colors = HiveProgressDefaults.Rainbow,
    cornerRadius = 6.dp,
    maxAlpha = 1f,
  )
}

@Preview(name = "Wide spacing", showBackground = true)
@Composable
private fun HiveProgressSpacingPreview() {
  HiveProgress(
    progress = HiveGeometry.FULLY_VISIBLE_PROGRESS,
    modifier = Modifier.size(160.dp),
    spacing = 16.dp,
    maxAlpha = 1f,
  )
}
