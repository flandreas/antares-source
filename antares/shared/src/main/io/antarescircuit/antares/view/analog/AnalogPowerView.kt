package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AnalogPower
import io.antarescircuit.antares.view.net.PowerViewShape
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider

class AnalogPowerView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: AnalogPower = AnalogPower()
) : AbstractAnalogVerticeView<AnalogPower>(styleProvider, model, Direction.NORTH, Rectangle2D()) {

	var voltage: Double
		get() = model.voltage
		set(value) {
			invalidate()
			model.voltage = value
			invalidate()
			validate()
		}

	init {
		orientation = Direction.SOUTH
	}

	override val relativeExternalLabelLocation: Point2D get() =
		Point2D(-(PowerViewShape.WIDTH + LENGTH) / 2, -PowerViewShape.HEIGHT / 2 - LABEL_DIST)

	override fun modelExchanged(oldModel: AnalogPower?) {
		super.modelExchanged(oldModel)
		addPortView(AnalogPortView(styleProvider, model.getOutput(), -LENGTH, 0, Direction.EAST))
		PowerViewShape.setBounds(this)
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		getPortViews().first().prepareConnectionDrawContext(context)
		PowerViewShape.drawBodyAt(-LENGTH.toDouble(), 0.0, context)
	}

	/** ---- [AbstractAnalogVerticeView] */

	override val mainPropertyValue: String get() = "$voltage V"

	override val mainPropertylabelOrientation: Direction get() = Direction.WEST

	override val mainPropertylabelLocation: Point2D
		// Basic view geometry: arrow head pointing towards west
		get() = Point2D(bounds.minX - MAIN_PROPERTY_LABEL_DIST, bounds.centerY)
}