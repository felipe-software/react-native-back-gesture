package com.felipesoftware.backgesture

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.Interpolator
import android.view.animation.PathInterpolator
import androidx.activity.BackEventCompat
import com.swmansion.rnscreens.ScreenStack
import kotlin.math.exp
import kotlin.math.roundToInt

private const val MAX_SHRINK = 0.1f
private const val EDGE_MARGIN_DP = 8f
private const val MAX_CORNER_RADIUS_DP = 32f
private const val VERTICAL_SENSITIVITY = 2f
private const val ELEVATION_DP = 6f

private const val IOS_PARALLAX = 0.3f
private const val IOS_DIM = 0.1f
private const val IOS_SHADOW_ALPHA = 0.022f
private const val IOS_SHADOW_WIDTH_DP = 20f
private const val IOS_SETTLE_DURATION_MS = 320L
private const val IOS_SETTLE_STIFFNESS = 8f

private const val CARD_SCALE = 0.92f
private const val CARD_CORNER_RADIUS_DP = 20f
private const val CARD_CORNER_RAMP = 6f
private const val CARD_DIM = 0.35f

private const val SHARED_AXIS_SHIFT_DP = 30f

private val FADE_CURVE: Interpolator = PathInterpolator(0.4f, 0f, 0.2f, 1f)
private val IOS_SETTLE_CURVE: Interpolator = CriticallyDampedSpring(IOS_SETTLE_STIFFNESS)

enum class StackAnimationStyle {
  SLIDE,
  SCALE,
  IOS,
  CARD,
  SHARED_AXIS,
}

internal class StackScene(
  val stack: ScreenStack,
  val top: View,
  val below: View,
  val direction: Float,
  val startTouchY: Float,
) {
  val scrim = StackScrim()
  private val density = top.resources.displayMetrics.density
  private val stackBackground = stack.background
  private val belowForeground = below.foreground
  private val topOutline = top.outlineProvider
  private val topClip = top.clipToOutline
  private val belowOutline = below.outlineProvider
  private val belowClip = below.clipToOutline

  val width get() = top.width.toFloat()
  val height get() = top.height.toFloat()

  fun dp(value: Float) = value * density

  fun begin() {
    below.foreground = scrim
    top.bringToFront()
  }

  fun backdrop(color: Int) {
    stack.background = ColorDrawable(color)
  }

  fun releaseBelow() {
    below.foreground = belowForeground
    below.outlineProvider = belowOutline
    below.clipToOutline = belowClip
    below.resetTransform()
    stack.background = stackBackground
  }

  fun reset() {
    releaseBelow()
    top.outlineProvider = topOutline
    top.clipToOutline = topClip
    top.resetTransform()
  }
}

internal abstract class StackMotion(
  protected val scene: StackScene,
) {
  open val finishDurationMs = FINISH_DURATION_MS
  open val finishCurve = FINISH_CURVE
  open val cancelDurationMs = CANCEL_DURATION_MS
  open val cancelCurve = CANCEL_CURVE

  // Android defines raw progress as linear in the finger's distance, reaching about 1 at the opposite edge.
  open fun progressOf(event: BackEventCompat) = event.progress

  open fun begin() = Unit

  abstract fun render(progress: Float, touchY: Float)

  open fun finishFrame(from: Float, fraction: Float, touchY: Float) = render(from + (1f - from) * fraction, touchY)
}

internal class SlideMotion(
  scene: StackScene,
) : StackMotion(scene) {
  override fun begin() {
    scene.top.outlineProvider = ViewOutlineProvider.BOUNDS
    scene.top.translationZ = scene.dp(ELEVATION_DP)
  }

  override fun render(progress: Float, touchY: Float) {
    scene.top.translationX = scene.direction * progress * scene.width
    scene.scrim.dim = SCRIM_DIM * (1f - progress)
  }
}

internal class ScaleMotion(
  scene: StackScene,
) : StackMotion(scene) {
  private var progress = 0f

  override fun progressOf(event: BackEventCompat) = GESTURE_CURVE.getInterpolation(event.progress)

  override fun begin() {
    scene.top.outlineProvider = roundedOutline { progress * scene.dp(MAX_CORNER_RADIUS_DP) }
    scene.top.clipToOutline = true
  }

  override fun render(progress: Float, touchY: Float) {
    this.progress = progress
    val top = scene.top
    val edgeMargin = scene.dp(EDGE_MARGIN_DP)
    val scale = 1f - MAX_SHRINK * progress
    val verticalRatio = ((touchY - scene.startTouchY) / scene.height * VERTICAL_SENSITIVITY).coerceIn(-1f, 1f)
    top.scaleX = scale
    top.scaleY = scale
    top.translationX = scene.direction * progress * (scene.width * MAX_SHRINK / 2f - edgeMargin)
    top.translationY = verticalRatio * progress * (scene.height * MAX_SHRINK / 2f - edgeMargin)
    top.invalidateOutline()
    scene.scrim.dim = SCRIM_DIM
  }

  override fun finishFrame(from: Float, fraction: Float, touchY: Float) {
    render(from, touchY)
    val fromX = scene.top.translationX
    scene.top.translationX = fromX + (scene.direction * scene.width - fromX) * fraction
    scene.top.alpha = 1f - fraction
    scene.scrim.dim = SCRIM_DIM * (1f - fraction)
  }
}

