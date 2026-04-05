package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.graph.model.oscilloscope.Oscilloscope

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

	override fun getTime(x: Double): Long = maxTime - getDTime(x)

	private fun getDTime(dx: Double): Long = (dx * FACTOR / scale).toLong()
}