package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.Themes

/**
 * Determines the [CompositeColor] in which an [AnalogSignal] is to be drawn during simulation.
 */
object AnalogSignalColor {

	// TODO Define a look-up table for a range of voltages between 0 and at least 5 volts

	private val LOW_COLOR = Themes.get<AntaresTheme>().zero
	private val HIGH_COLOR = Themes.get<AntaresTheme>().one

	fun ofSignal(signal: AnalogSignal): CompositeColor =
		if (signal.voltage < 1.5) LOW_COLOR else HIGH_COLOR
}