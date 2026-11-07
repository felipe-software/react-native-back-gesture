package com.felipesoftware.backgesture.events

import androidx.activity.BackEventCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

internal enum class BackGestureEventType(val eventName: String) {
  STARTED("topBackStarted"),
  PROGRESSED("topBackProgressed"),
  CANCELLED("topBackCancelled"),
  INVOKED("topBackInvoked"),
  ;

  val registrationName = "on" + eventName.removePrefix("top")
}

internal fun interface BackGestureEmitter {
  fun emit(type: BackGestureEventType, progress: Float, swipeEdge: Int)
}

internal class BackGestureEvent(
  surfaceId: Int,
  viewId: Int,
  private val type: BackGestureEventType,
  private val progress: Float,
  private val swipeEdge: Int,
) : Event<BackGestureEvent>(surfaceId, viewId) {
  override fun getEventName() = type.eventName

  override fun canCoalesce() = type == BackGestureEventType.PROGRESSED

  override fun getEventData(): WritableMap =
    Arguments.createMap().apply {
      putDouble("progress", progress.toDouble())
      putString("edge", if (swipeEdge == BackEventCompat.EDGE_RIGHT) "right" else "left")
    }
}
