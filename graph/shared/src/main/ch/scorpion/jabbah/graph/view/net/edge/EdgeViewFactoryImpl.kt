package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
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
        private val destinationEndpointConnectorSupplier: () -> DragEdgeViewDestinationConnector,
        private val currentSystemSpeedCategory: CurrentSystemSpeedCategory
) :EdgeViewFactory<T> {

	companion object {
		private val LOG by logger(EdgeViewFactoryImpl::class)
	}

    override fun createEdgeView(): EdgeView<T> {
	    LOG.debug("create EdgeView")
        return EdgeViewImpl(styleProvider, edgeToPortConnectorSupplier, originEndpointConnectorSupplier,
                destinationEndpointConnectorSupplier, currentSystemSpeedCategory)
    }

    override fun createEdgeView(net: Net<T>): EdgeView<T> {
	    LOG.debug("create EdgeView")
        return EdgeViewImpl(styleProvider, edgeToPortConnectorSupplier, originEndpointConnectorSupplier,
                destinationEndpointConnectorSupplier, currentSystemSpeedCategory, net)
    }
}