package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogNet
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImpl

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

	/**
	 * The current (A) flowing through this [AnalogEdgeView] during simulation.
	 * Positive values indicate current flowing from [origin] to [destination].
	 * Can't be modelled on model [Net] because [Net] doesn't contain nodes, which
	 * is required for modelling Kirchhoff's "Current Law".
	 */
	var current: Double = 0.0

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
	}

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		current = 0.0
	}
}