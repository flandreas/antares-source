package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.Net

/**
 * Standard implementation of the [NodeViewFactory] interface.
 */
class NodeViewFactoryImpl<T: Any>(private val styleProvider: StyleProvider) : NodeViewFactory<T> {

    override fun create(): NodeView<T> {
        return NodeViewImpl(styleProvider)
    }

    override fun create(net: Net<T>): NodeView<T> {
        return NodeViewImpl(styleProvider, net)
    }
}