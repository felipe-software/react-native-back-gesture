package com.felipesoftware.backgesture

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import com.felipesoftware.backgesture.view.BackGestureViewManager

class BackGesturePackage : ReactPackage {
  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> =
    listOf(BackGestureViewManager())
}
