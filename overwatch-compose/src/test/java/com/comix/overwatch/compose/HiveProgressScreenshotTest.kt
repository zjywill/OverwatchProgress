package com.comix.overwatch.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.comix.overwatch.HiveGeometry
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the comb at fixed points in the wave. Every case uses the stateless [HiveProgress]
 * overload, so the output depends only on its arguments and never on wall-clock timing.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w220dp-h220dp-xhdpi")
class HiveProgressScreenshotTest {

  @get:Rule
  val compose = createComposeRule()

  @Test
  fun rainbowFullyVisible() = capture("rainbow-fully-visible") {
    HiveProgress(
      progress = HiveGeometry.FULLY_VISIBLE_PROGRESS,
      modifier = Modifier.size(200.dp),
      colors = HiveProgressDefaults.Rainbow,
      maxAlpha = 1f,
    )
  }

  /** At zero spacing the hexagons must tile with no seam and no overlap. */
  @Test
  fun zeroSpacingTiles() = capture("zero-spacing") {
    HiveProgress(
      progress = HiveGeometry.FULLY_VISIBLE_PROGRESS,
      modifier = Modifier.size(200.dp),
      spacing = 0.dp,
      color = Color.Black,
      maxAlpha = 1f,
    )
  }

  @Test
  fun wideSpacingShrinksHexagons() = capture("wide-spacing") {
    HiveProgress(
      progress = HiveGeometry.FULLY_VISIBLE_PROGRESS,
      modifier = Modifier.size(200.dp),
      spacing = 24.dp,
      color = Color.Black,
      maxAlpha = 1f,
    )
  }

  @Test
  fun roundedCorners() = capture("rounded-corners") {
    HiveProgress(
      progress = HiveGeometry.FULLY_VISIBLE_PROGRESS,
      modifier = Modifier.size(200.dp),
      cornerRadius = 10.dp,
      color = Color.Black,
      maxAlpha = 1f,
    )
  }

  /** Mid-wave: the staggered fade should leave the cells at visibly different opacities. */
  @Test
  fun midWave() = capture("mid-wave") {
    HiveProgress(
      progress = 420f,
      modifier = Modifier.size(200.dp),
      colors = HiveProgressDefaults.Rainbow,
      maxAlpha = 1f,
    )
  }

  @Test
  fun shrinkScalesWithOpacity() = capture("shrink") {
    HiveProgress(
      progress = 420f,
      modifier = Modifier.size(200.dp),
      shrink = true,
      colors = HiveProgressDefaults.Rainbow,
      maxAlpha = 1f,
    )
  }

  private fun capture(name: String, content: @Composable () -> Unit) {
    compose.setContent {
      androidx.compose.foundation.layout.Box(Modifier.background(Color.White)) { content() }
    }
    compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
  }
}
