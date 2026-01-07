package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.event.Modifier
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertSame

class GraphViewInputEventHandlerTest : AbstractInputEventHandlerTest() {

	private val verticeView: TestVerticeView

	init {
		handler = InputEventHandlerAdapter()
		verticeView = TestVerticeView(loc = Point2D(100, 100), width = 200)
		builder.addVerticeView(verticeView)
	}

	@Test
	fun shouldForwardMouseMoveToInputToOutputConnector() {
		mouseMoveTo(300, 100, modifiers = Modifier.Alt.mask)
		assertSame(verticeView, GraphViewModule.outputToInputOrEdgeConnector.usedFor)
	}

	@Test
	fun shouldForwardMouseMoveToOutputToInputConnector() {
		mouseMoveTo(100, 100, modifiers = Modifier.Alt.mask)
		assertSame(verticeView, GraphViewModule.inputToOutputOrEdgeConnector.usedFor)
	}

	@Test
	fun shouldForwardMouseMoveToReconnectOriginConnector() {
		val ev = builder.connectOutputOpen(verticeView, Point2D(500, 100))
		mouseMoveTo(295, 100, modifiers = Modifier.Alt.mask)
		assertSame(ev, GraphViewModule.reconnectOriginConnector.usedFor)
	}

	@Test
	fun shouldForwardMouseMoveToReconnectDestinationConnector() {
		val ev = builder.connectInputOpen(verticeView, Point2D(0, 100))
		mouseMoveTo(105, 100, modifiers = Modifier.Alt.mask)
		assertSame(ev, GraphViewModule.reconnectDestinationConnector.usedFor)
	}
}
