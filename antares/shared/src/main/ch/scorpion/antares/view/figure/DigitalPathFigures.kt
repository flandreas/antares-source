package ch.scorpion.antares.view.figure

import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.polyline.PolylineDrawable
import ch.scorpion.jabbah.edit.figure.Figure
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponent

class AndGateFigure : AbstractPathFigure(SymbolStyle.AND_PATH, TYPE) {
	companion object {
		val TYPE: String by lazy { Translations.getString("antares.figure.andShape") }
	}
}

class OrGateFigure : AbstractPathFigure(SymbolStyle.OR_PATH, TYPE) {
	companion object {
		val TYPE: String by lazy { Translations.getString("antares.figure.orShape") }
	}
}

class NotGateFigure : AbstractPathFigure(SymbolStyle.NOT_PATH, TYPE) {
	companion object {
		val TYPE: String by lazy { Translations.getString("antares.figure.notShape") }
	}
}

fun createMultiplexerFigure(): Figure =
	PolylineComponent(PolylineDrawable()
		.addPoint(0.0, 0.0)
		.addPoint(0.0, 12.0 * SCALE)
		.addPoint(6.0 * SCALE, 10.0 * SCALE)
		.addPoint(6.0 * SCALE, 2.0 * SCALE)
		.addPoint(0.0, 0.0))

fun createAluFigure(): Figure =
	PolylineComponent(PolylineDrawable()
		.addPoint(0.0, 0.0)
		.addPoint(4.0 * SCALE, 12.0 * SCALE)
		.addPoint(12.0 * SCALE, 12.0 * SCALE)
		.addPoint(16.0 * SCALE, 0.0)
		.addPoint(11.0 * SCALE, 0.0)
		.addPoint(10.0 * SCALE, 2.0 * SCALE)
		.addPoint(6.0 * SCALE, 2.0 * SCALE)
		.addPoint(5.0 * SCALE, 0.0)
		.addPoint(0.0, 0.0))
