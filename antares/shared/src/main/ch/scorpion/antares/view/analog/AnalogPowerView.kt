package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogPower
import ch.scorpion.antares.view.net.PowerViewShape
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

class AnalogPowerView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: AnalogPower = AnalogPower()
) : AbstractAnalogVerticeView<AnalogPower>(styleProvider, model) {

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

	override fun modelExchanged(oldModel: AnalogPower?) {
		super.modelExchanged(oldModel)
		addPortView(AnalogPortView(styleProvider, model.getOutput(), -LENGTH, 0, Direction.EAST))
		PowerViewShape.setBounds(this)
		updateLabel()
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		getPortViews().first().prepareConnectionDrawContext(context)
		PowerViewShape.drawBodyAt(-LENGTH.toDouble(), 0.0, context)
	}

	/** ---- [AbstractAnalogVerticeView] */

	override val mainPropertyValue: String get() = "$voltage V"

	override val labelOrientation: Direction get() = Direction.WEST

	override val labelLocation: Point2D
		// Basic view geometry: arrow head pointing towards west
		get() = Point2D(bounds.minX - MAIN_PROPERTY_LABEL_DIST, bounds.centerY)
}