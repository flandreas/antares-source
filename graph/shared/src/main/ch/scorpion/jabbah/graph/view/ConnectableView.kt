package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.net.node.NodeView

/**
 *  Represents an object to which [EdgeView]s can be connected.
 */
interface ConnectableView : Drawable, Storable {

    val id: Int

    /**
     * Returns the absolute location of the connection point of the specified [Port].
     * @param port the [Port] whose connection point is requested.
     */
    fun getPortConnectionPoint(port: Port<*>?): Point2D

    /**
     * Returns the [Direction]s in which [EdgeView]s could leave the specified [Port].
     * @param edgeView the [EdgeView] being layouted
     * @param port the [Port].
     * @param refPoint the [Point] that should be targeted by [EdgeView]s leaving the specified [Port].
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
    fun <G: Any> handleConnect(edgeView: EdgeView<G>, port: Port<G>?)

    /**
     * Notifies this [ConnectableView] that an [EdgeView] has disconnected from a [PortView] of a
     * particular [Port].  As a reaction, this [ConnectableView] could initiate the update the geometry
     * of the [PortView], if necessary.

     * @param edgeView the [EdgeView] that has unconnected
     * @param port the [Port] from which the [Net] of the [EdgeView] has been disconnected
     */
    fun <G: Any> handleUnconnect(edgeView: EdgeView<G>, port: Port<G>?)

    /**
     * Returns the [PortView] of the specified [Port].
     * @param port the [Port] whose [PortView] is requested.
     * @return the [PortView] of `isPort`.
     */
    fun <G: Any> getPortView(port: Port<G>):PortView<G>?
}