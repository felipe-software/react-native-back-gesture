package com.felipesoftware.backgesture.extensions

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.facebook.react.ReactActivity

// Keep in sync with the check in ReactActivity.onCreate.
internal val ComponentActivity.reactRegistersBackCallback: Boolean
  get() =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA &&
      applicationInfo.targetSdkVersion >= Build.VERSION_CODES.BAKLAVA

internal fun ComponentActivity.reactBackCallback(): OnBackPressedCallback? =
  runCatching {
    // Looked up by type because the field is private and R8 renames it in release builds.
    ReactActivity::class.java.declaredFields
      .first { OnBackPressedCallback::class.java.isAssignableFrom(it.type) }
      .apply { isAccessible = true }
      .get(this) as OnBackPressedCallback
  }.getOrNull()
