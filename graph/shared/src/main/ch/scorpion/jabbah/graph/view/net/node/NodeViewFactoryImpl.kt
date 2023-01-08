package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.NetView

/**
 * Standard implementation of the [NodeViewFactory] interface.
 */
class NodeViewFactoryImpl(
    private val styleProvider: StyleProvider
) : NodeViewFactory {

    override fun <T: Any> create(netView: NetView<T>, graphView: GraphView): NodeView<T> =
    	NodeViewImpl(styleProvider, netView.net, netView.style)
}