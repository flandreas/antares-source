package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.analog.AnalogNet
import io.antarescircuit.antares.model.net.DigitalNet
import io.antarescircuit.antares.view.DigitalGraphView
import io.antarescircuit.antares.view.analog.AnalogGraphView
import io.antarescircuit.antares.view.analog.AnalogNodeView
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.NetView
import io.antarescircuit.jabbah.graph.view.graph.GraphViewImpl
import io.antarescircuit.jabbah.graph.view.net.node.NodeView
import io.antarescircuit.jabbah.graph.view.net.node.NodeViewFactory
import io.antarescircuit.jabbah.graph.view.net.node.NodeViewImpl

class AntaresNodeViewFactory(
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : NodeViewFactory {

	override fun <T : Any> create(netView: NetView<T>, graphView: GraphView): NodeView<T> {
		return when (graphView) {
			is DigitalGraphView -> DigitalNodeView(styleProvider, netView.net as DigitalNet, netView.style) as NodeView<T>
			is AnalogGraphView -> AnalogNodeView(styleProvider, netView.net as AnalogNet) as NodeView<T>
			is GraphViewImpl -> NodeViewImpl(styleProvider, netView.net, netView.style)
			else -> throw IllegalArgumentException("Unsupported GraphView type when creating NodeView")
		}
	}
}