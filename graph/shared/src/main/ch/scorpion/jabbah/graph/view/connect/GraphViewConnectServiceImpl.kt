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

    private val LOG by logger()

    /** ---- [GraphViewConnectService] interface */

    override fun <T : Any> connectToOrigin(edgeView: EdgeView<T>, orig: ConnectableView, port: Port<T>?) {
        connectToOrigin(edgeView, orig, port, layout = true);
    }

    override fun <T : Any> unconnectFromOrigin(edgeView: EdgeView<T>): EdgeView<T>? {
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
            return removeNodeView<T>((origNodeView.parent as GraphView<GraphElementView<*>>), origNodeView)
        }
        return null
    }

    override fun <T : Any> connectToDestination(edgeView: EdgeView<T>, dest: ConnectableView, port: Port<T>?) {
        connectToDestination(edgeView, dest, port, layout = true);
    }

    override fun <T : Any> unconnectFromDestination(edgeView: EdgeView<T>): EdgeView<T>? {
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
            return removeNodeView<T>((destNodeView.parent as GraphView<GraphElementView<*>>), destNodeView)
        }
        return null
    }

    override fun <T : Any> connect(edgeView: EdgeView<T>, orig: VerticeView<*>, dest: VerticeView<*>) {
        val outputPortView = orig.getPortView(orig.vertice.getOutput<T>())
        val inputPortView = dest.getPortView(dest.vertice.getInput<T>())
        connect(edgeView, outputPortView!!, inputPortView!!)
    }

    override fun <T : Any> connect(edgeView: EdgeView<T>, origOutput: PortView<T>, destInput: PortView<T>) {
        connectToOrigin(edgeView, origOutput.owner!!, origOutput.port);
        connectToDestination(edgeView, destInput.owner!!, destInput.port);
    }

    override fun <T : Any> unconnect(edgeView: EdgeView<T>): EdgeView<T>? {
        LOG.debug("unconnect")
        val joinedOrigEdgeView = unconnectFromOrigin(edgeView)
        val joinedDestEdgeView = unconnectFromDestination(edgeView)
        if (joinedOrigEdgeView != null) {
            return joinedOrigEdgeView
        }
        return joinedDestEdgeView
    }

    override fun unconnect(graphView: GraphView<out GraphElementView<*>>, verticeView: VerticeView<*>) {
        graphView.getEdgeViews()
                .filter { ev -> ev.origin === verticeView }
                .forEach { ev ->
                    val port = ev.originPort
                    val net = ev.model
                    net!!.unconnect(port!!)
                    ev.connectToOrigin(null, null)
                }
        graphView.getEdgeViews()
                .filter { ev -> ev.destination === verticeView }
                .forEach { ev ->
                    val port = ev.destinationPort
                    val net = ev.model
                    net!!.unconnect(port!!)
                    ev.connectToDestination(null, null)
                }
    }

    override fun <T : Any> addConnection(graphView: GraphView<GraphElementView<*>>, orig: VerticeView<*>, dest: VerticeView<*>): EdgeView<T> {
        LOG.debug("addConnection from VerticeView ${orig.id} to VerticeView ${dest.id}")
        val edgeView = edgeViewFactorySupplier.invoke().createEdgeView() as EdgeView<T>
        graphView.add(edgeView)
        connect(edgeView, orig, dest)
        return edgeView
    }

    override fun <T : Any> addConnection(graphView: GraphView<GraphElementView<*>>, origOutput: PortView<T>, destInput: PortView<T>): EdgeView<T> {
        LOG.debug("addConnection from Port ${origOutput.port.portId} to Port ${destInput.port.portId}")
        val edgeView = edgeViewFactorySupplier.invoke().createEdgeView() as EdgeView<T>
        graphView.add(edgeView)
        connect(edgeView, origOutput, destInput)
        return edgeView
    }

    override fun <T : Any> split(graphView: GraphView<GraphElementView<*>>, splittedEdgeView: EdgeView<T>, splitSegmentIndex: Int, newEdgeView: EdgeView<T>, destInput: PortView<T>?): SplitEdgeViewResult<T> {
        LOG.debug("split EdgeView ${splittedEdgeView.id} and connect to Port ${if (destInput == null) null else destInput.port.portId}")

        val nodeView = nodeViewFactorySupplier.invoke().create(splittedEdgeView.model as Net<Any>) as NodeView<T>
        nodeView.location = newEdgeView.getSegmentPoint(0)
        graphView.add(nodeView)

        // Create tail part of EdgeView that is being split
        val tail = splittedEdgeView.split(
                splitSegmentIndex,
                newEdgeView.getSegmentPoint(0)
        ) { edgeViewFactorySupplier.invoke().createEdgeView(it as Net<Any>) } as EdgeView<T>

        // Update view of head part of EdgeView that is being split
        connectToDestination(splittedEdgeView, nodeView, null, true)

        graphView.add(tail)
        connectToOrigin(tail, nodeView, null, true)

        // Connect newEdgeView
        graphView.add(newEdgeView)
        connectToOrigin<T>(newEdgeView, nodeView, null)

        if (destInput != null) {
            connectToDestination(newEdgeView, destInput.owner!!, destInput.port)
        }

        // return tail;
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

    private fun <T: Any> connectToOrigin(edgeView: EdgeView<T>, orig: ConnectableView, port: Port<T>?, layout: Boolean) {
        LOG.debug("connectToOrigin EdgeView ${edgeView.id} Port ${port?.portId}")
        checkNotNull(orig)
        if (port != null) {
            edgeView.model!!.connect(port)
        }
        edgeView.connectToOrigin(orig, port)
        if (layout) {
            edgeView.layoutOrigin()
        }
    }

    private fun <T: Any> connectToDestination(edgeView: EdgeView<T>, dest: ConnectableView, port: Port<T>?, layout: Boolean) {
        LOG.debug("connectToDestination EdgeView ${edgeView.id} Port ${port?.portId}")
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