package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistories

/** Provides a transformation from execution time to model space coordinates used for drawing [SignalHistories]. */
interface SignalHistoryTimeline {

    var scale: Double

	/** Horizontal displacement of the triggered-sweep grid from its centered position. */
	var triggerOffsetX: Double

	/** Width of one visual time division in model coordinates. */
	var divisionWidth: Double

	val maxTime: Long

    /** Returns the distance on the x-axis for displaying a duration of [duration] ns.*/
    fun getDx(duration: Long): Double

    /** Returns the x-coordinate at which a signal that occurred at the specified execution time should be drawn.*/
    fun getX(time: Long): Double

    /** Returns the execution time drawn at the specified x-coordinate.*/
    fun getTime(x: Double): Long
}
