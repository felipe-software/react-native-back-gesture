package com.felipesoftware.backgesture.animations.stack

import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import androidx.activity.BackEventCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.felipesoftware.backgesture.animations.BackAnimation
import com.felipesoftware.backgesture.extensions.topIndex
import com.swmansion.rnscreens.Screen
import com.swmansion.rnscreens.ScreenStack

private const val POP_TIMEOUT_MS = 1000L

internal class StackBackAnimation private constructor(
  private val scene: StackScene,
  private val motion: StackMotion,
  private val topScreen: Screen,
  private val fragmentManager: FragmentManager,
  private val below: Fragment,
  private val attachedBelow: Boolean,
) : BackAnimation {
  private val mainHandler = Handler(Looper.getMainLooper())
  private var progress = 0f
  private var settling: ValueAnimator? = null
  private var awaitingPop = false

  init {
    render(progress)
  }

  override fun update(event: BackEventCompat) {
    scene.track(event)
    progress = motion.progressOf(event, scene)
    render(progress)
  }

  override fun cancel() {
    val from = progress
    settling = motion.cancel.animate(onFrame = { render(from * (1f - it)) }, onEnd = ::restore)
  }

  override fun finish(onDone: () -> Unit) {
    val from = progress
    awaitingPop = true
    motion.finish.animate(onFrame = { scene.render(motion.finishFrame(from, it, scene)) }) {
      scene.releaseBelow()
      onDone()
      mainHandler.postDelayed(::restoreIfNotPopped, POP_TIMEOUT_MS)
    }
  }

  fun endCancel() {
    settling?.end()
  }

  fun isAwaitingPop() = awaitingPop && scene.stack.topScreen === topScreen

  private fun render(progress: Float) = scene.render(motion.frame(progress, scene))

  private fun restoreIfNotPopped() {
    awaitingPop = false
    if (scene.stack.topScreen === topScreen) restore()
  }

  private fun restore() {
    settling = null
    scene.reset()
    if (attachedBelow) fragmentManager.removeNow(below)
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
      if (attachedBelow) fragmentManager.addNow(stack.id, below)
      val belowView = below.view
      if (belowView == null) {
        if (attachedBelow) fragmentManager.removeNow(below)
        return null
      }
      val scene = StackScene(stack, top, belowView, start, style.motion.backdrop)
      return StackBackAnimation(scene, style.motion, wrapper.screen, fragmentManager, below, attachedBelow)
    }
  }
}

private fun FragmentManager.addNow(containerId: Int, fragment: Fragment) {
  beginTransaction().setReorderingAllowed(true).add(containerId, fragment).commitNowAllowingStateLoss()
}

private fun FragmentManager.removeNow(fragment: Fragment) {
  if (!fragment.isAdded || isDestroyed) return
  beginTransaction().setReorderingAllowed(true).remove(fragment).commitNowAllowingStateLoss()
}
