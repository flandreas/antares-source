package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * Provides service methods for connecting [GraphElementView]s of a [GraphView].
 */
interface GraphViewConnectService {

    /**
     * Connects the origin of an existing [EdgeView] with a particular [Port] of a [ConnectableView]
     * both on the view and the model layer.
     */
    fun <T: Any> connectToOrigin(edgeView: EdgeView<T>, orig: ConnectableView, port: Port<T>?)

    /**
     * Unconnects an [EdgeView] from its origin [ConnectableView].
     * @return the joined [EdgeView] that might result when unconnecting an [EdgeView] from a [NodeView]
     */
    fun <T: Any> unconnectFromOrigin(edgeView: EdgeView<T>): EdgeView<T>?

    /**
     * Connects the destination of an existing [EdgeView] with a particular [Port] of a
     * [ConnectableView] both on the view and the model layer.
     */
    fun <T: Any> connectToDestination(edgeView: EdgeView<T>, dest: ConnectableView, port: Port<T>?)

    /**
     * Unconnects an [EdgeView] from its destination [ConnectableView].
     * @return the joined [EdgeView] that might result when unconnecting an [EdgeView] from a [NodeView]
     */
    fun <T: Any> unconnectFromDestination(edgeView: EdgeView<T>): EdgeView<T>?

    /**
     * Connects the specified [EdgeView] with the first [OutputPort] of the origin [VerticeView] and
     * the first [InputPort] of the destination [VerticeView], and layouts the [EdgeView].
     */
    fun <T: Any> connect(edgeView: EdgeView<T>, orig: VerticeView<*>, dest: VerticeView<*>)

    /**
     * Connects the specified [EdgeView] with the [PortView] of an origin [VerticeView] and the input
     * [PortView] of a destination [VerticeView], and layouts the [EdgeView].
     */
    fun <T: Any> connect(edgeView: EdgeView<T>, origOutput: PortView<T>, destInput: PortView<T>)

    /**
     * Unconnects an [EdgeView] from the origin [Port] to which it it currently connected,
     * and from the [ConnectableView] that contains a [PortView] for that [Port].
     * Does nothing if not connected.
     */
    fun unconnectEdgeViewOrigin(edgeView: EdgeView<Any>)

    /**
     * Unconnects an [EdgeView] from the destination [Port] to which it is currently connected,
     * and from the [ConnectableView] that contains a [PortView] for that [Port].
     * Does nothing if not connected.
     */
    fun unconnectEdgeViewDestination(edgeView: EdgeView<Any>)

     /**
     * Unconnects an [EdgeView] from both [ConnectableView]s to which it might be connected.
     *
     * If the [EdgeView] is connected to [NodeView]s that have only two [EdgeView]s remaining after
     * unconnecting, those [NodeView]s are removed as well, and the remaining [EdgeView]s get joined.
     * @param edgeView the [EdgeView] to unconnect
     * @return the joined [EdgeView] that might result when unconnecting an [EdgeView] from a [NodeView]
     */
    fun <T: Any> unconnect(edgeView: EdgeView<T>): EdgeView<T>?

    /**
     * Unconnects all [PortView]s of a [VerticeView] from the [EdgeView]s to which it is
     * connected, including unconnecting on the model layer.
     * @param verticeView the [VerticeView] to be unconnected
     * @param graphView the [GraphView] that contains the [VerticeView] as well as the connected [EdgeView]s
     */
    fun unconnect(graphView: GraphView<out GraphElementView<*>>, verticeView: VerticeView<*>)

    /**
     * Creates a new [EdgeView] in the specified [GraphView] and connects it with the first
     * [OutputPort] of the origin [VerticeView] and the first [InputPort] of the destination
     * [VerticeView], and layouts the [EdgeView].
     */
    fun <T: Any> addConnection(graphView: GraphView<GraphElementView<*>>, orig: VerticeView<*>, dest: VerticeView<*>): EdgeView<T>

    /**
     * Creates a new [EdgeView] in the specified [GraphView] and connects it with the output
     * [PortView] of an origin [VerticeView] and the input [PortView] of a destination
     * [VerticeView], and layouts the [EdgeView].
     */
    fun <T: Any> addConnection(graphView: GraphView<GraphElementView<*>>, origOutput: PortView<T>, destInput: PortView<T>): EdgeView<T>

    /**
     * Splits an existings [EdgeView], inserts a [NodeView] at the split location, and connects the
     * [NodeView] with a destination [InputPort], if available.
     *
     * The created [NodeView] is placed at the first segment point of the [newEdgeView].
     */
    fun <T: Any> split(graphView: GraphView<GraphElementView<*>>, splittedEdgeView: EdgeView<T>, splitSegmentIndex: Int,
              newEdgeView: EdgeView<T>, destInput: PortView<T>?): SplitEdgeViewResult<T>

    /**
     * Removes a [NodeView] from a [GraphView] and joins the outgoing [EdgeView]s.
     * @return the remaining [EdgeView]
     * @throws IllegalStateException if `nodeView` doesn't have exactly two outgoing [EdgeView].
     */
    fun <T: Any> removeNodeView(graphView: GraphView<GraphElementView<*>>, nodeView: NodeView<T>): EdgeView<T>
}

/** Represents the result of splitting an [EdgeView].*/
data class SplitEdgeViewResult<T: Any>(val newEdgeView: EdgeView<T>, val tailEdgeView: EdgeView<T>, val nodeView: NodeView<T>)