package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.view.NetView

/**
 * Standard implementation of the [NodeViewFactory] interface.
 */
class NodeViewFactoryImpl<T: Any>(
    private val styleProvider: StyleProvider,
    private val currentSystemSpeedCategory: CurrentSystemSpeedCategory
) : NodeViewFactory<T> {

    override fun create(netView: NetView<T>): NodeView<T> =
    	NodeViewImpl(styleProvider, currentSystemSpeedCategory, netView.net, netView.style)
}