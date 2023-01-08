package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.analog.AnalogNet
import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.antares.view.DigitalGraphView
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.AnalogNodeView
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.net.node.NodeViewFactory
import ch.scorpion.jabbah.graph.view.net.node.NodeViewImpl

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