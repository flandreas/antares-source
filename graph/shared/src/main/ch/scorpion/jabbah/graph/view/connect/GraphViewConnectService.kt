package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * Provides service methods for connecting [GraphElementView]s of a [GraphView].
 */
interface GraphViewConnectService {

	fun <T : Any> connect(edgeView: EdgeView<T>, endpointType: EdgeViewEndpointType, connection: Connection<T>) {
		when (endpointType) {
			EdgeViewEndpointType.ORIGIN -> connectToOrigin(edgeView, connection)
			EdgeViewEndpointType.DESTINATION -> connectToDestination(edgeView, connection)
		}
	}

	/**
	 * Connects the origin of an existing [EdgeView] with a particular [Port] of a [ConnectableView]
	 * both on the view and the model layer.
	 * @param direction the [Direction] in which the [EdgeView]'s first segment should be laid out
	 */
	fun <T : Any> connectToOrigin(edgeView: EdgeView<T>, connection: Connection<T>, direction: Direction? = null, doLayout: Boolean = true)

	/**
	 * Unconnects an [EdgeView] from its origin [ConnectableView].
	 */
	fun <T : Any> unconnectFromOrigin(edgeView: EdgeView<T>)

	/**
	 * Connects the destination of an existing [EdgeView] with a particular [Port] of a
	 * [ConnectableView] both on the view and the model layer.
	 * @param direction the [Direction] in which the [EdgeView]'s last segment should be laid out
	 */
	fun <T : Any> connectToDestination(edgeView: EdgeView<T>, connection: Connection<T>, direction: Direction? = null, doLayout: Boolean = true)

	/**
	 * Unconnects an [EdgeView] from its destination [ConnectableView].
	 */
	fun <T : Any> unconnectFromDestination(edgeView: EdgeView<T>)

	/**
	 * Connects the specified [EdgeView] with the first [OutputPort] of the origin [VerticeView] and
	 * the first [InputPort] of the destination [VerticeView], and layouts the [EdgeView].
	 */
	fun <T : Any> connect(edgeView: EdgeView<T>, orig: VerticeView<*>, dest: VerticeView<*>)

	/**
	 * Connects the specified [EdgeView] with the [PortView] of an origin [VerticeView] and the input
	 * [PortView] of a destination [VerticeView], and layouts the [EdgeView].
	 */
	fun <T : Any> connect(edgeView: EdgeView<T>, origOutput: PortView<T>?, destInput: PortView<T>?)

	/**
	 * Unconnects an [EdgeView] from the origin [Port] to which it is currently connected,
	 * and from the [ConnectableView] that contains a [PortView] for that [Port].
	 * Does nothing if not connected.
	 */
	fun unconnectEdgeViewOrigin(edgeView: EdgeView<*>)

	/**
	 * Unconnects an [EdgeView] from the destination [Port] to which it is currently connected,
	 * and from the [ConnectableView] that contains a [PortView] for that [Port].
	 * Does nothing if not connected.
	 */
	fun unconnectEdgeViewDestination(edgeView: EdgeView<*>)

	fun unconnectEdgeView(edgeView: EdgeView<*>, endpointType: EdgeViewEndpointType) {
		when (endpointType) {
			EdgeViewEndpointType.ORIGIN -> unconnectEdgeViewOrigin(edgeView)
			EdgeViewEndpointType.DESTINATION -> unconnectEdgeViewDestination(edgeView)
		}
	}

	/**
	 * Unconnects an [EdgeView] from both [ConnectableView]s to which it might be connected.
	 *
	 * If the [EdgeView] is connected to [NodeView]s that have only two [EdgeView]s remaining after
	 * unconnecting, those [NodeView]s are removed as well, and the remaining [EdgeView]s get joined.
	 * @param edgeView the [EdgeView] to unconnect
	 */
	fun <T : Any> unconnect(edgeView: EdgeView<T>)

	/**
	 * Unconnects all [PortView]s of a [VerticeView] from the [EdgeView]s to which it is
	 * connected, including unconnecting on the model layer.
	 * @param verticeView the [VerticeView] to be unconnected
	 * @param graphView the [GraphView] that contains the [VerticeView] as well as the connected [EdgeView]s
	 */
	fun unconnect(graphView: GraphView, verticeView: VerticeView<*>)

	/**
	 * Creates a new [EdgeView] in the specified [GraphView] and connects it with the first
	 * [OutputPort] of the origin [VerticeView] and the first [InputPort] of the destination
	 * [VerticeView], and layouts the [EdgeView].
	 */
	fun <T : Any> addConnection(
		graphView: GraphView,
		orig: VerticeView<*>,
		dest: VerticeView<*>
	): EdgeView<T>

	/**
	 * Creates a new [EdgeView] in the specified [GraphView] and connects it with the output
	 * [PortView] of an origin [VerticeView] and the input [PortView] of a destination
	 * [VerticeView], adds it to the [GraphView], and layouts the [EdgeView].
	 */
	fun <T : Any> addConnection(
		graphView: GraphView,
		origOutput: PortView<T>,
		destInput: PortView<T>
	): EdgeView<T>

	/**
	 * Splits an existing [EdgeView], inserts a [NodeView] at the start location of [newEdgeView],
	 * and connects the [NodeView] with a destination [Port], if available.
	 * Adds the [newEdgeView] to the [graphView] if not already contained.
	 *
	 * @param newEdgeViewEndpointType the [EdgeViewEndpointType] to connect [newEdgeView] with the created [NodeView]
	 * @param otherNewEdgeViewPortView the [PortView] to connect the end of [newEdgeView] that is not connected to the [NodeView]
	 */
	fun <T : Any> split(
		graphView: GraphView,
		splitEdgeView: EdgeView<T>,
		splitSegmentIndex: Int,
		splitLocation: Point2D,
		newEdgeView: EdgeView<T>,
		newEdgeViewEndpointType: EdgeViewEndpointType = EdgeViewEndpointType.ORIGIN,
		otherNewEdgeViewPortView: PortView<T>?,
		tailEdgeView: EdgeView<T>? = null
	): SplitEdgeViewResult<T>

	/**
	 * Removes a [NodeView] from a [GraphView] and joins the outgoing [EdgeView]s.
	 * @throws IllegalStateException if `nodeView` has more than two outgoing [EdgeView].
	 */
	fun <T : Any> removeNodeView(graphView: GraphView, nodeView: NodeView<T>)

	/**
	 * Joins [edgeView1] with the [Point2D]s of [edgeView2] and removes [edgeView2] from [graphView],
	 * including all the necessary reconnecting. The point of join is defined my moving
	 * [endpointType1] of [edgeView1] to [endpointType2] of [edgeView2].
	 */
	fun <T : Any> join(
		graphView: GraphView,
		edgeView1: EdgeView<T>,
		endpointType1: EdgeViewEndpointType,
		edgeView2: EdgeView<T>,
		endpointType2: EdgeViewEndpointType
	)
}

/** Represents the result of splitting an [EdgeView].*/
data class SplitEdgeViewResult<T : Any>(
	val newEdgeView: EdgeView<T>,
	val tailEdgeView: EdgeView<T>,
	val nodeView: NodeView<T>)
