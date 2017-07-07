package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.graph.model.Net

/**
 * A factory for creating [NodeView] instances
 * @param T the type of signal
 */
interface NodeViewFactory<T: Any> {

    fun create(): NodeView<T>

    fun create(net: Net<T>): NodeView<T>
}