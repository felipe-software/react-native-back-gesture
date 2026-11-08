package com.felipesoftware.backgesture.view

import android.os.Build
import androidx.activity.ComponentActivity
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.views.view.ReactViewGroup
import com.felipesoftware.backgesture.events.BackGestureEvent
import com.felipesoftware.backgesture.events.BackGestureEventType
import com.felipesoftware.backgesture.router.BackRouter
import com.felipesoftware.backgesture.router.BackRouterOptions

class BackGestureView(private val reactContext: ThemedReactContext) : ReactViewGroup(reactContext) {
  internal val options = BackRouterOptions()
  private var router: BackRouter? = null

  var systemBackAllowed = false
    set(value) {
      field = value
      router?.setSystemBackAllowed(value)
    }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    // From Android 13, opting into predictive back stops onBackPressed, so the router has to forward back
    // to React Native. Gesture progress only arrives from Android 14.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val activity = reactContext.currentActivity as? ComponentActivity ?: return
    router = BackRouter(activity, options, ::emit).also { it.setSystemBackAllowed(systemBackAllowed) }
  }

  override fun onDetachedFromWindow() {
    router?.release()
    router = null
    super.onDetachedFromWindow()
  }

  // getEventDispatcher(context) only exists from React Native 0.85, and 0.76 is still supported.
  @Suppress("DEPRECATION")
  private fun emit(type: BackGestureEventType, progress: Float, swipeEdge: Int) {
    val event = BackGestureEvent(UIManagerHelper.getSurfaceId(this), id, type, progress, swipeEdge)
    UIManagerHelper.getEventDispatcherForReactTag(reactContext, id)?.dispatchEvent(event)
  }
}
