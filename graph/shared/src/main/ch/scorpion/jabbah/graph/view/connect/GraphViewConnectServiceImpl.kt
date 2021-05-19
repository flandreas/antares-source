package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.net.node.NodeViewFactory
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * Standard implementation of the [GraphViewConnectService] interface.
 */
class GraphViewConnectServiceImpl(
	private val edgeViewFactorySupplier: () -> EdgeViewFactory<Any>,
	private val nodeViewFactorySupplier: () -> NodeViewFactory<Any>
) : GraphViewConnectService {

	companion object {
		private val LOG by logger(GraphViewConnectServiceImpl::class)
	}

	/** ---- [GraphViewConnectService] interface */

	override fun <T : Any> connectToOrigin(edgeView: EdgeView<T>, connection: Connection<T>) {
		LOG.trace("connect EdgeView ${edgeView.id} to Port ${connection.port?.portId} of origin ConnectableView ${connection.connectableView.id}")
		connectPortToNet(connection.port, edgeView.model)
		edgeView.connectToOrigin(connection)
		edgeView.layout.layoutOrigin()
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

	override fun <T : Any> connectToDestination(edgeView: EdgeView<T>, connection: Connection<T>) {
		LOG.trace("connect EdgeView ${edgeView.id} to Port ${connection.port?.portId} of destination ConnectableView ${connection.connectableView.id}")
		connectPortToNet(connection.port, edgeView.model)
		edgeView.connectToDestination(connection)
		edgeView.layout.layoutDestination()
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
		val edgeView = edgeViewFactorySupplier.invoke().createEdgeView() as EdgeView<T>
		graphView.add(edgeView)
		connect(edgeView, orig, dest)
		return edgeView
	}

	override fun <T : Any> addConnection(
		graphView: GraphView,
		origOutput: PortView<T>,
		destInput: PortView<T>
	): EdgeView<T> {
		val edgeView = edgeViewFactorySupplier.invoke().createEdgeView() as EdgeView<T>
		LOG.trace("add EdgeView ${edgeView.id} from Port ${origOutput.port.portId} of origin ${origOutput.owner?.id} to Port ${destInput.port.portId} of destination ${destInput.owner?.id}")
		graphView.add(edgeView)
		connect(edgeView, origOutput, destInput)
		return edgeView
	}

	override fun <T : Any> split(
		graphView: GraphView,
		splitEdgeView: EdgeView<T>,
		splitSegmentIndex: Int,
		newEdgeView: EdgeView<T>,
		newEdgeViewEndpointType: EdgeViewEndpointType,
		otherNewEdgeViewPortView: PortView<T>?,
		tailEdgeView: EdgeView<T>?
	): SplitEdgeViewResult<T> {
		LOG.trace("split EdgeView ${splitEdgeView.id} and connect to Port ${otherNewEdgeViewPortView?.port?.portId} of destination ConnectableView ${otherNewEdgeViewPortView?.owner?.id}")

		val splitLocation = newEdgeViewEndpointType.getLocation(newEdgeView)
		val nodeView = nodeViewFactorySupplier.invoke().create(splitEdgeView.netView as NetView<Any>) as NodeView<T>
		graphView.add(nodeView)

		// Create tail part of EdgeView that is being split
		val tail = splitEdgeView.split(
			splitSegmentIndex,
			splitLocation
		) { tailEdgeView ?: edgeViewFactorySupplier.invoke().createEdgeView(it as NetView<Any>) as EdgeView<T> }

		nodeView.location = splitLocation
		connectToDestination(splitEdgeView, Connection(nodeView))

		graphView.add(tail)
		connectToOrigin(tail, Connection(nodeView))

		// Connect newEdgeView
		newEdgeView.net = splitEdgeView.net

		graphView.add(newEdgeView)

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

	override fun <T : Any> removeNodeView(graphView: GraphView, nodeView: NodeView<T>) {
		LOG.trace("removeNodeView: ${nodeView.id}")

		when (nodeView.getEdgeViews().size) {
			3 -> {
				throw IllegalStateException("Cannot remove NodeView with ${nodeView.getEdgeViews().size} EdgeViews")
			}
			2 -> {
				val nodeEdgeViews = nodeView.getEdgeViews()

				val joinedEdgeView = nodeEdgeViews[0]
				val otherEdgeView = nodeEdgeViews[1]

				unconnectEdgeViewFromNodeView(nodeView, joinedEdgeView)
				unconnectEdgeViewFromNodeView(nodeView, otherEdgeView)

				joinedEdgeView.join(otherEdgeView)
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