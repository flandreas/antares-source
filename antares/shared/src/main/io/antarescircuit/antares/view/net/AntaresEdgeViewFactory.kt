package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.analog.AnalogNet
import io.antarescircuit.antares.model.net.DigitalNet
import io.antarescircuit.antares.view.DigitalGraphView
import io.antarescircuit.antares.view.analog.AnalogEdgeView
import io.antarescircuit.antares.view.analog.AnalogGraphView
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.NetView
import io.antarescircuit.jabbah.graph.view.graph.GraphViewImpl
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewFactory
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewImpl

@Suppress("UNCHECKED_CAST")
class AntaresEdgeViewFactory(
	private val styleProvider: StyleProvider
) : EdgeViewFactory {

	override fun <T: Any> createEdgeView(graphView: GraphView): EdgeView<T> =
		when (graphView) {
			is DigitalGraphView -> DigitalEdgeView(styleProvider) as EdgeView<T>
			is AnalogGraphView -> AnalogEdgeView(styleProvider) as EdgeView<T>
			is GraphViewImpl -> EdgeViewImpl(styleProvider)
			else -> throw IllegalArgumentException("Unsupported GraphView type when creating EdgeView")
		}

	override fun <T: Any> createEdgeView(graphView: GraphView, netView: NetView<T>): EdgeView<T> =
		when (graphView) {
			is DigitalGraphView -> DigitalEdgeView(styleProvider, netView.net as DigitalNet) as EdgeView<T>
			is AnalogGraphView -> AnalogEdgeView(styleProvider, netView.net as AnalogNet) as EdgeView<T>
			is GraphViewImpl -> EdgeViewImpl(styleProvider)
			else -> throw IllegalArgumentException("Unsupported GraphView type when creating EdgeView")
		}
}