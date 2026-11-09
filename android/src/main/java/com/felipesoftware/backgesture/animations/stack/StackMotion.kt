package com.felipesoftware.backgesture.animations.stack

import androidx.activity.BackEventCompat
import com.felipesoftware.backgesture.animations.Timing

internal data class Layer(
  val x: Float = 0f,
  val y: Float = 0f,
  val scale: Float = 1f,
  val alpha: Float = 1f,
  val radius: Float = 0f,
  val elevation: Float = 0f,
)

internal data class Frame(val top: Layer, val below: Layer = Layer(), val dim: Float = 0f, val shadow: Float = 0f)

internal interface StackMotion {
  val finish: Timing get() = Timing.FINISH
  val cancel: Timing get() = Timing.CANCEL
  val backdrop: Int? get() = null

  fun progressOf(event: BackEventCompat, scene: StackScene) = event.progress

  fun frame(progress: Float, scene: StackScene): Frame

  fun finishFrame(from: Float, fraction: Float, scene: StackScene) = frame(from + (1f - from) * fraction, scene)
}

internal enum class StackAnimationStyle(val motion: StackMotion) {
  SLIDE(SlideMotion),
  SCALE(ScaleMotion),
  IOS(IosMotion),
  CARD(CardMotion),
  SHARED_AXIS(SharedAxisMotion),
  ;

  private val prop = name.lowercase().replace('_', '-')

  companion object {
    fun fromProp(value: String?): StackAnimationStyle? =
      when (value) {
        "none" -> null
        else -> entries.firstOrNull { it.prop == value } ?: SLIDE
      }
  }
}
