package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.Battery
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.view.style.GraphStyleType
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView

/**
 * Port 1 is the plus pin, port 2 is the minus pin.
 */
class BatteryView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Battery = Battery()
) : AbstractAnalogVerticeView<Battery>(
	styleProvider,
	model,
	Direction.NORTH,
	Rectangle2D(LENGTH, -SIZE / 2, SIZE, SIZE)
) {

	companion object {
		private val SIZE = wInt(4)
		private val MINUS_STROKE = Stroke(3f)
	}

	@Suppress("unused") // Reflective bean property
	var voltage: Double
		get() = model.voltage
		set(value) {
			model.voltage = value
		}

	init {
		orientation = Direction.SOUTH
	}

	override val relativeExternalLabelLocation: Point2D get() = Point2D(LENGTH + SIZE / 2, -SIZE / 2 - LABEL_DIST)

	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: Battery?) {
		super.modelExchanged(oldModel)
		addPortView(AnalogPortView(styleProvider, model.getPort(1), LENGTH, 0, Direction.WEST))
		addPortView(AnalogPortView(styleProvider, model.getPort(2), LENGTH + SIZE, 0, Direction.EAST))
		updateGeometry()
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		// Plus bar
		(getPortView(model.getPort(1)) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)
		context.g.stroke = styleProvider.getStyle(GraphStyleType.EDGE).stroke
		context.g.drawLine(x, 0.0, x + SIZE / 2, 0.0)

		if (!context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			context.g.color = context.chooseForeground(foregroundColor)
		}
		context.g.stroke = stroke
		context.g.drawLine(x + SIZE / 2, y, x + SIZE / 2, y + SIZE)

		// Plus sign
		context.g.stroke = styleProvider.getStyle(GraphStyleType.EDGE).stroke
		context.g.drawLine(x + w(0.5), y, x + w(0.5), y + h(1))
		context.g.drawLine(x, y + h(0.5), x + w(1), y + h(0.5))

		// Minus bar
		(getPortView(model.getPort(2)) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)
		context.g.stroke = styleProvider.getStyle(GraphStyleType.EDGE).stroke
		context.g.drawLine(x + SIZE, 0.0, x + SIZE / 2 + w(1), 0.0)
		if (!context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			context.g.color = context.chooseForeground(foregroundColor)
		}
		context.g.stroke = MINUS_STROKE
		context.g.drawLine(x + SIZE / 2 + w(1), y + h(1), x + SIZE / 2 + w(1), y + SIZE - h(1))
	}

	/** ---- [AbstractAnalogVerticeView] */

	override val mainPropertyValue: String get() = "${model.voltage} V"
}