package com.felipesoftware.backgesture.router

import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.felipesoftware.backgesture.animations.BackAnimation
import com.felipesoftware.backgesture.animations.JsBackAnimation
import com.felipesoftware.backgesture.animations.SheetBackAnimation
import com.felipesoftware.backgesture.animations.stack.StackAnimationStyle
import com.felipesoftware.backgesture.animations.stack.StackBackAnimation
import com.felipesoftware.backgesture.events.BackGestureEmitter
import com.felipesoftware.backgesture.extensions.findVisibleSheet
import com.felipesoftware.backgesture.extensions.findVisibleStack
import com.felipesoftware.backgesture.extensions.reactBackCallback
import com.felipesoftware.backgesture.extensions.reactRegistersBackCallback

private const val EDGE_NONE = 2

internal class BackRouterOptions {
  var interceptsBack = false
  var stackAnimation: StackAnimationStyle? = StackAnimationStyle.SLIDE
  var sheetAnimation = true
}

internal class BackRouter(
  private val activity: ComponentActivity,
  private val options: BackRouterOptions,
  private val emitter: BackGestureEmitter,
) : OnBackPressedCallback(true) {
  private val reactCallback = activity.reactBackCallback()

  private val registeredReactCallback = reactCallback?.takeUnless { activity.reactRegistersBackCallback }
  private var animation: BackAnimation? = null
  private var lastStackAnimation: StackBackAnimation? = null
  private var systemBackAllowed = false

  init {
    registeredReactCallback?.let { activity.onBackPressedDispatcher.addCallback(activity, it) }
    activity.onBackPressedDispatcher.addCallback(activity, this)
  }

  override fun handleOnBackStarted(backEvent: BackEventCompat) {
    animation = createAnimation(backEvent)
  }

  override fun handleOnBackProgressed(backEvent: BackEventCompat) {
    animation?.update(backEvent)
  }

  override fun handleOnBackCancelled() {
    animation?.cancel()
    animation = null
  }

  override fun handleOnBackPressed() {
    val finishing = animation
    animation = null
    if (finishing == null) forwardToReact() else finishing.finish(::forwardToReact)
  }

  fun release() {
    remove()
    registeredReactCallback?.remove()
    reactCallback?.isEnabled = true
  }

  fun setSystemBackAllowed(allowed: Boolean) {
    systemBackAllowed = allowed && reactCallback != null
    isEnabled = !systemBackAllowed
    reactCallback?.isEnabled = !systemBackAllowed
  }

  private fun forwardToReact() {
    isEnabled = false
    activity.onBackPressedDispatcher.onBackPressed()
    isEnabled = !systemBackAllowed
  }

  private fun createAnimation(event: BackEventCompat): BackAnimation? {
    if (event.swipeEdge == EDGE_NONE) return null
    lastStackAnimation?.let { previous ->
      previous.endCancel()
      if (previous.isAwaitingPop()) return null
    }
    if (options.interceptsBack) return JsBackAnimation(event, emitter)
    val root = activity.window.decorView
    root.findVisibleSheet()?.let { sheet ->
      return if (options.sheetAnimation && sheet.isHideable) SheetBackAnimation(sheet, event) else null
    }
    val style = options.stackAnimation ?: return null
    val stack = root.findVisibleStack() ?: return null
    return StackBackAnimation.create(stack, event, style)?.also { lastStackAnimation = it }
  }
}
