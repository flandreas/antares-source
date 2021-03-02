package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.PullDirection
import ch.scorpion.antares.model.net.PullResistor
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.graphics.LineCap
import ch.scorpion.jabbah.draw.graphics.LineJoin
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.view.style.GraphTheme

class PullResistorView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: PullResistor = PullResistor()
) : DigitalComponentView<PullResistor>(styleProvider, model) {

	companion object {
		private val STROKE = Stroke(
			Themes.get<GraphTheme>().figure.stroke.width,
			cap = LineCap.BUTT,
			join = LineJoin.MITER
		)
		private const val WIDTH_HALF = SCALE.toDouble()
		private const val WIDTH = 2 * SCALE
		private const val BOX_HEIGHT = 6 * SCALE
		private const val PULL_DIRECTION_HEIGHT = 2 * SCALE

		private val PATH = System.createPath()
			.moveTo(0, DigitalPortView.LENGTH)
			.lineTo(WIDTH_HALF, DigitalPortView.LENGTH + 0.5 * SCALE)
			.lineTo(-WIDTH_HALF, DigitalPortView.LENGTH + 1.5 * SCALE)
			.lineTo(WIDTH_HALF, DigitalPortView.LENGTH + 2.5 * SCALE)
			.lineTo(-WIDTH_HALF, DigitalPortView.LENGTH + 3.5 * SCALE)
			.lineTo(WIDTH_HALF, DigitalPortView.LENGTH + 4.5 * SCALE)
			.lineTo(-WIDTH_HALF, DigitalPortView.LENGTH + 5.5 * SCALE)
			.lineTo(0, DigitalPortView.LENGTH + 6 * SCALE)
	}

	init {
		modelExchanged(null)
		setBounds(-WIDTH_HALF.toInt(), getInput().unconnectedLength, WIDTH, BOX_HEIGHT + PULL_DIRECTION_HEIGHT)
	}

	override fun modelExchanged(oldModel: PullResistor?) {
		super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getPort(),
			direction = Direction.NORTH
		)
		portView.setLocation(0, DigitalPortView.LENGTH)
		addPortView(portView)
	}

	/** ---- UI properties */

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			invalidate()
			model.bitWidth = value
			invalidate()
		}

	var pullDirection: PullDirection
		get() = model.pullDirection
		set(value) { model.pullDirection = value }

	/** ---- [AbstractDrawable] */

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		context.g.stroke = STROKE
		context.g.color = context.choose(color).foregroundColor
		context.g.draw(PATH)
		drawPullDirection(context)
	}

	private fun drawPullDirection(context: DrawContext) {
		when(pullDirection) {
			PullDirection.LOW -> drawLowPullDirection(context)
			PullDirection.HIGH -> drawHighPullDirection(context)
		}
	}

	private fun drawLowPullDirection(context: DrawContext) {
		val yBegin = DigitalPortView.LENGTH + 6 * SCALE
		val yEnd = yBegin + PULL_DIRECTION_HEIGHT
		context.g.stroke = Themes.get<GraphTheme>().edge.stroke
		context.g.drawLine(0, yBegin, 0, yEnd)
		context.g.stroke = stroke
		context.g.drawLine(-0.75 * SCALE, yEnd.toDouble(), 0.75 * SCALE, yEnd.toDouble())
	}

	private fun drawHighPullDirection(context: DrawContext) {
		val yBegin = DigitalPortView.LENGTH + 6 * SCALE
		val yEnd = yBegin + PULL_DIRECTION_HEIGHT
		context.g.stroke = Themes.get<GraphTheme>().edge.stroke
		context.g.drawLine(0, yBegin, 0, yEnd)
		context.g.stroke = stroke
		context.g.drawLine(0.0, yEnd.toDouble(), -0.75 * SCALE, yEnd - 0.75 * SCALE)
		context.g.drawLine(0.0, yEnd.toDouble(), 0.75 * SCALE, yEnd - 0.75 * SCALE)
	}
}