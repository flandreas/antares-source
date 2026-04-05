package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.model.TestGraph
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView

/**
 * A view extension of [TestGraph].
 */
class TestGraphView(
	eventBus: EventBus = BaseModule.eventBus
) : TestGraph(eventBus) {

	val graphView = GraphViewModule.graphViewFactory.create(null)
	val vv1: TestVerticeView = TestVerticeView(vertice = v1, name = "1")
	val vv2: TestVerticeView = TestVerticeView(vertice = v2, name = "2")
	val ev: EdgeView<Boolean> = GraphViewModule.getEdgeViewFactory().createEdgeView(graphView, netView)

	init {
		vv1.location = Point2D(100, 100)
		vv2.location = Point2D(200, 100)
		ev.connectToOrigin(Connection(vv1, vv1.model.getOutput()))
		ev.connectToDestination(Connection(vv2, vv2.model.getInput()))

		graphView.add(vv1).add(vv2).add(ev)
		ev.layout.layoutOrigin()
	}
}