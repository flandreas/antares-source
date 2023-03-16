package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistories

/** Provides a transformation from execution time to model space coordinates used for drawing [SignalHistories]. */
interface SignalHistoryTimeline {

    var scale: Double

	val maxTime: Long

    /** Returns the distance on the x-axis for displaying a duration of [duration] ns.*/
    fun getDx(duration: Long): Double

    /** Returns the x-coordinate at which a signal that occurred at the specified execution time should be drawn.*/
    fun getX(time: Long): Double
}