package io.antarescircuit.antares.view.net

import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.RectangularDrawable
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.graph.view.style.GraphTheme

object PowerViewShape {

	const val WIDTH = 2.0 * Look.SCALE
	const val HEIGHT = 2.0 * Look.SCALE
	private const val ARROW_WIDTH = 1.0 * Look.SCALE
	private const val ARROW_HEIGHT = 1.25 * Look.SCALE

	fun drawBodyAt(x: Double, y: Double, context: DrawContext) {
		context.g.drawLine(x, y, x - WIDTH,0.0)

		val minX = x - WIDTH
		context.g.stroke = Themes.get<GraphTheme>().figure.stroke
		context.g.drawLine(minX, y, minX + ARROW_WIDTH, -ARROW_HEIGHT / 2)
		context.g.drawLine(minX, y, minX + ARROW_WIDTH, +ARROW_HEIGHT / 2)
	}

	fun setBounds(drawable: RectangularDrawable) {
		drawable.setBounds(
			-AbstractAntaresPortView.LENGTH - WIDTH, -HEIGHT / 2,
			WIDTH, HEIGHT
		)
	}
}