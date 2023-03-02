package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogSignalColor
import ch.scorpion.jabbah.draw.graphics.CompositeColor

data class AnalogSignal(val voltage: Double) {

	companion object {
		val ZERO = AnalogSignal(0.0)
	}

	val color: CompositeColor by lazy { AnalogSignalColor.ofSignal(this) }

	override fun toString(): String = "$voltage V"
}