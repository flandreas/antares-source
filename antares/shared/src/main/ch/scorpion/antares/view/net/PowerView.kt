package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Power
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

class PowerView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Power = Power()
) : DigitalComponentView<Power>(styleProvider, model) {

	companion object {
		private const val WIDTH = 2.0 * SCALE
		private const val HEIGHT = 2.0 * SCALE
		private const val ARROW_WIDTH = 1.0 * SCALE
		private const val ARROW_HEIGHT = 1.25 * SCALE

		fun drawBodyAt(x: Double, y: Double, context: DrawContext, arrowHeadStroke: Stroke) {
			context.g.drawLine(x, y, x - WIDTH,0.0)

			val minX = x - WIDTH
			context.g.stroke = arrowHeadStroke
			context.g.drawLine(minX, y, minX + ARROW_WIDTH, -ARROW_HEIGHT / 2)
			context.g.drawLine(minX, y, minX + ARROW_WIDTH, +ARROW_HEIGHT / 2)
		}
	}

	init {
		modelExchanged(null)
		setBounds(
			-DigitalPortView.LENGTH - WIDTH, -HEIGHT / 2,
			WIDTH, HEIGHT
		)
		orientation = Direction.SOUTH
	}

	override fun modelExchanged(oldModel: Power?) {
		super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getOutput(),
			direction = Direction.EAST)
		portView.setLocation(-DigitalPortView.LENGTH, 0)
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
		drawBodyAt(-DigitalPortView.LENGTH.toDouble(), 0.0, context, stroke)
	}
}