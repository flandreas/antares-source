package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle

/**
 * A factory for creating [NodeView] instances
 */
interface NodeViewFactory {

	/**
	 * Creates a new [NodeView] to be added to the specified [NetView], meaning that the newly
	 * created [NodeView] should have the same [NetViewStyle] like [netView].
	 *
	 * @param T the type of signal
	 * @param graphView the [GraphView] to which the created [NodeView] is to be added.
	 * Might determine the type of the created [NodeView] depending on [GraphType].
	 */
    fun <T: Any> create(netView: NetView<T>, graphView: GraphView): NodeView<T>
}