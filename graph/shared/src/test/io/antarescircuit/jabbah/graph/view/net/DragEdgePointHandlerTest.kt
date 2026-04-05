package io.antarescircuit.jabbah.graph.view.net

import io.antarescircuit.jabbah.base.event.MouseEventType
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.editor.InputEventDriver
import io.antarescircuit.jabbah.graph.view.AbstractInputEventHandlerTest
import io.antarescircuit.jabbah.graph.view.net.edge.DragEdgePointHandler
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewInputEventHandler
import io.antarescircuit.jabbah.graph.view.net.edge.LayoutType
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

class DragEdgePointHandlerTest : AbstractInputEventHandlerTest() {

	private val v3: TestVerticeView
	private val edgeViewInputEventHandler get() = handler as EdgeViewInputEventHandler
	private val edgeView get() = builder.graphView.getEdgeViews().first()

	init {
		handler = DragEdgePointHandler()
		v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v2", 200, 200))

		builder.connect(v1, v3)
		editor.commandManager.reset()
		edgeView.layout.type = LayoutType.NONE
		edgeViewInputEventHandler.edgeView = edgeView
	}

	override fun mouseMoveTo(x: Int, y: Int, modifiers: Int): InputEventDriver {
		handler.mouseMoved(context(MouseEventType.MOVED, x, y, modifiers))
		return this
	}

	@Test
	fun shouldDragEdgePoint() {
		dragEdgePoint()

		assertEdgePointDragged()
	}

	private fun dragEdgePoint() {
		editor.view.selectionManager.select(edgeView)
		mouseMoveTo(160, 100)
		pressMouseAt(160, 100)
		dragMouseTo(140, 100)
		releaseMouseAt(140, 100)
	}

	private fun assertEdgePointDragged() {
		val newEv = builder.graphView.getEdgeViews().first()
		assertEquals(
			listOf(Point2D(120, 100), Point2D(140, 100), Point2D(160, 200), Point2D(200, 200)),
			newEv.polyline.getPoints(0, newEv.segmentPointCount)
		)
	}

	@Test
	fun shouldUndoDragEdgePoint() {
		dragEdgePoint()

		editor.commandManager.undo()

		assertOriginalEdgeView()
	}

	private fun assertOriginalEdgeView() {
		val newEv = builder.graphView.getEdgeViews().first()
		assertEquals(
			listOf(Point2D(120, 100), Point2D(160, 100), Point2D(160, 200), Point2D(200, 200)),
			newEv.polyline.getPoints(0, newEv.segmentPointCount)
		)
	}

	@Test
	fun shouldRedoDragEdgePoint() {
		dragEdgePoint()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertEdgePointDragged()
	}
}