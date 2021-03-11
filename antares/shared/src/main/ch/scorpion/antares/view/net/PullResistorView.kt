package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.PullDirection
import ch.scorpion.antares.model.net.PullDirection.*
import ch.scorpion.antares.model.net.PullResistor
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.symbolstyle.SymbolStyle.Companion.RESISTER_HEIGHT
import ch.scorpion.antares.view.symbolstyle.SymbolStyle.Companion.RESISTOR_WIDTH_HALF
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
	pullDirection: PullDirection = LOW,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: PullResistor = PullResistor(pullDirection = pullDirection)
) : DigitalComponentView<PullResistor>(styleProvider, model) {

	companion object {
		private val STROKE = Stroke(
			Themes.get<GraphTheme>().figure.stroke.width,
			cap = LineCap.BUTT,
			join = LineJoin.MITER
		)
		private const val PULL_DIRECTION_HEIGHT = 2 * SCALE
	}

	init {
		modelExchanged(null)
		setBounds(
			-RESISTOR_WIDTH_HALF, getOutput().unconnectedLength.toDouble(),
			2 * RESISTOR_WIDTH_HALF, RESISTER_HEIGHT.toDouble() + PULL_DIRECTION_HEIGHT)
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
		getPortViews().first().prepareConnectionDrawContext(context)
		drawPullDirection(context)

		AntaresViewModule.currentSymbolStyle.symbolStyle.drawResistor(
			this,
			context,
			context.g.color,
			getApplicableBackgroudColor(context),
			STROKE)
	}

	private fun drawPullDirection(context: DrawContext) {
		when(pullDirection) {
			LOW -> drawLowPullDirection(context)
			HIGH -> drawHighPullDirection(context)
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