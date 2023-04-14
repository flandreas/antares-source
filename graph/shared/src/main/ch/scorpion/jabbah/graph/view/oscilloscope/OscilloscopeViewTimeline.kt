package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.graph.model.oscilloscope.Oscilloscope

/**
 * A [SignalHistoryTimeline] implementation used for [Oscilloscope].
 */
class OscilloscopeViewTimeline(
	override var scale: Double,
	private val maxTimeProvider: () -> Long
) : SignalHistoryTimeline {

	companion object {
		private const val FACTOR = 10_000
	}

	override val maxTime: Long get() = maxTimeProvider()

	override fun getDx(duration: Long): Double = scale * duration / FACTOR

	override fun getX(time: Long): Double = getDx(maxTime - time)
}