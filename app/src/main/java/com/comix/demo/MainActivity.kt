package com.comix.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.comix.overwatch.HiveProgressView
import com.comix.overwatch.compose.HiveProgress
import com.comix.overwatch.compose.HiveProgressDefaults
import kotlin.math.roundToInt

private const val ANIMATION_MILLIS = 5000
private val CORNER_RADIUS = 8.dp
private const val MAX_SPACING_DP = 48f

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MaterialTheme {
        Surface(Modifier.fillMaxSize()) { DemoScreen() }
      }
    }
  }
}

/** Drives both renderers from one slider, so any drift between them is visible side by side. */
@Composable
private fun DemoScreen() {
  var spacingDp by remember { mutableFloatStateOf(8f) }
  // HiveProgressView still takes a raw pixel corner radius, so convert to keep both sides equal.
  val cornerRadiusPx = with(LocalDensity.current) { CORNER_RADIUS.roundToPx() }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .safeDrawingPadding()
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
  ) {
    Text("Compose", style = MaterialTheme.typography.labelLarge)
    HiveProgress(
      modifier = Modifier.size(170.dp),
      colors = HiveProgressDefaults.Rainbow,
      spacing = spacingDp.dp,
      cornerRadius = CORNER_RADIUS,
      shrink = true,
      maxAlpha = 1f,
      animationDurationMillis = ANIMATION_MILLIS,
    )

    Text("View", style = MaterialTheme.typography.labelLarge)
    AndroidView(
      factory = { context ->
        HiveProgressView(context).apply {
          isRainbow = true
          isShrink = true
          cornerRadius = cornerRadiusPx
          maxAlpha = 255
          animationTime = ANIMATION_MILLIS
        }
      },
      update = { view -> view.setSpacingDp(spacingDp) },
      modifier = Modifier.size(170.dp),
    )

    Text("Spacing ${spacingDp.roundToInt()}dp")
    Slider(
      value = spacingDp,
      onValueChange = { spacingDp = it },
      valueRange = 0f..MAX_SPACING_DP,
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun DemoScreenPreview() {
  MaterialTheme { DemoScreen() }
}
