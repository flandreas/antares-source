package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.AnalogSignalColor
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.graph.model.oscilloscope.Oscilloscope
import io.antarescircuit.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * An [AnalogSignal] represents first and foremost an electrical [voltage].
 * For usages in [Oscilloscope], it can optionally be enriched with an electrical [current].
 * If [current] is set, it is the dominant signal sensed by a [OscilloscopeProbeVertice],
 * else the [voltage] (default) is the dominant signal.
 */
data class AnalogSignal(
	val voltage: Double,
	val current: Double? = null
): Comparable<AnalogSignal> {

	companion object {
		val ZERO_VOLTAGE = AnalogSignal(0.0)
		val HIGH_VOLTAGE = AnalogSignal(5.0)
		val UNDEFINED: AnalogSignal? = null

		val DEFAULT_CURRENT = AnalogSignal(0.0, 0.05)

		const val VOLTAGE_SIGMA = 0.005

		private fun roundValue(v: Double, f: Int): String = (((v * f).roundToInt()) / f.toDouble()).toString()

		fun roundVoltage(v: Double, f: Int = 10): String = roundValue(v, f)

		fun roundCurrent(v: Double, f: Int = 1000): String = roundValue(v, f)

		fun roundAbsCurrent(c: Double, f: Int = 1000): String = roundValue(abs(c), f)
	}

	val dominantValue: Double get() = if (current != null) current else voltage

	val color: CompositeColor by lazy { AnalogSignalColor.ofSignal(this) }

	override fun toString(): String = if (current != null) "${roundedDesc(1000)} A" else "${roundedDesc()} V"

	override fun compareTo(other: AnalogSignal): Int =
		if (current != null && other.current != null) this.current compareTo(other.current) else this.voltage.compareTo(other.voltage)

	private fun roundedDesc(f: Int = 10): String = if (current != null) roundCurrent(current, f) else roundVoltage(voltage, f)
}