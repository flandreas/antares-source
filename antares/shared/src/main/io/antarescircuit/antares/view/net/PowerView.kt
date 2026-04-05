package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.net.Power
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.graph.view.OrientableRectangularVerticeView
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.GraphApplicationContext

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
		getPortView(model.getOutput())!!.prepareConnectionDrawContext(context)
		if (!context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
			context.g.color = context.chooseForeground(foregroundColor)
		}
		PowerViewShape.drawBodyAt(-AbstractAntaresPortView.LENGTH.toDouble(), 0.0, context)
	}

	override fun getEditPortViewColor(styleProvider: StyleProvider): CompositeColor =
		customColor?.color ?: super.getEditPortViewColor(styleProvider)
}