package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
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
		private const val SCALE_WIDTH = 5
	}

	private val label = Label(
		"${AnalogSignal.HIGH.voltage} V",
		Themes.get<AntaresTheme>().annotation.font,
		Themes.get<AntaresTheme>().figure.color.textColor,
		HorizontalAlignment.LEFT,
		VerticalAlignment.CENTER)

	/** ---- [RectangularDrawable] */

	override val lineWidth: Double get() = 0.0

	/** ---- [AbstractSignalHistoryYAxis] */

	override val preferredWidth: Int get() = WIDTH

	override fun toMetric(signal: AnalogSignal): Double = signal.voltage

	override fun drawRuler(context: DrawContext) {
		val fiveVoltY = baselineY + signalY(AnalogSignal.HIGH)
		context.g.color = color.foregroundColor
		context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
		context.g.drawLine(bounds.minX, fiveVoltY, bounds.minX - SCALE_WIDTH, fiveVoltY)

		label.location = Point2D(bounds.minX + SCALE_WIDTH, fiveVoltY)
		label.draw(context)
	}
}