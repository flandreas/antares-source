package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.port.PortView

/**
 * Represents the connection of an [EdgeView] with a [ConnectableView] and one of its [Port] (if any).
 */
data class Connection<T : Any>(
	val connectableView: ConnectableView,
	val port: Port<T>? = null
) {

	val portView: PortView<T>? get() = port?.let { connectableView.getPortView(it) }

	val portConnectionPoint : Point2D get() = connectableView.getPortConnectionPoint(port)

	val asReference: ConnectionReference get() = ConnectionReference(connectableView.id, port?.portId)

	fun getPortConnectionLayoutDirections(edgeView: EdgeView<*>, refPoint: Point2D): Set<Direction> =
		connectableView.getPortConnectionLayoutDirections(edgeView, port, refPoint)
}

/**
 * Contains the same information as [Connection], but in an instance-independent
 * way that can be used for [Command]s (which must work with snapshot-replaying).
 */
data class ConnectionReference(
	val connectableViewId: Int,
	val portId: Int?
) {

	fun <T: Any> getConnection(graphView: GraphView): Connection<T> {
		val connectableView = graphView.getWithId(connectableViewId) as ConnectableView
		val port = portId?.let { connectableView.getPort(it) }
		@Suppress("UNCHECKED_CAST")
		return Connection(connectableView, port) as Connection<T>
	}
}
