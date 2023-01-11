package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogNet
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImpl
import kotlin.math.abs

class AnalogEdgeView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	net: AnalogNet = AnalogNet()
) : EdgeViewImpl<AnalogSignal>(
	styleProvider,
	DEF_EDGE_TO_PORT_CONNECTOR_SUPPLIER,
	DEF_ORIG_ENDPOINT_CONNECTOR_SUPPLIER,
	DEF_DEST_ENDPOINT_CONNECTOR_SUPPLIER,
	net
) {

	companion object {

		private val LOG by logger(AnalogEdgeView::class)

		/** Determines the speed of the current flow animation.*/
		private const val FACTOR = 30.0
	}

	/**
	 * The electrical current (in A) flowing through this [AnalogEdgeView] during simulation.
	 * Positive values indicate current flowing from [origin] to [destination].
	 * Can't be modelled on model [Net] because [Net] doesn't contain nodes, which
	 * is required for modelling Kirchhoff's "Current Law".
	 */
	var current: Double = 0.0
		set(value) {
			LOG.debug("Set current=$value on EdgeView $id")
			field = value
			(origin?.portView as AnalogPortView?)?.current = value
			(destination?.portView as AnalogPortView?)?.current = value
		}

	/**
	 * Periodically updated by the current flow animation task of [AnalogGraphView].
	 * Represents the "timing tick" that drives the animation. Repeats within the range
	 * from 0 to [CurrentFlowVisualization.DISTANCE].
	 * All [AnalogEdgeView]s in a [AnalogGraphView] experience the same [currentFlowAnimationTick] tick.
	 */
	var animationOffset: Double = 0.0
		private set

	/** Repeatedly called by [AnalogGraphView] to drive the current flow animation. */
	fun currentFlowAnimationTick() {
		val delta = abs(current * FACTOR)
		val newOffset = animationOffset + delta
		animationOffset = if (newOffset >= CurrentFlowVisualization.DISTANCE) {
			0.0
		} else {
			animationOffset + delta
		}
	}

	override fun draw(context: DrawContext) {
		val graphAppContext = context.castedAppContext<GraphApplicationContext>()!!

		context.color =
			if (graphAppContext.showNetState) {
				if (model.isError) {
					Themes.get<AntaresTheme>().error
				} else {
					model.signal?.color ?: color
				}
			} else {
				context.choose(color)
			}

		super.draw(context)

		if (graphAppContext.showNetState) {
			CurrentFlowVisualization.draw(this, context)
		}
	}

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		current = 0.0
		animationOffset = 0.0
	}

	override fun getExecutionTooltipContent(): String =
		Translations.getString("antares.analogEdgeView.simTooltipContent", model.signal!!.voltage, abs(current))
}