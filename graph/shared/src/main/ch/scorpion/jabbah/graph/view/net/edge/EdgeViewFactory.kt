package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle

/**
 * A factory for creating [EdgeView] instances.
 * @param T the type of signals that are forwarded over the created [EdgeView]s
 */
interface EdgeViewFactory {

	/**
	 * Creates a new [EdgeView] to be added to [graphView]. Different [GraphView] types
	 * might require different [EdgeView] types.
	 */
    fun <T: Any> createEdgeView(graphView: GraphView): EdgeView<T>

	/**
	 * Creates a new [EdgeView] to be added to the specified [NetView], meaning that the newly
	 * created [EdgeView] should have the same [NetViewStyle] like [netView].
	 */
	fun <T: Any> createEdgeView(graphView: GraphView, netView: NetView<T>): EdgeView<T>
}