package io.antarescircuit.jabbah.graph.view.net.node

import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.NetView

/**
 * Standard implementation of the [NodeViewFactory] interface.
 */
class NodeViewFactoryImpl(
    private val styleProvider: StyleProvider
) : NodeViewFactory {

    override fun <T: Any> create(netView: NetView<T>, graphView: GraphView): NodeView<T> =
    	NodeViewImpl(styleProvider, netView.net, netView.style)
}