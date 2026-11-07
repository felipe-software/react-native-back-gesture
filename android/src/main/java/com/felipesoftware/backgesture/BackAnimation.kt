package com.felipesoftware.backgesture

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.PathInterpolator
import androidx.activity.BackEventCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.swmansion.rnscreens.Screen
import com.swmansion.rnscreens.ScreenStack

internal const val SCRIM_DIM = 0.2f
internal const val FINISH_DURATION_MS = 220L
internal const val CANCEL_DURATION_MS = 180L
private const val POP_TIMEOUT_MS = 1000L
private const val SHEET_RESET_DELAY_MS = 500L

// The curve Android's own cross-activity back animation applies to gesture progress.
internal val GESTURE_CURVE: Interpolator = PathInterpolator(0.1f, 0.1f, 0f, 1f)
internal val FINISH_CURVE: Interpolator = PathInterpolator(0.4f, 0f, 0.2f, 1f)
internal val CANCEL_CURVE: Interpolator = DecelerateInterpolator()

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
  private val scene: StackScene,
  private val topScreen: Screen,
  private val below: Fragment,
  private val attachedBelow: Boolean,
  private val fragmentManager: FragmentManager,
  private val motion: StackMotion,
) : BackAnimation {
  private val mainHandler = Handler(Looper.getMainLooper())
  private var progress = 0f
  private var touchY = scene.startTouchY
  private var settling: ValueAnimator? = null
  private var awaitingPop = false

  init {
    scene.begin()
    motion.begin()
    motion.render(progress, touchY)
  }

  override fun update(event: BackEventCompat) {
    progress = motion.progressOf(event)
    touchY = event.touchY
    motion.render(progress, touchY)
  }

  override fun cancel() {
    val from = progress
    settling = animate(motion.cancelDurationMs, motion.cancelCurve, onEnd = ::restore) { fraction ->
      progress = from * (1f - fraction)
      motion.render(progress, touchY)
    }
  }

  fun endCancel() {
    settling?.end()
  }

  fun isAwaitingPop() = awaitingPop && scene.stack.topScreen === topScreen

  override fun finish(onDone: () -> Unit) {
    val from = progress
    awaitingPop = true
    animate(motion.finishDurationMs, motion.finishCurve, onEnd = {
      scene.releaseBelow()
      onDone()
      mainHandler.postDelayed(::restoreIfNotPopped, POP_TIMEOUT_MS)
    }) { fraction -> motion.finishFrame(from, fraction, touchY) }
  }

  private fun restoreIfNotPopped() {
    awaitingPop = false
    if (scene.stack.topScreen === topScreen) restore()
  }

  private fun restore() {
    settling = null
    scene.reset()
    if (attachedBelow && below.isAdded && !fragmentManager.isDestroyed) detach(fragmentManager, below)
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
      val fragmentManager = wrapper.fragment.parentFragmentManager
      val attachedBelow = !below.isAdded
      // A pushed page detaches the one under it, so bring it back only for as long as the gesture needs it.
      if (attachedBelow) {
        fragmentManager
          .beginTransaction()
          .setReorderingAllowed(true)
          .add(stack.id, below)
          .commitNowAllowingStateLoss()
      }
      val belowView = below.view
      if (belowView == null) {
        if (attachedBelow) detach(fragmentManager, below)
        return null
      }
      val direction = if (start.swipeEdge == BackEventCompat.EDGE_RIGHT) -1f else 1f
      val scene = StackScene(stack, top, belowView, direction, start.touchY)
      val motion = when (style) {
        StackAnimationStyle.SLIDE -> SlideMotion(scene)
        StackAnimationStyle.SCALE -> ScaleMotion(scene)
        StackAnimationStyle.IOS -> IosMotion(scene)
        StackAnimationStyle.CARD -> CardMotion(scene)
        StackAnimationStyle.SHARED_AXIS -> SharedAxisMotion(scene)
      }
      return StackBackAnimation(scene, wrapper.screen, below, attachedBelow, fragmentManager, motion)
    }
  }
}

private fun detach(fragmentManager: FragmentManager, fragment: Fragment) {
  fragmentManager
    .beginTransaction()
    .setReorderingAllowed(true)
    .remove(fragment)
    .commitNowAllowingStateLoss()
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
