package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.PullDirection
import ch.scorpion.antares.model.net.PullDirection.HIGH
import ch.scorpion.antares.model.net.PullDirection.LOW
import ch.scorpion.antares.model.net.PullResistor
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.GraphApplicationContext
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
		private const val PULL_DIRECTION_WIDTH = 2 * SCALE
	}

	init {
		modelExchanged(null)
		setBounds(
			-DigitalPortView.LENGTH.toDouble() - SymbolStyle.RESISTOR_WIDTH - PULL_DIRECTION_WIDTH, -SymbolStyle.RESISTER_HEIGHT_HALF,
			SymbolStyle.RESISTOR_WIDTH + PULL_DIRECTION_WIDTH, 2 * SymbolStyle.RESISTER_HEIGHT_HALF
		)
		orientation = when (pullDirection) {
			LOW -> Direction.NORTH
			HIGH -> Direction.SOUTH
		}
	}

	override fun modelExchanged(oldModel: PullResistor?) {
		super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getPort(),
			direction = Direction.EAST
		)
		portView.setLocation(-DigitalPortView.LENGTH, 0)
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

		if (context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
			context.g.color = pullDirectionExecutionColor.foregroundColor
		}
		drawPullDirection(context)

		AntaresViewModule.currentSymbolStyle.symbolStyle.drawResistor(
			this,
			context,
			getColorGradient(context) ?: context.g.color,
			getApplicableBackgroudColor(context),
			STROKE)
	}

	private fun getColorGradient(context: DrawContext): LinearColorGradient? {
		if (context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
			return LinearColorGradient(
				bounds.centerLeft,
				pullDirectionExecutionColor.foregroundColor,
				bounds.centerRight,
				netExecutionColor.foregroundColor)
		}
		return null
	}

	private val pullDirectionExecutionColor: CompositeColor get() =
		when (pullDirection) {
			LOW -> Bit.False.color
			HIGH -> Bit.True.color
		}

	private val netExecutionColor: CompositeColor get() =
		model.getOutputPort().net?.signal?.getColor() ?: Bit.Undefined.color

	private fun drawPullDirection(context: DrawContext) {
		when(pullDirection) {
			LOW -> drawLowPullDirection(context)
			HIGH -> drawHighPullDirection(context)
		}
	}

	private fun drawLowPullDirection(context: DrawContext) {
		GroundView.drawBodyAt(-DigitalPortView.LENGTH - SymbolStyle.RESISTOR_WIDTH, 0.0, context)
	}

	private fun drawHighPullDirection(context: DrawContext) {
		PowerView.drawBodyAt(-DigitalPortView.LENGTH - SymbolStyle.RESISTOR_WIDTH, 0.0, context, stroke)
	}
}