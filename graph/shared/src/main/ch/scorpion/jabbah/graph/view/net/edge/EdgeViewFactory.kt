package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * A factory for creating [EdgeView] instances.
 * @param T the type of signals that are forwared over the created [EdgeView]s
 */
interface EdgeViewFactory<T: Any> {

    fun createEdgeView(): EdgeView<T>

    fun createEdgeView(net: Net<T>): EdgeView<T>
}