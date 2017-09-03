package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistory

/** Draws a [SignalHistory] as a single row of an [OscilloscopeView]. */
interface SignalHistoryDrawer : RectangularDrawable {

    /** Binds this [SignalHistoryDrawer] with the data source it displays. */
    fun bind(signalHistory: SignalHistory<Any>?, timeline: SignalHistoryTimeline?)
}