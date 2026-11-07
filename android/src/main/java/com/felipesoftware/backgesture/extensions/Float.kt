package com.felipesoftware.backgesture.extensions

import android.content.res.Resources

internal val Float.px: Float
  get() = this * Resources.getSystem().displayMetrics.density
