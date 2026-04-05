package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.UndoableDataHolder
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.GraphStorable
import io.antarescircuit.jabbah.graph.model.*
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.connect.SplitEdgeViewResult
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewFactory
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import io.antarescircuit.jabbah.io.Storable

/**
 * Provides a convenient API for building [GraphView]s that contain connected [VerticeView]s.
 * @param T the type of signal
 */
open class GraphViewBuilder<T : Any>(
	graphStorable: GraphStorable,
	private val edgeViewFactory: EdgeViewFactory = GraphViewModule.getEdgeViewFactory(),
	private val undoableStateCallback: (GraphViewBuilder<*>) -> Unit = {}
) : UndoableDataHolder {

	constructor(
		name: String = Translations.getString("graph.name.unknown"),
		type: GraphType = GraphModelModule.graphTypeRegistry.default,
		undoableStateCallback: (GraphViewBuilder<*>) -> Unit = {}
	): this(GraphStorable(GraphViewModule.graphViewFactory.create(GraphModelModule.graphFactory.create(TranslatableText(name), type))), undoableStateCallback = undoableStateCallback)

	companion object {
		private val LOG by logger(GraphViewBuilder::class)
	}

	/** ---- [UndoableDataHolder] */

	var graphStorable: GraphStorable = graphStorable
		private set

	override fun getUndoableState(): Storable? {
		return graphStorable
	}

	override fun setUndoableState(state: Storable) {
		graphStorable = state as GraphStorable
		undoableStateCallback(this)
		LOG.trace("Set undoable state $state with GraphView ${graphView.hashCode().toString(16)}")
	}

	override fun undoableStateEstablished(state: Storable) {}

	/** ---- [GraphViewBuilder] */

	val graphView: GraphView get() = graphStorable.graphView
	val graph: Graph get() = graphView.graph!!

	fun build(): GraphView {
		return graphView
	}

	fun <T : GraphElementView<out GraphElement>> add(graphElementView: T): T {
		graphView.add(graphElementView)
		return graphElementView
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

	fun connect(
		from: VerticeView<out Vertice>,
		fromPort: OutputPort<T> = from.model.getOutput(),
		to: VerticeView<out Vertice>,
		toPort: InputPort<T> = to.model.getInput(),
		points: List<Point2D>
	): EdgeView<T> {
		val edgeView = edgeViewFactory.createEdgeView<T>(graphView)
		points.forEach { edgeView.addSegmentPoint(it) }
		edgeView.layout.isAdjusted = true
		graphView.add(edgeView)
		GraphViewModule.graphViewConnectService.connect(
			edgeView, from.getPortView(fromPort), to.getPortView(toPort)
		)
		return edgeView
	}

	fun connectOutputOpen(from: VerticeView<out Vertice>, toLocation: Point2D): EdgeView<T> {
		val edgeView = GraphViewModule.getEdgeViewFactory().createEdgeView<T>(graphView)
		edgeView.addSegmentPoint(Point2D.ZERO)
		edgeView.addSegmentPoint(Point2D.ZERO)
		graphView.add(edgeView)
		GraphViewModule.graphViewConnectService.connectToOrigin(edgeView, Connection(from, from.vertice.getOutput()))
		edgeView.moveDestinationEndPoint(toLocation.x, toLocation.y)
		return edgeView
	}

	fun connectInputOpen(to: VerticeView<out Vertice>, fromLocation: Point2D): EdgeView<T> {
		val edgeView = GraphViewModule.getEdgeViewFactory().createEdgeView<T>(graphView)
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

	fun split(
		edgeView: EdgeView<T>,
		segmentIndex: Int,
		location: Point2D,
		dest: PortView<T>?
	): SplitEdgeViewResult<T> {
		val newEdgeView = GraphViewModule.getEdgeViewFactory().createEdgeView(graphView, edgeView.netView!!)
		newEdgeView.addSegmentPoint(location)
		val result = GraphViewModule.graphViewConnectService.split(
			graphView,
			edgeView,
			segmentIndex,
			EdgeViewEndpointType.ORIGIN.getLocation(newEdgeView),
			newEdgeView,
			EdgeViewEndpointType.ORIGIN,
			dest)
		if (dest == null) {
			newEdgeView.addSegmentPoint(location)
		}
		return result
	}

	fun split(
		edgeView: EdgeView<T>,
		segmentIndex: Int,
		location: Point2D,
		dest: VerticeView<out Vertice>?
	): SplitEdgeViewResult<T> {
		var portView: PortView<T>? = null
		if (dest != null) {
			portView = dest.getPortView(dest.model.getPort())
		}
		return split(edgeView, segmentIndex, location, portView)
	}
}