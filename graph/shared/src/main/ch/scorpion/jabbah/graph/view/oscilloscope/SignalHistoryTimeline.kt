package ch.scorpion.jabbah.graph.view.oscilloscope

/** Provides a transformation from execution time to model space coordinates used for drawing [SignalHistories][SignalHistory]. */
interface SignalHistoryTimeline {

    /** Returns the x-coordinate at which a signal that occured at the specified execution time should be drawn.*/
    fun getX(time: Long): Double
}