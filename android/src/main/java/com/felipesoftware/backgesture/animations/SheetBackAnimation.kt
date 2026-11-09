package com.felipesoftware.backgesture.animations

import android.os.Handler
import android.os.Looper
import androidx.activity.BackEventCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior

private const val RESET_DELAY_MS = 500L

internal class SheetBackAnimation(private val sheet: BottomSheetBehavior<*>, start: BackEventCompat) : BackAnimation {
  init {
    sheet.startBackProgress(start)
  }

  override fun update(event: BackEventCompat) = sheet.updateBackProgress(event)

  override fun cancel() = sheet.cancelBackProgress()

  override fun finish(onDone: () -> Unit) {
    onDone()
    Handler(Looper.getMainLooper()).postDelayed(sheet::cancelBackProgress, RESET_DELAY_MS)
  }
}
