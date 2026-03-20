package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Power
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.graph.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

class PowerView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Power = Power()
) : OrientableRectangularVerticeView<Power>(styleProvider, model) {

	init {
		modelExchanged(null)
		PowerViewShape.setBounds(this)
		orientation = Direction.SOUTH
	}

	override fun modelExchanged(oldModel: Power?) {
		super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getOutput(),
			direction = Direction.EAST)
		portView.setLocation(-AbstractAntaresPortView.LENGTH, 0)
		addPortView(portView)
	}

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			invalidate()
			model.bitWidth = value
		}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		getPortViews().first().prepareConnectionDrawContext(context)
		PowerViewShape.drawBodyAt(-AbstractAntaresPortView.LENGTH.toDouble(), 0.0, context)
	}
}