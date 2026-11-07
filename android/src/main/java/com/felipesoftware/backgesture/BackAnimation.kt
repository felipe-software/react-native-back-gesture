package com.felipesoftware.backgesture

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.PathInterpolator
import androidx.activity.BackEventCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.swmansion.rnscreens.Screen
import com.swmansion.rnscreens.ScreenStack

internal const val SCRIM_ALPHA = 51
private const val CANCEL_DURATION_MS = 180L
private const val FINISH_DURATION_MS = 220L
private const val POP_TIMEOUT_MS = 1000L
private const val SHEET_RESET_DELAY_MS = 500L

// The curve Android's own cross-activity back animation applies to gesture progress.
internal val GESTURE_CURVE: Interpolator = PathInterpolator(0.1f, 0.1f, 0f, 1f)
private val FINISH_CURVE: Interpolator = PathInterpolator(0.4f, 0f, 0.2f, 1f)
private val CANCEL_CURVE: Interpolator = DecelerateInterpolator()

internal interface BackAnimation {
  fun update(event: BackEventCompat)

  fun cancel()

  fun finish(onDone: () -> Unit)
}

internal interface BackGestureEmitter {
  fun emit(name: String, progress: Float, swipeEdge: Int)
}

internal class JsBackAnimation(
  start: BackEventCompat,
  private val emitter: BackGestureEmitter,
) : BackAnimation {
  private val swipeEdge = start.swipeEdge
  private var progress = GESTURE_CURVE.getInterpolation(start.progress)

  init {
    emitter.emit(BACK_STARTED, progress, swipeEdge)
  }

  override fun update(event: BackEventCompat) {
    progress = GESTURE_CURVE.getInterpolation(event.progress)
    emitter.emit(BACK_PROGRESSED, progress, swipeEdge)
  }

  override fun cancel() {
    emitter.emit(BACK_CANCELLED, progress, swipeEdge)
  }

  override fun finish(onDone: () -> Unit) {
    emitter.emit(BACK_INVOKED, progress, swipeEdge)
    onDone()
  }
}

internal class SheetBackAnimation(
  private val sheet: BottomSheetBehavior<*>,
  start: BackEventCompat,
) : BackAnimation {
  init {
    sheet.startBackProgress(start)
  }

  override fun update(event: BackEventCompat) {
    sheet.updateBackProgress(event)
  }

  override fun cancel() {
    sheet.cancelBackProgress()
  }

  // React Native's back handling dismisses the sheet. The shrink is undone once the sheet has slid away,
  // so it doesn't snap back to full size first.
  override fun finish(onDone: () -> Unit) {
    onDone()
    Handler(Looper.getMainLooper()).postDelayed(sheet::cancelBackProgress, SHEET_RESET_DELAY_MS)
  }
}

internal fun ScreenStack.topIndex(): Int = fragments.indexOfLast { it.screen === topScreen }

internal class StackBackAnimation private constructor(
  private val stack: ScreenStack,
  private val topScreen: Screen,
  private val top: View,
  private val below: Fragment,
  private val fragmentManager: FragmentManager,
  private val direction: Float,
  private val motion: StackMotion,
  start: BackEventCompat,
) : BackAnimation {
  private val attachedBelow = !below.isAdded
  private val scrim = ColorDrawable(Color.BLACK)
  private val mainHandler = Handler(Looper.getMainLooper())
  private var progress = 0f
  private var touchY = start.touchY
  private var settling: ValueAnimator? = null
  private var awaitingPop = false

  init {
    // A pushed page detaches the one under it, so bring it back only for as long as the gesture needs it.
    if (attachedBelow) {
      fragmentManager
        .beginTransaction()
        .setReorderingAllowed(true)
        .add(stack.id, below)
        .commitNowAllowingStateLoss()
    }
    scrim.alpha = motion.scrimAlpha(0f)
    below.view?.foreground = scrim
    top.bringToFront()
    motion.begin()
  }

  override fun update(event: BackEventCompat) {
    progress = motion.progressOf(event)
    touchY = event.touchY
    applyProgress()
  }

  override fun cancel() {
    val from = progress
    settling = animate(CANCEL_DURATION_MS, CANCEL_CURVE, onEnd = ::restore) { fraction ->
      progress = from * (1f - fraction)
      applyProgress()
    }
  }

  fun endCancel() {
    settling?.end()
  }

  fun isAwaitingPop() = awaitingPop && stack.topScreen === topScreen

  override fun finish(onDone: () -> Unit) {
    val fromX = top.translationX
    val toX = direction * top.width
    val fromScrim = scrim.alpha
    awaitingPop = true
    animate(FINISH_DURATION_MS, FINISH_CURVE, onEnd = {
      below.view?.foreground = null
      onDone()
      mainHandler.postDelayed(::restoreIfNotPopped, POP_TIMEOUT_MS)
    }) { fraction ->
      top.translationX = fromX + (toX - fromX) * fraction
      scrim.alpha = (fromScrim * (1f - fraction)).toInt()
      motion.finishFrame(fraction)
    }
  }

  private fun applyProgress() {
    motion.apply(progress, touchY)
    scrim.alpha = motion.scrimAlpha(progress)
  }

  private fun restoreIfNotPopped() {
    awaitingPop = false
    if (stack.topScreen === topScreen) restore()
  }

  private fun restore() {
    settling = null
    top.scaleX = 1f
    top.scaleY = 1f
    top.translationX = 0f
    top.translationY = 0f
    top.alpha = 1f
    motion.reset()
    below.view?.foreground = null
    if (attachedBelow && below.isAdded && !fragmentManager.isDestroyed) {
      fragmentManager
        .beginTransaction()
        .setReorderingAllowed(true)
        .remove(below)
        .commitNowAllowingStateLoss()
    }
  }

  companion object {
    fun create(stack: ScreenStack, start: BackEventCompat, style: StackAnimationStyle): StackBackAnimation? {
      val topIndex = stack.topIndex()
      if (topIndex < 1) return null
      val wrapper = stack.fragments[topIndex]
      val top = wrapper.fragment.view ?: return null
      val below = stack.fragments[topIndex - 1].fragment
      val pushOrPopRunning = top.animation?.hasEnded() == false || (below.view != null && !below.isAdded)
      if (pushOrPopRunning) return null
      val direction = if (start.swipeEdge == BackEventCompat.EDGE_RIGHT) -1f else 1f
      val motion = when (style) {
        StackAnimationStyle.SLIDE -> SlideMotion(top, direction)
        StackAnimationStyle.SCALE -> ScaleMotion(top, direction, start.touchY)
      }
      val fragmentManager = wrapper.fragment.parentFragmentManager
      return StackBackAnimation(stack, wrapper.screen, top, below, fragmentManager, direction, motion, start)
    }
  }
}

private fun animate(
  duration: Long,
  curve: Interpolator,
  onEnd: () -> Unit,
  onFrame: (Float) -> Unit,
): ValueAnimator =
  ValueAnimator.ofFloat(0f, 1f).apply {
    this.duration = duration
    interpolator = curve
    addUpdateListener { onFrame(it.animatedValue as Float) }
    addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator) = onEnd()
    })
    start()
  }
