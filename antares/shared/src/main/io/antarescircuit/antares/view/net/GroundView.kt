package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.net.Ground
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.graph.view.OrientableRectangularVerticeView
import io.antarescircuit.jabbah.edit.Look.SCALE
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.GraphApplicationContext

class GroundView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Ground = Ground()
) : OrientableRectangularVerticeView<Ground>(styleProvider, model) {

	companion object {
		private const val WIDTH = 2.0 * SCALE
		private const val HEIGHT = 2.0 * SCALE
		private const val BAR_WIDTH = 0.5 * SCALE

		fun drawBodyAt(x: Double, y: Double, context: DrawContext) {
			val barRightX = x - WIDTH + BAR_WIDTH
			context.g.drawLine(x, y, barRightX, y)
			context.g.fillRect(x - WIDTH, y - HEIGHT / 2, BAR_WIDTH, HEIGHT)
		}
	}

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			invalidate()
			model.bitWidth = value
		}

	init {
		modelExchanged(null)
		setBounds(
			-AbstractAntaresPortView.LENGTH - WIDTH, -HEIGHT / 2,
			WIDTH, HEIGHT
		)
		orientation = Direction.NORTH
	}

	override fun modelExchanged(oldModel: Ground?) {
		super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getOutput(),
			direction = Direction.EAST)
		portView.setLocation(-AbstractAntaresPortView.LENGTH, 0)
		addPortView(portView)
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		getPortView(model.getOutput())!!.prepareConnectionDrawContext(context)
		if (!context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
			context.g.color = context.chooseForeground(foregroundColor)
		}
		drawBodyAt(-AbstractAntaresPortView.LENGTH.toDouble(), 0.0, context)
	}

	override fun getEditPortViewColor(styleProvider: StyleProvider): CompositeColor =
		customColor?.color ?: super.getEditPortViewColor(styleProvider)
}