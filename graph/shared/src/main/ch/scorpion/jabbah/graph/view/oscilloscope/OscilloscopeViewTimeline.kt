package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.graph.model.oscilloscope.Oscilloscope

/**
 * A [SignalHistoryTimeline] implementation used for for [Oscilloscope].
 */
class OscilloscopeViewTimeline(
	override var scale: Double,
	private val model: Oscilloscope
) : SignalHistoryTimeline {

	override val maxTime: Long get() = model.maxTime

	override fun getDx(duration: Long): Double {
		return scale * duration / 20
	}

	override fun getX(time: Long): Double {
		return getDx(model.maxTime - time)
	}
}