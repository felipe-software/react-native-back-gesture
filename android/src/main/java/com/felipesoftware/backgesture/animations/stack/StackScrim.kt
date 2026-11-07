package com.felipesoftware.backgesture.animations.stack

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.Drawable
import kotlin.math.roundToInt

internal class StackScrim(private val shadowWidth: Float) : Drawable() {
  private val dimPaint = Paint().apply { color = Color.BLACK }
  private val shadowGradient =
    LinearGradient(0f, 0f, shadowWidth, 0f, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP)
  private val shadowPaint = Paint().apply { shader = shadowGradient }
  private val shadowMatrix = Matrix()
  private var shadowEdge = Float.NaN
  private var shadowSide = 1f

  var dim = 0f
    set(value) {
      if (field == value) return
      field = value
      invalidateSelf()
    }

  fun castShadow(edge: Float, fallsLeft: Boolean, alpha: Float) {
    shadowEdge = edge
    shadowSide = if (fallsLeft) 1f else -1f
    shadowPaint.alpha = (alpha * 255).roundToInt()
    shadowMatrix.setScale(shadowSide, 1f)
    shadowMatrix.postTranslate(edge - shadowSide * shadowWidth, 0f)
    shadowGradient.setLocalMatrix(shadowMatrix)
    invalidateSelf()
  }

  override fun draw(canvas: Canvas) {
    if (dim > 0f) {
      dimPaint.alpha = (dim * 255).roundToInt()
      canvas.drawRect(bounds, dimPaint)
    }
    if (shadowEdge.isNaN()) return
    val far = shadowEdge - shadowSide * shadowWidth
    val top = bounds.top.toFloat()
    canvas.drawRect(minOf(far, shadowEdge), top, maxOf(far, shadowEdge), bounds.bottom.toFloat(), shadowPaint)
  }

  override fun setAlpha(alpha: Int) = Unit

  override fun setColorFilter(colorFilter: ColorFilter?) = Unit

  @Deprecated("Deprecated in Java")
  override fun getOpacity() = PixelFormat.TRANSLUCENT
}
