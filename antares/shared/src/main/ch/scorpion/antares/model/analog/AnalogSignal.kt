package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogSignalColor
import ch.scorpion.jabbah.draw.graphics.CompositeColor

data class AnalogSignal(val voltage: Double): Comparable<AnalogSignal> {

	companion object {
		val ZERO = AnalogSignal(0.0)
		val HIGH = AnalogSignal(5.0)
		val UNDEFINED: AnalogSignal? = null
	}

	val color: CompositeColor by lazy { AnalogSignalColor.ofSignal(this) }

	override fun toString(): String = "$voltage V"

	override fun compareTo(other: AnalogSignal): Int =
		this.voltage.compareTo(other.voltage)
}