package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.model.Net

/**
 * Standard implementation of the [NodeViewFactory] interface.
 */
class NodeViewFactoryImpl<T: Any>(
    private val styleProvider: StyleProvider,
    private val currentSystemSpeedCategory: CurrentSystemSpeedCategory
) : NodeViewFactory<T> {

    override fun create(): NodeView<T> {
        return NodeViewImpl(styleProvider, currentSystemSpeedCategory)
    }

    override fun create(net: Net<T>): NodeView<T> {
        return NodeViewImpl(styleProvider, currentSystemSpeedCategory, net)
    }
}