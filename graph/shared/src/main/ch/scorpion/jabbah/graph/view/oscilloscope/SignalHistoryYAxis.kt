package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.Disposable
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistory

/**
 * A scalable y-axis for drawing signals, as well as a graphical representation
 * of such a y-axis including scale marks. Used together with [SignalHistoryDrawer].
 *
 * Depending on the range of signals in the [SignalHistory] being drawn,
 * a [SignalHistoryYAxis] is expected to adjust its scale to make the entire
 * signal curve visible.
 */
interface SignalHistoryYAxis<T: Any> : RectangularDrawable, Disposable {

	val preferredWidth: Int

	/**
	 * The y-coordinate of the baseline (where 0 values are located at)
	 * relative to the geometry of this [RectangularDrawable].
	 */
	val baselineY: Double

	/**
	 * The maximum height of the signal, i.e. the vertical distance in model coordinates
	 * between min and max signals.
	 */
	val signalHeight: Double

	/** Converts [signal] to a metric value used for scaling. */
	fun toMetric(signal: T): Double

	fun setMinMax(min: T?, max: T?)

	/**
	 * The distance in y direction from the baseline where [signal] is to be drawn.
	 */
	fun signalY(signal: T): Double

	/**
	 * Informs this [SignalHistoryYAxis] that it has been loaded with the specified [OscilloscopeProbeVertice],
	 * so that it can adjust to its settings, if needed.
	 */
	fun loadedWith(probe: OscilloscopeProbeVertice<*>) {}
}