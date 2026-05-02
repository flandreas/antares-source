package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.graph.health.GraphViewConsistencyCheck
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.view.AbstractInputEventHandlerTest
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.node.NodeView
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import dev.mokkery.verify
import kotlin.test.*

class DragEdgeViewOriginConnectorTest
	: AbstractInputEventHandlerTest() {

	init {
		handler = GraphViewModule.dragEdgeViewOriginConnector.handler
		builder.connectInputOpen(v2, Point2D(150, 100))
		editor.commandManager.reset()
		CurrentConnectMethod.defaultMethod = ConnectMethod.AutoLayout
	}

	@Test
	fun shouldConnect() {
		mouseMoveTo(150, 100)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		verify { view.setCursor(Cursor.CROSSHAIR) }

		pressMouseAt(150, 100)
		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)

		dragMouseTo(140, 100)

		dragMouseTo(130, 100)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		releaseMouseAt(130, 100)

		assertConnected()
	}

	private fun assertConnected() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV1 = builder.graphView.getVerticeView("v1")!!

		assertTrue(newEv.model.isConnectedWith(newV1.model.getOutput()))
		assertEquals(newV1.getPortConnectionPoint(newV1.model.getOutput<Boolean>()), newEv.originEndpointView.location)

		GraphViewConsistencyCheck.execute(builder.graphView)
	}

	private fun assertOriginal() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV1 = builder.graphView.getVerticeView("v1")!!

		assertFalse(newEv.model.isConnectedWith(newV1.model.getOutput()))
		assertEquals(Point2D(150, 100), newEv.originEndpointView.location)
	}

	@Test
	fun shouldUndoConnect() {
		connect()

		editor.commandManager.undo()

		assertOriginal()
	}

	private fun connect() {
		mouseMoveTo(150, 100)
		pressMouseAt(150, 100)
		dragMouseTo(130, 100)
		releaseMouseAt(130, 100)
	}

	@Test
	fun shouldRedoConnect() {
		connect()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertConnected()
	}

	@Test
	fun shouldMoveOnly() {
		moveOnly()

		assertMoved()
	}

	private fun moveOnly() {
		mouseMoveTo(150, 100)
		pressMouseAt(150, 100)
		dragMouseTo(145, 100)
		releaseMouseAt(145, 100)
	}

	private fun assertMoved() {
		val newEv = builder.graphView.getEdgeViews().first()
		assertEquals(1, newEv.model.portsCount)
		assertEquals(Point2D(145, 100), newEv.originEndpointView.location)
	}

	@Test
	fun shouldUndoMoveOnly() {
		moveOnly()

		editor.commandManager.undo()

		assertOriginal()
	}

	@Test
	fun shouldRedoMoveOnly() {
		moveOnly()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertMoved()
	}

	@Test
	fun shouldDeleteHighlighterWhenEdgeViewIsDeleted() {
		val ev = builder.graphView.getEdgeViews().first()
		mouseMoveTo(150, 100)

		GraphViewModule.graphViewAppService.delete(listOf(ev), editor.view)

		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)
	}

	@Test
	fun shouldConnectToEdgeView() {
		connectToEdgeView()
		assertConnectedToEdgeView()
	}

	private fun connectToEdgeView() {
		v1.location = Point2D(100, 200)
		val v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))
		builder.connect(v1, v3)

		editor.commandManager.reset()

		mouseMoveTo(150, 100)
		pressMouseAt(150, 100)
		dragMouseTo(150, 200)
		releaseMouseAt(150, 200)
	}

	@Test
	fun shouldUndoConnectToEdgeView() {
		connectToEdgeView()

		editor.commandManager.undo()

		assertBeforeConnectToEdgeView()
	}

	@Test
	fun shouldRedoConnectToEdgeView() {
		connectToEdgeView()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertConnectedToEdgeView()
	}

	private fun assertBeforeConnectToEdgeView() {
		val newEv1 = builder.graphView.getEdgeViews().first()
		val newEv2 = builder.graphView.getEdgeViews().last()
		val newV1 = builder.graphView.getVerticeView("v1")!!
		val newV2 = builder.graphView.getVerticeView("v2")!!
		val newV3 = builder.graphView.getVerticeView("v3")!!

		assertTrue(newEv1.model.isConnectedWith(newV1.model.getOutput()))
		assertTrue(newEv1.model.isConnectedWith(newV3.model.getInput()))
		assertTrue(newEv2.model.isConnectedWith(newV2.model.getInput()))

		assertEquals(Point2D(150, 100), newEv2.originEndpointView.location)
		assertEquals(2, builder.graphView.netViewsCount)
		assertEquals(2, builder.graph.elements.filterIsInstance<Net<*>>().size)
	}

	private fun assertConnectedToEdgeView() {
		val nodeView = builder.graphView.getDrawable { it is NodeView<*> } as NodeView<*>
		val newV1 = builder.graphView.getVerticeView("v1")!!
		val newV2 = builder.graphView.getVerticeView("v2")!!
		val newV3 = builder.graphView.getVerticeView("v3")!!

		assertTrue(nodeView.model.isConnectedWith(newV1.model.getOutput()))
		assertTrue(nodeView.model.isConnectedWith(newV2.model.getInput()))
		assertTrue(nodeView.model.isConnectedWith(newV3.model.getInput()))

		// 3 VerticeViews, 1 NodeView, 3 EdgeViews
		assertEquals(7, builder.graphView.drawables.size)

		assertEquals(1, builder.graphView.netViewsCount)
		assertEquals(1, builder.graph.elements.filterIsInstance<Net<*>>().size)

		assertNull(nodeView.model.designError)
		builder.graphView.getEdgeViews().forEach {
			assertFalse(it.hasBrokenPortRef)
		}
	}
}