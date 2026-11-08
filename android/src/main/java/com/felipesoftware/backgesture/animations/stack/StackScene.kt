package com.felipesoftware.backgesture.animations.stack

import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewOutlineProvider
import androidx.activity.BackEventCompat
import com.felipesoftware.backgesture.extensions.px
import com.felipesoftware.backgesture.extensions.resetTransform
import com.swmansion.rnscreens.ScreenStack

private const val SHADOW_WIDTH_DP = 20f

internal class StackScene(
  val stack: ScreenStack,
  topView: View,
  belowView: View,
  private val start: BackEventCompat,
  backdrop: Int?,
) {
  private val top = LayerView(topView)
  private val below = LayerView(belowView)
  private val direction = if (start.swipeEdge == BackEventCompat.EDGE_RIGHT) -1f else 1f
  private val scrim = StackScrim(SHADOW_WIDTH_DP.px)
  private val stackBackground = stack.background
  private val belowForeground = belowView.foreground
  private var originX = Float.NaN
  private var touchY = start.touchY

  val width get() = top.view.width.toFloat()
  val height get() = top.view.height.toFloat()
  val verticalDrag get() = (touchY - start.touchY) / height

  init {
    belowView.foreground = scrim
    topView.bringToFront()
    backdrop?.let { stack.background = ColorDrawable(it) }
  }

  fun track(event: BackEventCompat) {
    touchY = event.touchY
  }

  fun fingerProgress(event: BackEventCompat): Float {
    if (originX.isNaN()) originX = event.touchX
    val travel = (event.touchX - originX) * direction
    if (travel.isNaN() || width <= 0f) return event.progress
    return (travel / width).coerceIn(0f, 1f)
  }

  fun render(frame: Frame) {
    top.render(frame.top, direction)
    below.render(frame.below, direction)
    scrim.dim = frame.dim
    if (frame.shadow > 0f) scrim.castShadow(topEdgeOverBelow(), fallsLeft = direction > 0f, alpha = frame.shadow)
  }

  fun releaseBelow() {
    below.view.foreground = belowForeground
    below.restore()
    stack.background = stackBackground
  }

  fun reset() {
    releaseBelow()
    top.restore()
  }

  private fun topEdgeOverBelow(): Float {
    val topEdge = if (direction > 0f) top.view.left else top.view.right
    return topEdge + top.view.translationX - below.view.left - below.view.translationX
  }
}

private class LayerView(val view: View) : ViewOutlineProvider() {
  private val savedOutline = view.outlineProvider
  private val savedClip = view.clipToOutline
  private var radius = 0f

  init {
    view.outlineProvider = this
  }

  override fun getOutline(view: View, outline: Outline) = outline.setRoundRect(0, 0, view.width, view.height, radius)

  fun render(layer: Layer, direction: Float) {
    view.translationX = direction * layer.x
    view.translationY = layer.y
    view.scaleX = layer.scale
    view.scaleY = layer.scale
    view.alpha = layer.alpha
    view.translationZ = layer.elevation
    if (radius == layer.radius) return
    radius = layer.radius
    view.clipToOutline = radius > 0f
    view.invalidateOutline()
  }

  fun restore() {
    view.outlineProvider = savedOutline
    view.clipToOutline = savedClip
    view.resetTransform()
  }
}
