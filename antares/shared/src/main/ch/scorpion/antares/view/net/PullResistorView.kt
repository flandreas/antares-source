package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.PullDirection
import ch.scorpion.antares.model.net.PullDirection.HIGH
import ch.scorpion.antares.model.net.PullDirection.LOW
import ch.scorpion.antares.model.net.PullResistor
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.edit.Look.SCALE
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.style.GraphTheme

class PullResistorView(
	pullDirection: PullDirection = LOW,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: PullResistor = PullResistor(pullDirection = pullDirection)
) : OrientableRectangularVerticeView<PullResistor>(styleProvider, model) {

	companion object {
		private const val PULL_DIRECTION_WIDTH = 2 * SCALE
	}

	init {
		modelExchanged(null)
		setBounds(
			-AbstractAntaresPortView.LENGTH.toDouble() - SymbolStyle.RESISTOR_WIDTH - PULL_DIRECTION_WIDTH, -SymbolStyle.RESISTER_HEIGHT_HALF,
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
		portView.setLocation(-AbstractAntaresPortView.LENGTH, 0)
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
		set(value) {
			if (value != model.pullDirection) {
				model.pullDirection = value
				tooltip.reset()
			}
		}

	/** ---- [AbstractDrawable] */

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		getPortViews().first().prepareConnectionDrawContext(context)

		val portStroke = context.g.stroke

		context.g.color = context.chooseForeground(
			if (context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
				transparent.applyTo(pullDirectionExecutionColor.foregroundColor)
			} else {
				styleProvider.getStyle(GraphStyleType.EDGE).color.foregroundColor
			}
		)

		drawPullDirection(context)

		val applicableForegroundColor = if (context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
			getColorGradient(context) ?: transparent.applyTo(pullDirectionExecutionColor.foregroundColor)
		} else {
			context.chooseForeground(when (AntaresViewModule.currentSymbolStyle.symbolStyle) {
				SymbolStyle.EUROPEAN,SymbolStyle.VERBOSE  -> foregroundColor
				SymbolStyle.AMERICAN -> styleProvider.getStyle(GraphStyleType.EDGE).color.foregroundColor
			})
		}

		AntaresViewModule.currentSymbolStyle.symbolStyle.drawResistor(
			this,
			isVariable = false,
			context,
			applicableForegroundColor,
			getApplicableBackgroundColor(context),
			portStroke)
	}

	private fun getColorGradient(context: DrawContext): LinearColorGradient? {
		if (context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
			return LinearColorGradient(
				bounds.centerLeft.addX(PULL_DIRECTION_WIDTH.toDouble()),
				transparent.applyTo(pullDirectionExecutionColor.foregroundColor),
				bounds.centerRight,
				transparent.applyTo(netExecutionColor.foregroundColor))
		}
		return null
	}

	private val pullDirectionExecutionColor: CompositeColor get() =
		when (pullDirection) {
			LOW -> Bit.False.color
			HIGH -> Bit.True.color
		}

	private val netExecutionColor: CompositeColor get() =
		model.getOutputPort().net?.signal?.color ?: Bit.Undefined.color

	private fun drawPullDirection(context: DrawContext) {
		when(pullDirection) {
			LOW -> drawLowPullDirection(context)
			HIGH -> drawHighPullDirection(context)
		}
	}

	private fun drawLowPullDirection(context: DrawContext) {
		GroundView.drawBodyAt(-AbstractAntaresPortView.LENGTH - SymbolStyle.RESISTOR_WIDTH, 0.0, context)
	}

	private fun drawHighPullDirection(context: DrawContext) {
		PowerViewShape.drawBodyAt(-AbstractAntaresPortView.LENGTH - SymbolStyle.RESISTOR_WIDTH, 0.0, context)
	}
}