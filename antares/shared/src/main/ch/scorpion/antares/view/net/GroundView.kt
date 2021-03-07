package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Ground
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.style.GraphTheme

class GroundView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Ground = Ground()
) : DigitalComponentView<Ground>(styleProvider, model) {

	companion object {
		private const val WIDTH = 2.0 * SCALE
		private const val HEIGHT = 2.0 * SCALE
		private const val BAR_WIDTH = 0.5 * SCALE
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

		// TEST BEGIN
		//context.g.draw(bounds)
		// TEST END

		//val barTopY = bounds.maxY - BAR_HEIGHT
		val barLeftX = bounds.minX + BAR_WIDTH

		// TODO Use same stroke as DigitalPortView (support Bus width)
		context.g.stroke = Themes.get<GraphTheme>().edge.stroke

		// TODO Don't use signal color if not required with current SystemSpeed
		context.g.color = if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			model.getOutput<DigitalSignal>().getOutgoingSignal()!!.getColor().foregroundColor
		} else {
			context.choose(color).foregroundColor
		}

		context.g.drawLine(
			-DigitalPortView.LENGTH.toDouble(), 0.0,
			barLeftX, 0.0
		)
		context.g.fillRect(
			bounds.x, bounds.y, BAR_WIDTH, HEIGHT
		)

		/*
		context.g.drawLine(
			0.0, DigitalPortView.LENGTH.toDouble(),
			0.0, barTopY
		)
		context.g.fillRect(
			bounds.x, barTopY,
			WIDTH, BAR_HEIGHT
		)
		 */
	}
}