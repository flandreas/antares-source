package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.graph.view.oscilloscope.AbstractSignalHistoryYAxis

class AnalogSignalHistoryYAxis(
	color: CompositeColor = Themes.get<AntaresTheme>().figure.color
) : AbstractSignalHistoryYAxis<AnalogSignal>(Rectangle2D(0, 0, WIDTH, 0), color) {

	companion object {
		const val FACTOR = 8
		private const val WIDTH = 40
		private const val SCALE_WIDTH = 5
	}

	private val label = Label(
		"${AnalogSignal.HIGH.voltage} V",
		Themes.get<AntaresTheme>().annotation.font,
		Themes.get<AntaresTheme>().figure.color.textColor,
		HorizontalAlignment.LEFT,
		VerticalAlignment.CENTER)

	override val lineWidth: Double get() = 0.0

	override fun signalY(signal: AnalogSignal): Double =
		FACTOR * signal.voltage

	override fun drawScale(context: DrawContext) {
		val fiveVoltY = baselineY - signalY(AnalogSignal.HIGH)
		context.g.color = color.foregroundColor
		context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
		context.g.drawLine(bounds.minX, fiveVoltY, bounds.minX - SCALE_WIDTH, fiveVoltY)

		label.location = Point2D(bounds.minX + SCALE_WIDTH, fiveVoltY)
		label.draw(context)
	}
}