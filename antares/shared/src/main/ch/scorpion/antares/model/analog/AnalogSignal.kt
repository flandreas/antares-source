package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogSignalColor
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import kotlin.math.abs
import kotlin.math.roundToInt

data class AnalogSignal(val voltage: Double): Comparable<AnalogSignal> {

	companion object {
		val ZERO = AnalogSignal(0.0)
		val HIGH = AnalogSignal(5.0)
		val UNDEFINED: AnalogSignal? = null

		const val VOLTAGE_SIGMA = 0.005

		private fun roundValue(v: Double, f: Int): String = (((v * f).roundToInt()) / f.toDouble()).toString()

		fun roundVoltage(v: Double, f: Int = 10): String = roundValue(v, f)

		fun roundAbsCurrent(c: Double, f: Int = 1000): String = roundValue(abs(c), f)
	}

	val color: CompositeColor by lazy { AnalogSignalColor.ofSignal(this) }

	override fun toString(): String = "${roundedDesc()} V"

	override fun compareTo(other: AnalogSignal): Int =
		this.voltage.compareTo(other.voltage)

	fun roundedDesc(f: Int = 10): String = roundVoltage(voltage, f)
}