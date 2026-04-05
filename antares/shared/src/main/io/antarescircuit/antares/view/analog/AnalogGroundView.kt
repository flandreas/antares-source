package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AnalogGround
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView

class AnalogGroundView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: AnalogGround = AnalogGround()
) : AbstractAnalogVerticeView<AnalogGround>(
	styleProvider,
	model,
	Direction.SOUTH,
	Rectangle2D(-SIZE / 2, LENGTH, SIZE, SIZE)
) {

	companion object {
		private val SIZE = wInt(4)

		private val TRIANGLE = System.createPath()
			.moveTo(w(-1.5), h(3.5))
			.lineTo(w(1.5), h(3.5))
			.lineTo(w(0), h(5.5))
			.close()
	}

	override val relativeExternalLabelLocation: Point2D get() = Point2D(0.0, bounds.maxY + LABEL_DIST)

	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: AnalogGround?) {
		super.modelExchanged(oldModel)
		addPortView(AnalogPortView(styleProvider, model.getPort(), 0, LENGTH, Direction.NORTH))
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fill(TRIANGLE)
			}
		}

		context.g.color = context.chooseForeground(foregroundColor)
		(getPortView(model.getPort()) as AbstractAntaresPortView).prepareConnectionDrawContext(context)
		context.g.drawLine(0.0, LENGTH.toDouble(), 0.0, h(3.5))

		context.g.color = context.chooseBackground(backgroundColor)
		context.g.fill(TRIANGLE)
		context.g.color = context.chooseForeground(foregroundColor)
		context.g.stroke = stroke
		context.g.draw(TRIANGLE)
	}
}