package com.felipesoftware.backgesture.animations

import androidx.activity.BackEventCompat
import com.felipesoftware.backgesture.events.BackGestureEmitter
import com.felipesoftware.backgesture.events.BackGestureEventType

internal class JsBackAnimation(start: BackEventCompat, private val emitter: BackGestureEmitter) : BackAnimation {
  private val swipeEdge = start.swipeEdge
  private var progress = 0f

  init {
    emit(BackGestureEventType.STARTED, start)
  }

  override fun update(event: BackEventCompat) = emit(BackGestureEventType.PROGRESSED, event)

  override fun cancel() = emitter.emit(BackGestureEventType.CANCELLED, progress, swipeEdge)

  override fun finish(onDone: () -> Unit) {
    emitter.emit(BackGestureEventType.INVOKED, progress, swipeEdge)
    onDone()
  }

  private fun emit(type: BackGestureEventType, event: BackEventCompat) {
    progress = GESTURE_CURVE.getInterpolation(event.progress)
    emitter.emit(type, progress, swipeEdge)
  }
}
