package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.view.oscilloscope.AbstractSignalHistoryYAxis

class AnalogSignalHistoryYAxis(
	topInset: Int = DEF_TOP_INSET,
	bottomInset: Int = DEF_BOTTOM_INSET,
	defaultValue: AnalogSignal = AnalogSignal.HIGH,
	defaultValueTopInset: Int = DEF_DEFAULT_VALUE_TO_INSET,
	color: CompositeColor = Themes.get<AntaresTheme>().figure.color
) : AbstractSignalHistoryYAxis<AnalogSignal>(topInset, bottomInset, defaultValue, defaultValueTopInset, color) {

	companion object {
		const val WIDTH = 40
	}

	/** ---- [RectangularDrawable] */

	override val lineWidth: Double get() = 0.0

	/** ---- [AbstractSignalHistoryYAxis] */

	override val preferredWidth: Int get() = WIDTH

	override fun toMetric(signal: AnalogSignal): Double = signal.voltage
}