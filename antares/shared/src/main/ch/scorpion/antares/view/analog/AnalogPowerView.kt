package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogPower
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.net.PowerViewShape
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.jabbah.base.geom.Direction
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
		PowerViewShape.setBounds(this)
		orientation = Direction.SOUTH
	}

	override fun modelExchanged(oldModel: AnalogPower?) {
		super.modelExchanged(oldModel)
		val portView = AnalogPortView(
			styleProvider = styleProvider,
			port = model.getOutput(),
			direction = Direction.EAST)
		portView.setLocation(-AbstractAntaresPortView.LENGTH, 0)
		addPortView(portView)
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		getPortViews().first().prepareConnectionDrawContext(context)
		PowerViewShape.drawBodyAt(-AbstractAntaresPortView.LENGTH.toDouble(), 0.0, context)
	}
}