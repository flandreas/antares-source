package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle

/**
 * A factory for creating [EdgeView] instances.
 * @param T the type of signals that are forwarded over the created [EdgeView]s
 */
interface EdgeViewFactory<T: Any> {

    fun createEdgeView(): EdgeView<T>

	/**
	 * Creates a new [EdgeView] to be added to the specified [NetView], meaning that the newly
	 * created [EdgeView] should have the same [NetViewStyle] like [netView].
	 */
	fun createEdgeView(netView: NetView<T>): EdgeView<T>
}