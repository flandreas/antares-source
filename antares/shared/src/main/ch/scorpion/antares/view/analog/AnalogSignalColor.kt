package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.CompositeColorGradient
import ch.scorpion.jabbah.draw.style.Themes

/**
 * Determines the [CompositeColor] in which an [AnalogSignal] is to be drawn during simulation.
 */
object AnalogSignalColor {

	private val GRADIENT = CompositeColorGradient(
		Themes.get<AntaresTheme>().one.backgroundColor,
		Themes.get<AntaresTheme>().zero.foregroundColor,
		Themes.get<AntaresTheme>().one.foregroundColor)

	private const val MIN_VOLTAGE = 0.0
	private const val MAX_VOLTAGE = 5.0

	fun ofSignal(signal: AnalogSignal): CompositeColor =
		GRADIENT.at((signal.voltage.coerceIn(MIN_VOLTAGE, MAX_VOLTAGE) / MAX_VOLTAGE).toFloat())
}