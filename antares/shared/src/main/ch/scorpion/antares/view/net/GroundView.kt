package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Ground
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

class GroundView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Ground = Ground()
) : DigitalComponentView<Ground>(styleProvider, model) {

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

	init {
		modelExchanged(null)
		setBounds(
			-DigitalPortView.LENGTH - WIDTH, -HEIGHT / 2,
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
		drawBodyAt(-DigitalPortView.LENGTH.toDouble(), 0.0, context)
	}
}