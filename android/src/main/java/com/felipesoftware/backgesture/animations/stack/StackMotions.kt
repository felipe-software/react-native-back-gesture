package com.felipesoftware.backgesture.animations.stack

import android.graphics.Color
import androidx.activity.BackEventCompat
import com.felipesoftware.backgesture.animations.CriticallyDampedSpring
import com.felipesoftware.backgesture.animations.GESTURE_CURVE
import com.felipesoftware.backgesture.animations.STANDARD_CURVE
import com.felipesoftware.backgesture.animations.Timing
import com.felipesoftware.backgesture.extensions.px

private const val SCRIM_DIM = 0.2f
private const val ELEVATION_DP = 6f
private const val SHRINK = 0.1f
private const val SHRINK_MARGIN_DP = 8f
private const val SHRINK_CORNER_DP = 32f
private const val SHRINK_VERTICAL_SENSITIVITY = 2f
private const val IOS_PARALLAX = 0.3f
private const val IOS_DIM = 0.1f
private const val IOS_SHADOW = 0.022f
private const val CARD_SCALE = 0.92f
private const val CARD_CORNER_DP = 20f
private const val CARD_CORNER_RAMP = 6f
private const val CARD_DIM = 0.35f
private const val SHARED_AXIS_SHIFT_DP = 30f

internal object SlideMotion : StackMotion {
  override fun frame(progress: Float, scene: StackScene) =
    Frame(top = Layer(x = progress * scene.width, elevation = ELEVATION_DP.px), dim = SCRIM_DIM * (1f - progress))
}

internal object ScaleMotion : StackMotion {
  override fun progressOf(event: BackEventCompat, scene: StackScene) = GESTURE_CURVE.getInterpolation(event.progress)

  override fun frame(progress: Float, scene: StackScene): Frame {
    val margin = SHRINK_MARGIN_DP.px
    val vertical = (scene.verticalDrag * SHRINK_VERTICAL_SENSITIVITY).coerceIn(-1f, 1f)
    val top = Layer(
      x = progress * (scene.width * SHRINK / 2f - margin),
      y = vertical * progress * (scene.height * SHRINK / 2f - margin),
      scale = 1f - SHRINK * progress,
      radius = progress * SHRINK_CORNER_DP.px,
    )
    return Frame(top = top, dim = SCRIM_DIM)
  }

  override fun finishFrame(from: Float, fraction: Float, scene: StackScene): Frame {
    val release = frame(from, scene).top
    val top = release.copy(x = release.x + (scene.width - release.x) * fraction, alpha = 1f - fraction)
    return Frame(top = top, dim = SCRIM_DIM * (1f - fraction))
  }
}

internal object IosMotion : StackMotion {
  private val settle = Timing(320, CriticallyDampedSpring(8f))
  override val finish = settle
  override val cancel = settle

  override fun progressOf(event: BackEventCompat, scene: StackScene) = scene.fingerProgress(event)

  override fun frame(progress: Float, scene: StackScene) =
    Frame(
      top = Layer(x = progress * scene.width),
      below = Layer(x = -IOS_PARALLAX * scene.width * (1f - progress)),
      dim = IOS_DIM * (1f - progress),
      shadow = IOS_SHADOW,
    )
}

internal object CardMotion : StackMotion {
  override val backdrop = Color.BLACK

  override fun frame(progress: Float, scene: StackScene): Frame {
    val corner = CARD_CORNER_DP.px
    val topCorner = corner * (progress * CARD_CORNER_RAMP).coerceAtMost(1f)
    val scale = CARD_SCALE + (1f - CARD_SCALE) * progress
    return Frame(
      top = Layer(x = progress * scene.width, radius = topCorner, elevation = ELEVATION_DP.px),
      below = Layer(scale = scale, radius = corner * (1f - progress)),
      dim = CARD_DIM * (1f - progress),
    )
  }
}

internal object SharedAxisMotion : StackMotion {
  override fun progressOf(event: BackEventCompat, scene: StackScene) = GESTURE_CURVE.getInterpolation(event.progress)

  override fun frame(progress: Float, scene: StackScene): Frame {
    val shift = SHARED_AXIS_SHIFT_DP.px
    val fade = STANDARD_CURVE.getInterpolation(progress)
    return Frame(top = Layer(x = shift * progress, alpha = 1f - fade), below = Layer(x = -shift * (1f - progress)))
  }
}