internal class IosMotion(
  scene: StackScene,
) : StackMotion(scene) {
  private var originX = Float.NaN

  override val finishDurationMs = IOS_SETTLE_DURATION_MS
  override val finishCurve = IOS_SETTLE_CURVE
  override val cancelDurationMs = IOS_SETTLE_DURATION_MS
  override val cancelCurve = IOS_SETTLE_CURVE

  override fun progressOf(event: BackEventCompat): Float {
    if (originX.isNaN()) originX = event.touchX
    val travel = (event.touchX - originX) * scene.direction
    if (travel.isNaN() || scene.width <= 0f) return event.progress
    return (travel / scene.width).coerceIn(0f, 1f)
  }

  override fun begin() {
    scene.scrim.castShadow(scene.dp(IOS_SHADOW_WIDTH_DP), IOS_SHADOW_ALPHA)
  }

  override fun render(progress: Float, touchY: Float) {
    val top = scene.top
    val below = scene.below
    top.translationX = scene.direction * progress * scene.width
    below.translationX = -scene.direction * IOS_PARALLAX * scene.width * (1f - progress)
    scene.scrim.dim = IOS_DIM * (1f - progress)
    val topEdge = if (scene.direction > 0) top.left + top.translationX else top.right + top.translationX
    scene.scrim.moveShadow(topEdge - below.left - below.translationX, fallsLeft = scene.direction > 0)
  }
}

internal class CardMotion(
  scene: StackScene,
) : StackMotion(scene) {
  private var topRadius = 0f
  private var belowRadius = 0f

  override fun begin() {
    scene.backdrop(Color.BLACK)
    scene.top.outlineProvider = roundedOutline { topRadius }
    scene.top.clipToOutline = true
    scene.top.translationZ = scene.dp(ELEVATION_DP)
    scene.below.outlineProvider = roundedOutline { belowRadius }
    scene.below.clipToOutline = true
  }

  override fun render(progress: Float, touchY: Float) {
    val corner = scene.dp(CARD_CORNER_RADIUS_DP)
    val scale = CARD_SCALE + (1f - CARD_SCALE) * progress
    topRadius = corner * (progress * CARD_CORNER_RAMP).coerceAtMost(1f)
    belowRadius = corner * (1f - progress)
    scene.top.translationX = scene.direction * progress * scene.width
    scene.below.scaleX = scale
    scene.below.scaleY = scale
    scene.scrim.dim = CARD_DIM * (1f - progress)
    scene.top.invalidateOutline()
    scene.below.invalidateOutline()
  }
}

internal class SharedAxisMotion(
  scene: StackScene,
) : StackMotion(scene) {
  override fun progressOf(event: BackEventCompat) = GESTURE_CURVE.getInterpolation(event.progress)

  override fun render(progress: Float, touchY: Float) {
    val shift = scene.dp(SHARED_AXIS_SHIFT_DP)
    scene.top.translationX = scene.direction * shift * progress
    scene.top.alpha = 1f - FADE_CURVE.getInterpolation(progress)
    scene.below.translationX = -scene.direction * shift * (1f - progress)
  }
}

internal class StackScrim : Drawable() {
  private val dimPaint = Paint().apply { color = Color.BLACK }
  private val shadowPaint = Paint()
  private val shadowMatrix = Matrix()
  private var shadowWidth = 0f
  private var shadowEdge = Float.NaN
  private var shadowSide = 1f

  var dim = 0f
    set(value) {
      if (field == value) return
      field = value
      invalidateSelf()
    }

  fun castShadow(width: Float, alpha: Float) {
    shadowWidth = width
    val color = Color.argb((alpha * 255).roundToInt(), 0, 0, 0)
    shadowPaint.shader = LinearGradient(0f, 0f, width, 0f, Color.TRANSPARENT, color, Shader.TileMode.CLAMP)
  }

  fun moveShadow(edge: Float, fallsLeft: Boolean) {
    val side = if (fallsLeft) 1f else -1f
    shadowEdge = edge
    shadowSide = side
    shadowMatrix.setScale(side, 1f)
    shadowMatrix.postTranslate(edge - side * shadowWidth, 0f)
    shadowPaint.shader?.setLocalMatrix(shadowMatrix)
    invalidateSelf()
  }

  override fun draw(canvas: Canvas) {
    if (dim > 0f) {
      dimPaint.alpha = (dim * 255).roundToInt()
      canvas.drawRect(bounds, dimPaint)
    }
    if (shadowWidth > 0f && !shadowEdge.isNaN()) {
      val far = shadowEdge - shadowSide * shadowWidth
      canvas.drawRect(minOf(far, shadowEdge), bounds.top.toFloat(), maxOf(far, shadowEdge), bounds.bottom.toFloat(), shadowPaint)
    }
  }

  override fun setAlpha(alpha: Int) = Unit

  override fun setColorFilter(colorFilter: ColorFilter?) = Unit

  @Deprecated("Deprecated in Java")
  override fun getOpacity() = PixelFormat.TRANSLUCENT
}

private class CriticallyDampedSpring(
  private val stiffness: Float,
) : Interpolator {
  private val end = settled(stiffness)

  override fun getInterpolation(input: Float) = settled(stiffness * input) / end

  private fun settled(time: Float) = 1f - (1f + time) * exp(-time)
}

private fun roundedOutline(radius: () -> Float) =
  object : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
      outline.setRoundRect(0, 0, view.width, view.height, radius())
    }
  }

private fun View.resetTransform() {
  translationX = 0f
  translationY = 0f
  translationZ = 0f
  scaleX = 1f
  scaleY = 1f
  alpha = 1f
}
