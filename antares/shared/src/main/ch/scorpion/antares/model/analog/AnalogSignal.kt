package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogSignalColor
import ch.scorpion.jabbah.draw.graphics.CompositeColor

data class AnalogSignal(val voltage: Float) {

	val color: CompositeColor by lazy { AnalogSignalColor.ofSignal(this) }
}