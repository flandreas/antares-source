package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.*
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewFactory
import io.antarescircuit.jabbah.graph.view.net.node.NodeView
import io.antarescircuit.jabbah.graph.view.net.node.NodeViewFactory
import io.antarescircuit.jabbah.graph.view.port.PortView

/**
 * Standard implementation of the [GraphViewConnectService] interface.
 */
class GraphViewConnectServiceImpl(
	private val edgeViewFactorySupplier: () -> EdgeViewFactory,
	private val nodeViewFactorySupplier: () -> NodeViewFactory
) : GraphViewConnectService {

	companion object {
		private val LOG by logger(GraphViewConnectServiceImpl::class)
	}

	/** ---- [GraphViewConnectService] interface */

	override fun <T : Any> connectToOrigin(edgeView: EdgeView<T>, connection: Connection<T>, direction: Direction?, doLayout: Boolean) {
		LOG.trace("connect EdgeView ${edgeView.id} to Port ${connection.port?.portId} of origin ConnectableView ${connection.connectableView.id}")
		connectPortToNet(connection.port, edgeView.model)
		edgeView.connectToOrigin(connection)
		if (doLayout) {
			edgeView.layout.layoutOrigin(direction?.let { setOf(it) })
		}
	}

	override fun <T : Any> unconnectFromOrigin(edgeView: EdgeView<T>) {
		LOG.trace("unconnectFromOrigin EdgeView: ${edgeView.id}")
		var origNodeView: NodeView<T>? = null
		if (edgeView.origin?.connectableView is NodeView<*>) {
			origNodeView = edgeView.origin?.connectableView as NodeView<T>
		}
		unconnectPortFromNet(edgeView.origin?.port, edgeView.model)
		edgeView.unconnectFromOrigin()

		if (origNodeView != null && origNodeView.getEdgeViews().size == 2) {
			removeNodeView((origNodeView.parent as GraphView), origNodeView)
		}
	}

	override fun <T : Any> connectToDestination(edgeView: EdgeView<T>, connection: Connection<T>, direction: Direction?, doLayout: Boolean) {
		LOG.trace("connect EdgeView ${edgeView.id} to Port ${connection.port?.portId} of destination ConnectableView ${connection.connectableView.id}")
		connectPortToNet(connection.port, edgeView.model)
		edgeView.connectToDestination(connection)
		if (doLayout) {
			edgeView.layout.layoutDestination(direction?.let { setOf(it) })
		}
	}

	override fun <T : Any> unconnectFromDestination(edgeView: EdgeView<T>) {
		LOG.trace("unconnectFromDestination EdgeView: ${edgeView.id}")
		var destNodeView: NodeView<T>? = null
		if (edgeView.destination?.connectableView is NodeView<*>) {
			destNodeView = edgeView.destination!!.connectableView as NodeView<T>
		}
		unconnectPortFromNet(edgeView.destination?.port, edgeView.model)
		edgeView.unconnectFromDestination()

		if (destNodeView != null && destNodeView.getEdgeViews().size == 2) {
			removeNodeView((destNodeView.parent as GraphView), destNodeView)
		}
	}

	override fun <T : Any> connect(edgeView: EdgeView<T>, orig: VerticeView<*>, dest: VerticeView<*>) {
		val outputPortView = orig.getPortView(orig.vertice.getOutput<T>())
		val inputPortView = dest.getPortView(dest.vertice.getInput<T>())
		connect(edgeView, outputPortView!!, inputPortView!!)
	}

	override fun <T : Any> connect(edgeView: EdgeView<T>, origOutput: PortView<T>?, destInput: PortView<T>?) {
		if (origOutput != null) {
			connectToOrigin(edgeView, Connection(origOutput.owner!!, origOutput.port))
		}
		if (destInput != null) {
			connectToDestination(edgeView, Connection(destInput.owner!!, destInput.port))
		}
	}

	override fun <T : Any> unconnect(edgeView: EdgeView<T>) {
		unconnectFromOrigin(edgeView)
		unconnectFromDestination(edgeView)
	}

	override fun unconnectEdgeViewOrigin(edgeView: EdgeView<*>) {
		if (edgeView.origin?.port != null) {
			edgeView.model.unconnect(edgeView.origin!!.port!!)
		}
		edgeView.unconnectFromOrigin()
	}

	override fun unconnectEdgeViewDestination(edgeView: EdgeView<*>) {
		if (edgeView.destination?.port != null) {
			edgeView.model.unconnect(edgeView.destination!!.port!!)
		}
		edgeView.unconnectFromDestination()
	}

	override fun unconnect(graphView: GraphView, verticeView: VerticeView<*>) {
		graphView.getEdgeViews()
			.filter { ev -> ev.origin?.connectableView === verticeView }
			.forEach { ev -> unconnectEdgeViewOrigin(ev) }
		graphView.getEdgeViews()
			.filter { ev -> ev.destination?.connectableView === verticeView }
			.forEach { ev -> unconnectEdgeViewDestination(ev) }
	}

	override fun <T : Any> addConnection(graphView: GraphView, orig: VerticeView<*>, dest: VerticeView<*>): EdgeView<T> {
		LOG.trace("addConnection from VerticeView ${orig.id} to VerticeView ${dest.id}")
		val edgeView = edgeViewFactorySupplier.invoke().createEdgeView<T>(graphView)
		graphView.add(edgeView)
		connect(edgeView, orig, dest)
		return edgeView
	}

	override fun <T : Any> addConnection(
		graphView: GraphView,
		origOutput: PortView<T>,
		destInput: PortView<T>
	): EdgeView<T> {
		val edgeView = edgeViewFactorySupplier.invoke().createEdgeView<T>(graphView)
		LOG.trace("add EdgeView ${edgeView.id} from Port ${origOutput.port.portId} of origin ${origOutput.owner?.id} to Port ${destInput.port.portId} of destination ${destInput.owner?.id}")
		graphView.add(edgeView)
		connect(edgeView, origOutput, destInput)
		return edgeView
	}

	override fun <T : Any> split(
		graphView: GraphView,
		splitEdgeView: EdgeView<T>,
		splitSegmentIndex: Int,
		splitLocation: Point2D,
		newEdgeView: EdgeView<T>,
		newEdgeViewEndpointType: EdgeViewEndpointType,
		otherNewEdgeViewPortView: PortView<T>?,
		tailEdgeView: EdgeView<T>?
	): SplitEdgeViewResult<T> {
		LOG.trace("split EdgeView ${splitEdgeView.id} and connect to Port ${otherNewEdgeViewPortView?.port?.portId} of destination ConnectableView ${otherNewEdgeViewPortView?.owner?.id}")

		val nodeView = nodeViewFactorySupplier.invoke().create(splitEdgeView.netView as NetView<Any>, graphView) as NodeView<T>
		graphView.add(nodeView)

		// Splitting an EdgeView should not change the layout of the EdgeView being split
		splitEdgeView.layout.isAdjusted = true
		val destinationDirection = splitEdgeView.getSegmentDirection(splitSegmentIndex)

		// Create tail part of EdgeView that is being split
		val tail = splitEdgeView.split(
			splitSegmentIndex,
			splitLocation
		) { tailEdgeView ?: edgeViewFactorySupplier.invoke().createEdgeView(graphView, it as NetView<Any>) as EdgeView<T> }

		nodeView.location = splitLocation

		connectToDestination(splitEdgeView, Connection(nodeView), destinationDirection)

		graphView.add(tail)
		connectToOrigin(tail, Connection(nodeView), tail.getSegmentDirection(0))

		if (!graphView.contains(newEdgeView)) {
			graphView.add(newEdgeView)
		}
		if (splitEdgeView.model !== newEdgeView.model) {
			combineNetViews(graphView, splitEdgeView.netView!!, newEdgeView.netView!!)
		}

		// Connect newEdgeView
		when (newEdgeViewEndpointType) {
			EdgeViewEndpointType.ORIGIN -> {
				connectToOrigin(newEdgeView, Connection(nodeView))
				if (otherNewEdgeViewPortView != null) {
					connectToDestination(newEdgeView, Connection(otherNewEdgeViewPortView.owner!!, otherNewEdgeViewPortView.port))
				}
			}
			EdgeViewEndpointType.DESTINATION -> {
				connectToDestination(newEdgeView, Connection(nodeView))
				if (otherNewEdgeViewPortView != null) {
					connectToOrigin(newEdgeView, Connection(otherNewEdgeViewPortView.owner!!, otherNewEdgeViewPortView.port))
				}
			}
		}

		return SplitEdgeViewResult(
			newEdgeView = newEdgeView,
			tailEdgeView = tail,
			nodeView = nodeView)
	}

	private fun <T: Any> combineNetViews(graphView: GraphView, netView1: NetView<T>, netView2: NetView<T>) {
		val oldNet = netView2.net
		netView1.combine(netView2)
		graphView.graph?.remove(oldNet)
		graphView.removeNetView(netView2)
	}

	override fun <T : Any> removeNodeView(graphView: GraphView, nodeView: NodeView<T>) {
		LOG.trace("removeNodeView: ${nodeView.id}")

		when (nodeView.getEdgeViews().size) {
			3 -> {
				throw IllegalStateException("Cannot remove NodeView with ${nodeView.getEdgeViews().size} EdgeViews")
			}
			2 -> {
				val nodeEdgeViews = nodeView.getEdgeViews()

				val joinedEdgeView = nodeEdgeViews[0]
				val joinedEndpointType = if (nodeView.getOutgoingEdgeViews().contains(joinedEdgeView)) {
					EdgeViewEndpointType.ORIGIN
				} else {
					EdgeViewEndpointType.DESTINATION
				}

				val otherEdgeView = nodeEdgeViews[1]
				val otherEndpointType = if (nodeView.getOutgoingEdgeViews().contains(otherEdgeView)) {
					EdgeViewEndpointType.ORIGIN
				} else {
					EdgeViewEndpointType.DESTINATION
				}

				unconnectEdgeViewFromNodeView(nodeView, joinedEdgeView)
				unconnectEdgeViewFromNodeView(nodeView, otherEdgeView)

				joinedEdgeView.join(joinedEndpointType,otherEdgeView, otherEndpointType)
				graphView.remove(otherEdgeView)
			}
			1 -> {
				unconnectEdgeViewFromNodeView(nodeView, nodeView.getEdgeViews()[0])
			}
		}

		graphView.remove(nodeView)
	}

	private fun unconnectEdgeViewFromNodeView(nodeView: NodeView<*>, edgeView: EdgeView<*>) {
		if (edgeView.origin?.connectableView === nodeView) {
			unconnectEdgeViewOrigin(edgeView)
		} else if (edgeView.destination?.connectableView === nodeView) {
			unconnectEdgeViewDestination(edgeView)
		}
	}

	override fun <T : Any> join(
		graphView: GraphView,
		edgeView1: EdgeView<T>,
		endpointType1: EdgeViewEndpointType,
		edgeView2: EdgeView<T>,
		endpointType2: EdgeViewEndpointType
	) {
		endpointType1.moveTo(edgeView1, endpointType2.getEndpoint(edgeView2).location)

		/** Remember the [Connection]s being reset in [EdgeView.join].*/
		val oldOrigin2 = edgeView2.origin
		val oldDestination2 = edgeView2.destination

		/** This joins the [EdgeView]s only on the view layer. Requires cleaning up the model layer afterward. */
		edgeView1.join(endpointType1, edgeView2, endpointType2)

		oldOrigin2?.let { conn ->
			conn.port?.let { port ->
				edgeView2.model.unconnect(port)
			}
		}
		oldDestination2?.let { conn ->
			conn.port?.let { port ->
				edgeView2.model.unconnect(port)
			}
		}

		edgeView1.origin?.let { conn ->
			conn.port?.let { port ->
				if (!port.isConnected) {
					edgeView1.model.connect(port)
				}
			}
		}
		edgeView1.destination?.let { conn ->
			conn.port?.let { port ->
				if (!port.isConnected) {
					edgeView1.model.connect(port)
				}
			}
		}

		if (edgeView1.model !== edgeView2.model) {
			combineNetViews(graphView, edgeView1.netView!!, edgeView2.netView!!)
		}

		graphView.remove(edgeView2)
	}

	private fun <T : Any> connectPortToNet(port: Port<T>?, net: Net<T>) {
		port?.let {
			if (!net.isConnectedWith(it)) {
				net.connect(it)
			}
		}
	}

	private fun <T : Any> unconnectPortFromNet(port: Port<T>?, net: Net<T>) {
		port?.let {
			if (net.isConnectedWith(it)) {
				net.unconnect(it)
			}
		}
	}
}