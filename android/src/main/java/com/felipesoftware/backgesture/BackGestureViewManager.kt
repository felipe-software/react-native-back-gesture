package com.felipesoftware.backgesture

import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.BackGestureViewManagerDelegate
import com.facebook.react.viewmanagers.BackGestureViewManagerInterface

@ReactModule(name = BackGestureViewManager.NAME)
class BackGestureViewManager :
  ViewGroupManager<BackGestureView>(),
  BackGestureViewManagerInterface<BackGestureView> {
  private val delegate = BackGestureViewManagerDelegate(this)

  override fun getDelegate(): ViewManagerDelegate<BackGestureView> = delegate

  override fun getName() = NAME

  override fun createViewInstance(context: ThemedReactContext) = BackGestureView(context)

  @ReactProp(name = "interceptsBack")
  override fun setInterceptsBack(view: BackGestureView, value: Boolean) {
    view.interceptsBack = value
  }

  @ReactProp(name = "systemBackAllowed")
  override fun setSystemBackAllowed(view: BackGestureView, value: Boolean) {
    view.systemBackAllowed = value
  }

  @ReactProp(name = "stackAnimation")
  override fun setStackAnimation(view: BackGestureView, value: String?) {
    view.stackAnimation = when (value) {
      "scale" -> StackAnimationStyle.SCALE
      "none" -> null
      else -> StackAnimationStyle.SLIDE
    }
  }

  @ReactProp(name = "sheetAnimation", defaultBoolean = true)
  override fun setSheetAnimation(view: BackGestureView, value: Boolean) {
    view.sheetAnimation = value
  }

  override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> =
    BACK_EVENT_NAMES.associateWith { name ->
      mapOf("registrationName" to "on" + name.removePrefix("top"))
    }

  companion object {
    const val NAME = "BackGestureView"
  }
}
