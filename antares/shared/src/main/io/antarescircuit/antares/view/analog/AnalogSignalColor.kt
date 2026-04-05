package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.graphics.CompositeColorGradient
import io.antarescircuit.jabbah.draw.style.Themes
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

	fun ofVoltage(voltage: Double): CompositeColor {
		return if (voltage >= 0 || abs(voltage) < AnalogSignal.VOLTAGE_SIGMA) {
			POS_GRADIENT.at((voltage.coerceIn(MIN_VOLTAGE, MAX_VOLTAGE) / MAX_VOLTAGE).toFloat())
		} else {
			NEG_GRADIENT.at((voltage.absoluteValue.coerceAtMost(MAX_VOLTAGE) / MAX_VOLTAGE).toFloat())
		}
	}

	fun ofSignal(signal: AnalogSignal): CompositeColor = ofVoltage(signal.voltage)
}