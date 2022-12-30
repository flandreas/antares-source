package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewDestinationConnector
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewOriginConnector
import ch.scorpion.jabbah.graph.view.connect.EdgeToPortConnector

/**
 * Standard implementation of the [EdgeViewFactory] interface.
 */
open class EdgeViewFactoryImpl(
	private val styleProvider: StyleProvider,
	private val edgeToPortConnectorSupplier: () -> EdgeToPortConnector,
	private val originEndpointConnectorSupplier: () -> DragEdgeViewOriginConnector,
	private val destinationEndpointConnectorSupplier: () -> DragEdgeViewDestinationConnector
) : EdgeViewFactory {

	override fun <T: Any> createEdgeView(graphView: GraphView): EdgeView<T> =
		EdgeViewImpl(styleProvider, edgeToPortConnectorSupplier, originEndpointConnectorSupplier,
			destinationEndpointConnectorSupplier)

	override fun <T: Any> createEdgeView(graphView: GraphView, netView: NetView<T>): EdgeView<T> =
		EdgeViewImpl(styleProvider, edgeToPortConnectorSupplier, originEndpointConnectorSupplier,
			destinationEndpointConnectorSupplier, netView.net)
}