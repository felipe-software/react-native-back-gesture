package com.felipesoftware.backgesture

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import androidx.activity.BackEventCompat

private const val MAX_SHRINK = 0.1f
private const val EDGE_MARGIN_DP = 8f
private const val MAX_CORNER_RADIUS_DP = 32f
private const val VERTICAL_SENSITIVITY = 2f
private const val SLIDE_ELEVATION_DP = 6f

enum class StackAnimationStyle {
  SLIDE,
  SCALE,
}

internal interface StackMotion {
  fun progressOf(event: BackEventCompat): Float

  fun begin()

  fun apply(progress: Float, touchY: Float)

  fun scrimAlpha(progress: Float): Int

  fun finishFrame(fraction: Float)

  fun reset()
}

internal class SlideMotion(
  private val top: View,
  private val direction: Float,
) : StackMotion {
  private val elevation = SLIDE_ELEVATION_DP * top.resources.displayMetrics.density

  // Android defines raw progress as linear in the finger's distance, reaching about 1 at the opposite edge.
  override fun progressOf(event: BackEventCompat) = event.progress

  override fun begin() {
    top.outlineProvider = ViewOutlineProvider.BOUNDS
    top.translationZ = elevation
  }

  override fun apply(progress: Float, touchY: Float) {
    top.translationX = direction * progress * top.width
  }

  override fun scrimAlpha(progress: Float) = (SCRIM_ALPHA * (1f - progress)).toInt()

  override fun finishFrame(fraction: Float) = Unit

  override fun reset() {
    top.translationZ = 0f
    top.outlineProvider = ViewOutlineProvider.BACKGROUND
  }
}

internal class ScaleMotion(
  private val top: View,
  private val direction: Float,
  private val startTouchY: Float,
) : StackMotion {
  private val density = top.resources.displayMetrics.density
  private var progress = 0f

  override fun progressOf(event: BackEventCompat) = GESTURE_CURVE.getInterpolation(event.progress)

  override fun begin() {
    top.outlineProvider = object : ViewOutlineProvider() {
      override fun getOutline(view: View, outline: Outline) {
        outline.setRoundRect(0, 0, view.width, view.height, progress * MAX_CORNER_RADIUS_DP * density)
      }
    }
    top.clipToOutline = true
  }

  override fun apply(progress: Float, touchY: Float) {
    this.progress = progress
    val width = top.width.toFloat()
    val height = top.height.toFloat()
    val edgeMargin = EDGE_MARGIN_DP * density
    val scale = 1f - MAX_SHRINK * progress
    val verticalRatio = ((touchY - startTouchY) / height * VERTICAL_SENSITIVITY).coerceIn(-1f, 1f)
    top.scaleX = scale
    top.scaleY = scale
    top.translationX = direction * progress * (width * MAX_SHRINK / 2f - edgeMargin)
    top.translationY = verticalRatio * progress * (height * MAX_SHRINK / 2f - edgeMargin)
    top.invalidateOutline()
  }

  override fun scrimAlpha(progress: Float) = SCRIM_ALPHA

  override fun finishFrame(fraction: Float) {
    top.alpha = 1f - fraction
  }

  override fun reset() {
    top.clipToOutline = false
    top.outlineProvider = ViewOutlineProvider.BACKGROUND
  }
}
