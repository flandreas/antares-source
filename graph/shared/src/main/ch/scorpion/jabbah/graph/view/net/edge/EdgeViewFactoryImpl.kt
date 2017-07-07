package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewDestinationConnector
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewOriginConnector
import ch.scorpion.jabbah.graph.view.connect.EdgeToPortConnector

/**
 * Standard implementation of the [EdgeViewFactory] interface.
 */
open class EdgeViewFactoryImpl<T: Any>(
    private val styleProvider: StyleProvider,
    private val edgeToPortConnectorSupplier: () -> EdgeToPortConnector,
    private val originEndpointConnectorSupplier: () -> DragEdgeViewOriginConnector,
    private val destinationEndpointConnectorSupplier: () -> DragEdgeViewDestinationConnector
) :EdgeViewFactory<T> {

    override fun createEdgeView(): EdgeView<T> {
        return EdgeViewImpl(styleProvider, edgeToPortConnectorSupplier, originEndpointConnectorSupplier, destinationEndpointConnectorSupplier)
    }

    override fun createEdgeView(net: Net<T>): EdgeView<T> {
        return EdgeViewImpl(styleProvider, edgeToPortConnectorSupplier, originEndpointConnectorSupplier, destinationEndpointConnectorSupplier, net)
    }
}