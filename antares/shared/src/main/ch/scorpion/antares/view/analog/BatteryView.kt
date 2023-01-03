package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.Battery
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

class BatteryView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Battery = Battery()
) : OrientableRectangularVerticeView<Battery>(styleProvider, model) {

	companion object {
		private val SIZE = wInt(4)
		private val MINUS_STROKE = Stroke(3f)
	}

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: Battery?) {
		super.modelExchanged(oldModel)
		addPortView(AnalogPortView(styleProvider, model.getPort(1), AbstractAntaresPortView.LENGTH, 0, Direction.WEST))
		addPortView(AnalogPortView(styleProvider, model.getPort(2), AbstractAntaresPortView.LENGTH + SIZE, 0, Direction.EAST))
		setBounds(AbstractAntaresPortView.LENGTH, -SIZE / 2, SIZE, SIZE)
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRect(xInt, yInt, widthInt, heightInt)
			}
		}

		context.g.color = context.chooseForeground(foregroundColor)

		// Plus bar
		context.g.stroke = stroke
		context.g.drawLine(x + SIZE / 2, y, x + SIZE / 2, y + SIZE)

		// Minus bar
		context.g.stroke = MINUS_STROKE
		context.g.drawLine(x + SIZE / 2 + w(1), y + h(1), x + SIZE / 2 + w(1), y + SIZE - h(1))

		context.g.stroke = styleProvider.getStyle(GraphStyleType.EDGE).stroke

		// Plus sign
		context.g.drawLine(x + w(0.5), y, x + w(0.5), y + h(1))
		context.g.drawLine(x, y + h(0.5), x + w(1), y + h(0.5))

		// Wires
		(getPortView(model.getPort(1)) as AnalogPortView).prepareConnectionDrawContext(context)
		context.g.drawLine(x, 0.0, x + SIZE / 2, 0.0)

		(getPortView(model.getPort(2)) as AnalogPortView).prepareConnectionDrawContext(context)
		context.g.drawLine(x + SIZE, 0.0, x + SIZE / 2 + w(1), 0.0)
	}
}