package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle

/**
 * A factory for creating [NodeView] instances
 * @param T the type of signal
 */
interface NodeViewFactory<T: Any> {

	/**
	 * Creates a new [NodeView] to be added to the specified [NetView], meaning that the newly
	 * created [NodeView] should have the same [NetViewStyle] like [netView].
	 */
    fun create(netView: NetView<T>): NodeView<T>
}