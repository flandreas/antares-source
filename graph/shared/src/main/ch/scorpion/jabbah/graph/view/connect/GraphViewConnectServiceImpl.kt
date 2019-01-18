package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.net.node.NodeViewFactory
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * Standard implementation of the [GraphViewConnectService] interface.
 */
class GraphViewConnectServiceImpl(
	val edgeViewFactorySupplier: () -> EdgeViewFactory<Any>,
	val nodeViewFactorySupplier: () -> NodeViewFactory<Any>
) : GraphViewConnectService {

	companion object {
		private val LOG by logger(GraphViewConnectServiceImpl::class)
	}

	/** ---- [GraphViewConnectService] interface */

	override fun <T : Any> connectToOrigin(edgeView: EdgeView<T>, orig: ConnectableView, port: Port<T>?) {
		connectToOrigin(edgeView, orig, port, layout = true);
	}

	override fun <T : Any> unconnectFromOrigin(edgeView: EdgeView<T>): JoinEdgeViewsResult<T>? {
		LOG.debug("unconnectFromOrigin EdgeView: ${edgeView.id}")
		var origNodeView: NodeView<T>? = null
		if (edgeView.origin is NodeView<*>) {
			origNodeView = edgeView.origin as NodeView<T>
		}

		edgeView.originPort?.let {
			if (edgeView.model!!.isConnectedWith(edgeView.originPort!!)) {
				edgeView.model!!.unconnect(edgeView.originPort!!)
			}
		}

		edgeView.connectToOrigin(null, null)

		if (origNodeView != null && origNodeView.getEdgeViews().size == 2) {
			val tailEdgeView = origNodeView.getEdgeViews()[1]
			val joinedEdgeView = removeNodeView<T>((origNodeView.parent as GraphView<GraphElementView<*>>), origNodeView)
			val destPortView = if (edgeView.destinationPort != null) edgeView.destination?.getPortView(edgeView.destinationPort!!) else null
			return JoinEdgeViewsResult<T>(
				joinedEdgeView,
				joinedEdgeView.findSegment(origNodeView.location.x, origNodeView.location.y)!!,
				edgeView,
				destPortView,
				tailEdgeView
			)
		}
		return null
	}

	override fun <T : Any> connectToDestination(edgeView: EdgeView<T>, dest: ConnectableView, port: Port<T>?) {
		connectToDestination(edgeView, dest, port, layout = true);
	}

	override fun <T : Any> unconnectFromDestination(edgeView: EdgeView<T>): JoinEdgeViewsResult<T>? {
		LOG.debug("unconnectFromDestination EdgeView: ${edgeView.id}")
		var destNodeView: NodeView<T>? = null
		if (edgeView.destination is NodeView<*>) {
			destNodeView = edgeView.destination as NodeView<T>
		}

		edgeView.destinationPort?.let {
			if (edgeView.model!!.isConnectedWith(edgeView.destinationPort!!)) {
				edgeView.model!!.unconnect(edgeView.destinationPort!!)
			}
		}
		edgeView.connectToDestination(null, null)

		if (destNodeView != null && destNodeView.getEdgeViews().size == 2) {
			val tailEdgeView = destNodeView.getEdgeViews()[1]
			val joinedEdgeView = removeNodeView<T>((destNodeView.parent as GraphView<GraphElementView<*>>), destNodeView)
			val origPortView = if (edgeView.originPort != null) edgeView.origin?.getPortView(edgeView.originPort!!) else null
			return JoinEdgeViewsResult<T>(
				joinedEdgeView,
				joinedEdgeView.findSegment(destNodeView.location.x, destNodeView.location.y)!!,
				edgeView,
				origPortView,
				tailEdgeView
			)
		}
		return null
	}

	override fun <T : Any> connect(edgeView: EdgeView<T>, orig: VerticeView<*>, dest: VerticeView<*>) {
		val outputPortView = orig.getPortView(orig.vertice.getOutput<T>())
		val inputPortView = dest.getPortView(dest.vertice.getInput<T>())
		connect(edgeView, outputPortView!!, inputPortView!!)
	}

	override fun <T : Any> connect(edgeView: EdgeView<T>, origOutput: PortView<T>?, destInput: PortView<T>?) {
		if (origOutput != null) {
			connectToOrigin(edgeView, origOutput.owner!!, origOutput.port)
		}
		if (destInput != null) {
			connectToDestination(edgeView, destInput.owner!!, destInput.port)
		}
	}

	override fun <T : Any> unconnect(edgeView: EdgeView<T>): JoinEdgeViewsResult<T>? {
		LOG.debug("unconnect")
		val origPortView = if (edgeView.originPort != null) edgeView.origin?.getPortView(edgeView.originPort!!) else null

		val originJoinResults = unconnectFromOrigin(edgeView)
		val destJoinResults = unconnectFromDestination(edgeView)
		if (originJoinResults != null) {
			return originJoinResults
		}

		if (destJoinResults != null) {
			// Rebuild JoinEdgeViewsResult in order to include origPortView (which has been reset by unconnectFromOrigin)
			return JoinEdgeViewsResult(
				destJoinResults.joinedEdgeView,
				destJoinResults.segmentIndex,
				destJoinResults.removedEdgeView,
				origPortView,
				destJoinResults.tailEdgeView)
		}

		return null
	}

	override fun unconnectEdgeViewOrigin(edgeView: EdgeView<Any>) {
		if (edgeView.model != null && edgeView.originPort != null) {
			edgeView.model!!.unconnect(edgeView.originPort!!)
			edgeView.connectToOrigin(null, null)
		}
	}

	override fun unconnectEdgeViewDestination(edgeView: EdgeView<Any>) {
		if (edgeView.model != null && edgeView.destinationPort != null) {
			edgeView.model!!.unconnect(edgeView.destinationPort!!)
			edgeView.connectToDestination(null, null)
		}
	}

	override fun unconnect(graphView: GraphView<out GraphElementView<*>>, verticeView: VerticeView<*>) {
		graphView.getEdgeViews()
			.filter { ev -> ev.origin === verticeView }
			.forEach { ev -> unconnectEdgeViewOrigin(ev) }
		graphView.getEdgeViews()
			.filter { ev -> ev.destination === verticeView }
			.forEach { ev -> unconnectEdgeViewDestination(ev) }
	}

	override fun <T : Any> addConnection(graphView: GraphView<GraphElementView<*>>, orig: VerticeView<*>, dest: VerticeView<*>): EdgeView<T> {
		LOG.debug("addConnection from VerticeView ${orig.id} to VerticeView ${dest.id}")
		val edgeView = edgeViewFactorySupplier.invoke().createEdgeView() as EdgeView<T>
		graphView.add(edgeView)
		connect(edgeView, orig, dest)
		return edgeView
	}

	override fun <T : Any> addConnection(
		graphView: GraphView<GraphElementView<*>>,
		origOutput: PortView<T>,
		destInput: PortView<T>
	): EdgeView<T> {
		val edgeView = edgeViewFactorySupplier.invoke().createEdgeView() as EdgeView<T>
		LOG.debug("add EdgeView ${edgeView.id} from Port ${origOutput.port.portId} of origin ${origOutput.owner?.id} to Port ${destInput.port.portId} of destination ${destInput.owner?.id}")
		graphView.add(edgeView)
		connect(edgeView, origOutput, destInput)
		return edgeView
	}

	override fun <T : Any> split(
		graphView: GraphView<GraphElementView<*>>,
		splittedEdgeView: EdgeView<T>,
		splitSegmentIndex: Int,
		newEdgeView: EdgeView<T>,
		destPort: PortView<T>?,
		tailEdgeView: EdgeView<T>?
	): SplitEdgeViewResult<T> {
		LOG.debug("split EdgeView ${splittedEdgeView.id} and connect to Port ${destPort?.port?.portId} of destination ConnectableView ${destPort?.owner?.id}")

		val nodeView = nodeViewFactorySupplier.invoke().create(splittedEdgeView.model as Net<Any>) as NodeView<T>
		graphView.add(nodeView)

		// Create tail part of EdgeView that is being split
		val tail = splittedEdgeView.split(
			splitSegmentIndex,
			newEdgeView.getSegmentPoint(0)
		) { tailEdgeView ?: edgeViewFactorySupplier.invoke().createEdgeView(it as Net<Any>) } as EdgeView<T>

		// Update view of head part of EdgeView that is being split
		val origSplittedEVConnState = splittedEdgeView.connectionState
		when (origSplittedEVConnState) {
			EdgeViewConnectionState.Input -> {
				nodeView.location = newEdgeView.polyline.getLastPoint()
				connectToOrigin(splittedEdgeView, nodeView, null, true)
			}
			EdgeViewConnectionState.Output, EdgeViewConnectionState.Unconnected -> {
				nodeView.location = newEdgeView.polyline.getFirstPoint()
				connectToDestination(splittedEdgeView, nodeView, null, true)
			}
		}

		graphView.add(tail)
		connectToOrigin(tail, nodeView, null, true)

		graphView.add(newEdgeView)

		// Connect newEdgeView
		when (origSplittedEVConnState) {
			EdgeViewConnectionState.Input -> {
				connectToDestination<T>(newEdgeView, nodeView, null)
				if (destPort != null) {
					connectToOrigin(newEdgeView, destPort.owner!!, destPort.port)
				}
			}
			EdgeViewConnectionState.Output, EdgeViewConnectionState.Unconnected -> {
				connectToOrigin<T>(newEdgeView, nodeView, null)
				if (destPort != null) {
					connectToDestination(newEdgeView, destPort.owner!!, destPort.port)
				}
			}
		}

		return SplitEdgeViewResult<T>(
			newEdgeView = newEdgeView,
			tailEdgeView = tail,
			nodeView = nodeView)
	}

	override fun <T : Any> removeNodeView(graphView: GraphView<GraphElementView<*>>, nodeView: NodeView<T>): EdgeView<T> {
		LOG.debug("removeNodeView: ${nodeView.id}")
		if (nodeView.getEdgeViews().size != 2) {
			throw IllegalStateException("Cannot remove NodeView with ${nodeView.getEdgeViews().size}  EdgeViews")
		}

		val edgeView = nodeView.getEdgeViews()[0].join(nodeView.getEdgeViews()[1]) as EdgeView<T>
		nodeView.getEdgeViews().forEach { graphView.remove(it) }

		graphView.remove(nodeView)

		return edgeView
	}

	/** ---- [GraphViewConnectServiceImpl] */

	private fun <T : Any> connectToOrigin(edgeView: EdgeView<T>, orig: ConnectableView, port: Port<T>?, layout: Boolean) {
		LOG.debug("connect EdgeView ${edgeView.id} to Port ${port?.portId} of origin ConnectableView ${orig.id}")
		checkNotNull(orig)
		if (port != null) {
			edgeView.model!!.connect(port)
		}
		edgeView.connectToOrigin(orig, port)
		if (layout) {
			edgeView.layoutOrigin()
		}
	}

	private fun <T : Any> connectToDestination(edgeView: EdgeView<T>, dest: ConnectableView, port: Port<T>?, layout: Boolean) {
		LOG.debug("connect EdgeView ${edgeView.id} to Port ${port?.portId} of destination ConnectableView ${dest.id}")
		checkNotNull(dest)
		if (port != null) {
			edgeView.model!!.connect(port)
		}
		edgeView.connectToDestination(dest, port)
		if (layout) {
			edgeView.layoutDestination()
		}
	}
}