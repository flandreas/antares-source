package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.analog.AnalogNet
import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.antares.view.DigitalGraphView
import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImpl

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