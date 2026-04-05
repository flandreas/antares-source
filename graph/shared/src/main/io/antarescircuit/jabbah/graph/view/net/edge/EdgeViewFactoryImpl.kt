package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.NetView

/**
 * Standard implementation of the [EdgeViewFactory] interface.
 */
open class EdgeViewFactoryImpl(
	private val styleProvider: StyleProvider
) : EdgeViewFactory {

	override fun <T: Any> createEdgeView(graphView: GraphView): EdgeView<T> =
		EdgeViewImpl(styleProvider)

	override fun <T: Any> createEdgeView(graphView: GraphView, netView: NetView<T>): EdgeView<T> =
		EdgeViewImpl(styleProvider, netView.net)
}