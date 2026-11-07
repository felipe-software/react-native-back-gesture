package com.felipesoftware.backgesture

import androidx.activity.BackEventCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

internal const val BACK_STARTED = "topBackStarted"
internal const val BACK_PROGRESSED = "topBackProgressed"
internal const val BACK_CANCELLED = "topBackCancelled"
internal const val BACK_INVOKED = "topBackInvoked"

internal val BACK_EVENT_NAMES = listOf(BACK_STARTED, BACK_PROGRESSED, BACK_CANCELLED, BACK_INVOKED)

internal class BackGestureEvent(
  surfaceId: Int,
  viewId: Int,
  private val name: String,
  private val progress: Float,
  private val swipeEdge: Int,
) : Event<BackGestureEvent>(surfaceId, viewId) {
  override fun getEventName() = name

  override fun canCoalesce() = name == BACK_PROGRESSED

  override fun getEventData(): WritableMap =
    Arguments.createMap().apply {
      putDouble("progress", progress.toDouble())
      putString("edge", if (swipeEdge == BackEventCompat.EDGE_RIGHT) "right" else "left")
    }
}
