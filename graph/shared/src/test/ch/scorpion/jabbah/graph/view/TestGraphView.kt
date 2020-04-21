package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.TestGraph
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView

/**
 * A view extension of [TestGraph].
 */
class TestGraphView(
	eventBus: EventBus = BaseModule.eventBus
) : TestGraph(eventBus) {

	val graphView = GraphViewModule.createGraphView()
	val vv1: TestVerticeView = TestVerticeView(vertice = v1, name = "1")
	val vv2: TestVerticeView = TestVerticeView(vertice = v2, name = "2")
	val ev: EdgeView<Boolean> = GraphViewModule.getEdgeViewFactory<Boolean>().createEdgeView(net)

	init {
		vv1.location = Point2D(100, 100)
		vv2.location = Point2D(200, 100)
		ev.connectToOrigin(Connection(vv1, vv1.model.getOutput()))
		ev.connectToDestination(Connection(vv2, vv2.model.getInput()))

		graphView.add(vv1).add(vv2).add(ev)
		ev.layout.layoutOrigin()
	}
}