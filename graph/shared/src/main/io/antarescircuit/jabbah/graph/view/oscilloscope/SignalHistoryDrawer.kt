package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.draw.drawable.RectangularDrawable
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistory

/** Draws a [SignalHistory] as a single row of an [OscilloscopeView]. */
interface SignalHistoryDrawer<T: Any> : RectangularDrawable {

	/** Binds this [SignalHistoryDrawer] with the data source it displays. */
	fun bind(
		signalHistory: SignalHistory<T>?,
		gridSignalHistory: SignalHistory<T>?,
		timeline: SignalHistoryTimeline?,
		color: CompositeColor
	)
}