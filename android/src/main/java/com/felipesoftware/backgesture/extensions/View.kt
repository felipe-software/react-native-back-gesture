package com.felipesoftware.backgesture.extensions

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.swmansion.rnscreens.ScreenStack

internal fun ScreenStack.topIndex(): Int = fragments.indexOfLast { it.screen === topScreen }

internal fun View.findVisibleSheet(): BottomSheetBehavior<*>? {
  if (visibility != View.VISIBLE) return null
  val behavior = (layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? BottomSheetBehavior<*>
  if (behavior != null && behavior.state != BottomSheetBehavior.STATE_HIDDEN) return behavior
  return firstFromTop { it.findVisibleSheet() }
}

internal fun View.findVisibleStack(): ScreenStack? {
  if (visibility != View.VISIBLE) return null
  firstFromTop { it.findVisibleStack() }?.let { return it }
  return (this as? ScreenStack)?.takeIf { it.topIndex() > 0 && it.getGlobalVisibleRect(Rect()) }
}

internal fun View.resetTransform() {
  translationX = 0f
  translationY = 0f
  translationZ = 0f
  scaleX = 1f
  scaleY = 1f
  alpha = 1f
}

private fun <T> View.firstFromTop(find: (View) -> T?): T? {
  if (this !is ViewGroup) return null
  for (index in childCount - 1 downTo 0) find(getChildAt(index))?.let { return it }
  return null
}
