package com.felipesoftware.backgesture.animations

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.PathInterpolator
import androidx.activity.BackEventCompat
import kotlin.math.exp

internal val GESTURE_CURVE: Interpolator = PathInterpolator(0.1f, 0.1f, 0f, 1f)
internal val STANDARD_CURVE: Interpolator = PathInterpolator(0.4f, 0f, 0.2f, 1f)

internal interface BackAnimation {
  fun update(event: BackEventCompat)

  fun cancel()

  fun finish(onDone: () -> Unit)
}

internal class Timing(private val durationMs: Long, private val curve: Interpolator) {
  fun animate(onFrame: (Float) -> Unit, onEnd: () -> Unit): ValueAnimator =
    ValueAnimator.ofFloat(0f, 1f).apply {
      duration = durationMs
      interpolator = curve
      addUpdateListener { onFrame(it.animatedValue as Float) }
      addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) = onEnd()
      })
      start()
    }

  companion object {
    val FINISH = Timing(220, STANDARD_CURVE)
    val CANCEL = Timing(180, DecelerateInterpolator())
  }
}

internal class CriticallyDampedSpring(private val stiffness: Float) : Interpolator {
  private val end = settled(stiffness)

  override fun getInterpolation(input: Float) = settled(stiffness * input) / end

  private fun settled(time: Float) = 1f - (1f + time) * exp(-time)
}
