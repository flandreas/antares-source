package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewConnectionGeometry
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.net.node.NodeView

/**
 *  Represents an object to which [EdgeView]s can be connected.
 */
interface ConnectableView : Drawable, Storable {

    val id: Int

	/**
	 * Determines whether this [ConnectableView] is currently connectable.
	 * Implementations will typically be connectable. If a [VerticeView]'s model contains a broken link,
	 * it is regarded as "not being connectable", and [EdgeView] should not connect to it when being loaded.
	 */
    val isConnectable: Boolean get() = true

    /**
     * Returns the absolute location of the connection point of the specified [Port].
     * @param port the [Port] whose connection point is requested.
     */
    fun getPortConnectionPoint(port: Port<*>?): Point2D

    fun getUnconnectedPortConnectionPoint(port: Port<*>): Point2D

    /**
     * Returns the [Direction]s in which [EdgeView]s could leave the specified [Port].
     * @param edgeView the [EdgeView] being laid out
     * @param port the [Port].
     * @param refPoint the [Point2D] that should be targeted by [EdgeView]s leaving the specified [Port].
     * Used by [VerticeView] that are indifferent about [Direction]s, such as [NodeView]s.
     */
    fun getPortConnectionLayoutDirections(edgeView: EdgeView<*>, port: Port<*>?, refPoint: Point2D?): Set<Direction>

    /**
     * Returns the [Port] with the given ID.
     * @param portId the ID of the requested [Port].
     * @return the [Port] with ID `portId`.
     */
    fun getPort(portId: Int): Port<*>?

    /**
     * Notifies this [ConnectableView] that an [EdgeView] has connected to a [PortView] of a
     * particular [Port]. As a reaction, this [ConnectableView] could initiate the update the geometry
     * of the [PortView], if necessary.
     * @param edgeView the [EdgeView] that has connected
     * @param port the [Port] to which the [Net] of the [EdgeView] has been connected
     */
    fun <G: Any> handleConnect(edgeView: EdgeView<G>, port: Port<G>?, geometry: EdgeViewConnectionGeometry)

    /**
     * Notifies this [ConnectableView] that an [EdgeView] has disconnected from a [PortView] of a
     * particular [Port].  As a reaction, this [ConnectableView] could initiate the update the geometry
     * of the [PortView], if necessary.

     * @param edgeView the [EdgeView] that has unconnected
     * @param port the [Port] from which the [Net] of the [EdgeView] has been disconnected
     */
    fun <G: Any> handleUnconnect(edgeView: EdgeView<G>, port: Port<G>?, lockEndpoint: Boolean = false)

    /**
     * Returns the [PortView] of the specified [Port].
     * @param port the [Port] whose [PortView] is requested.
     * @return the [PortView] of `isPort`.
     */
    fun <G: Any> getPortView(port: Port<G>): PortView<G>?
}