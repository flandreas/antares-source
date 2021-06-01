package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.UndoableDataHolder
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.connect.SplitEdgeViewResult
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import ch.scorpion.jabbah.io.Storable

/**
 * Provides a convenient API for building [GraphView]s that contain connected [VerticeView]s.
 * @param T the type of signal
 */
open class GraphViewBuilder<T : Any>(
	name: String = Translations.getString("graph.name.unknown")
) : UndoableDataHolder {

	companion object {
		private val LOG by logger(GraphViewBuilder::class)
	}

	/** ---- [UndoableDataHolder] */

	var graphStorable: GraphStorable = GraphStorable(GraphViewModule.createGraphView(GraphModelModule.graphFactory.invoke(name)))
		private set

	override fun getUndoableState(): Storable? {
		return graphStorable
	}

	override fun setUndoableState(state: Storable) {
		graphStorable = state as GraphStorable
		LOG.trace("Set undoable state $state with GraphView ${graphView.hashCode().toString(16)}")
	}

	/** ---- [GraphViewBuilder] */

	val graphView: GraphView get() = graphStorable.graphView
	val graph: Graph get() = graphView.graph!!

	fun build(): GraphView {
		return graphView
	}

	fun <T : VerticeView<out Vertice>> addVerticeView(verticeView: T): T {
		graphView.add(verticeView)
		return verticeView
	}

	fun connect(from: VerticeView<out Vertice>, to: VerticeView<out Vertice>): EdgeView<T> {
		return connect(from, to, to.vertice.getInput())
	}

	fun connect(from: VerticeView<out Vertice>, to: VerticeView<out Vertice>, toPort: InputPort<T>): EdgeView<T> {
		return GraphViewModule.graphViewConnectService.addConnection(
			graphView, from.getPortView(from.vertice.getOutput())!!, to.getPortView(toPort)!!)
	}

	fun connect(
		from: VerticeView<out Vertice>,
		fromPort: OutputPort<T> = from.model.getOutput(),
		to: VerticeView<out Vertice>,
		toPort: InputPort<T> = to.model.getInput()
	): EdgeView<T> {
		return GraphViewModule.graphViewConnectService.addConnection(
			graphView, from.getPortView(fromPort)!!, to.getPortView(toPort)!!)
	}

	fun connectOutputOpen(from: VerticeView<out Vertice>, toLocation: Point2D): EdgeView<T> {
		val edgeView = GraphViewModule.getEdgeViewFactory<T>().createEdgeView()
		edgeView.addSegmentPoint(Point2D.ZERO)
		edgeView.addSegmentPoint(Point2D.ZERO)
		graphView.add(edgeView)
		GraphViewModule.graphViewConnectService.connectToOrigin(edgeView, Connection(from, from.vertice.getOutput()))
		edgeView.moveDestinationEndPoint(toLocation.x, toLocation.y)
		return edgeView
	}

	fun connectInputOpen(to: VerticeView<out Vertice>, fromLocation: Point2D): EdgeView<T> {
		val edgeView = GraphViewModule.getEdgeViewFactory<T>().createEdgeView()
		edgeView.addSegmentPoint(Point2D.ZERO)
		edgeView.addSegmentPoint(Point2D.ZERO)
		graphView.add(edgeView)
		GraphViewModule.graphViewConnectService.connectToDestination(edgeView, Connection(to, to.vertice.getInput()))
		edgeView.moveOriginEndPoint(fromLocation.x, fromLocation.y)
		return edgeView
	}

	fun reference(uuid: UUID): SubGraphVerticeView<SubGraphVerticeRef> {
		val vv = SubGraphVerticeViewImpl(SubGraphVerticeRef(graphUUID = uuid))
		graphView.add(vv)
		return vv
	}

	fun split(edgeView: EdgeView<T>, segmentIndex: Int, location: Point2D, dest: VerticeView<out Vertice>?): SplitEdgeViewResult<T> {
		val newEdgeView = GraphViewModule.getEdgeViewFactory<T>().createEdgeView(edgeView.netView!!)
		newEdgeView.addSegmentPoint(location)
		var portView: PortView<T>? = null
		if (dest != null) {
			portView = dest.getPortView(dest.model.getPort())
		}
		val result = GraphViewModule.graphViewConnectService.split(graphView, edgeView,
			segmentIndex, newEdgeView, EdgeViewEndpointType.ORIGIN, portView)
		if (dest == null) {
			newEdgeView.addSegmentPoint(location)
		}
		return result
	}
}