package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.CompositeColorGradient
import ch.scorpion.jabbah.draw.style.Themes
import kotlin.math.abs
import kotlin.math.absoluteValue

/**
 * Determines the [CompositeColor] in which an [AnalogSignal] is to be drawn during simulation.
 */
object AnalogSignalColor {

	private val MIN_NEG_COLOR = Color(112, 81, 42)
	private val MAX_NEG_COLOR = Color(247, 76, 16)

	private val POS_GRADIENT = CompositeColorGradient(
		Themes.get<AntaresTheme>().one.backgroundColor,
		Themes.get<AntaresTheme>().zero.foregroundColor,
		Themes.get<AntaresTheme>().one.foregroundColor)

	private val NEG_GRADIENT = CompositeColorGradient(
		Themes.get<AntaresTheme>().one.backgroundColor,
		MIN_NEG_COLOR,
		MAX_NEG_COLOR)

	private const val MIN_VOLTAGE = 0.0
	private const val MAX_VOLTAGE = 5.0

	fun ofVoltage(voltage: Double): CompositeColor =
		if (voltage >= 0 || abs(voltage) < 1E-3) {
			POS_GRADIENT.at((voltage.coerceIn(MIN_VOLTAGE, MAX_VOLTAGE) / MAX_VOLTAGE).toFloat())
		} else {
			NEG_GRADIENT.at((voltage.absoluteValue.coerceAtMost(MAX_VOLTAGE) / MAX_VOLTAGE).toFloat())
		}

	fun ofSignal(signal: AnalogSignal): CompositeColor = ofVoltage(signal.voltage)
}