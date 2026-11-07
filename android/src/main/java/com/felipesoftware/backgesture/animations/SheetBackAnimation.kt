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

  // React Native's back handling dismisses the sheet. The shrink is undone once the sheet has slid away,
  // so it doesn't snap back to full size first.
  override fun finish(onDone: () -> Unit) {
    onDone()
    Handler(Looper.getMainLooper()).postDelayed(sheet::cancelBackProgress, RESET_DELAY_MS)
  }
}
