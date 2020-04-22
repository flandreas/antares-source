package ch.scorpion.jabbah.graph.view.net

import ch.scorpion.jabbah.base.event.MouseEventType
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.net.edge.DragEdgePointHandler
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewInputEventHandler
import ch.scorpion.jabbah.graph.view.net.edge.LayoutType
import kotlin.test.Test
import kotlin.test.assertEquals

class DragEdgePointHandlerTest : AbstractInputEventHandlerTest(DragEdgePointHandler()) {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val v3 = builder.addVerticeView(createEastOutputVerticeView("v2", 200, 200))
	private val edgeViewInputEventHandler get() = handler as EdgeViewInputEventHandler
	private val edgeView get() = builder.graphView.getEdgeViews().first()

	init {
		builder.connect(v1, v3)
		editor.commandManager.reset()
		edgeView.layout.type = LayoutType.NONE
		edgeViewInputEventHandler.edgeView = edgeView
	}

	override fun mouseMoveTo(x: Int, y: Int, modifiers: Int) {
		handler.mouseMoved(context(MouseEventType.MOVED, x, y, modifiers))
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