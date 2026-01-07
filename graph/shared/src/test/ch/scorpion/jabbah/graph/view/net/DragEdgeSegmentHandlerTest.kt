package ch.scorpion.jabbah.graph.view.net

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.net.edge.DragEdgeSegmentHandler
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewInputEventHandler
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

class DragEdgeSegmentHandlerTest : AbstractInputEventHandlerTest() {

	private val v3: TestVerticeView

	init {
		handler = DragEdgeSegmentHandler()
		v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v2", 200, 200))
		builder.connect(v1, v3)
		editor.commandManager.reset()
		edgeViewInputEventHandler.edgeView = builder.graphView.getEdgeViews().first()
	}

	private val edgeViewInputEventHandler get() = handler as EdgeViewInputEventHandler

	@Test
	fun shouldMoveSegment() {
		moveSegment()

		assertSegmentMoved()
	}

	private fun moveSegment() {
		mouseMoveTo(160, 150)
		pressMouseAt(160, 150)
		dragMouseTo(190, 150)
		releaseMouseAt(190, 150)
	}

	private fun assertSegmentMoved() {
		val newEv = builder.graphView.getEdgeViews().first()
		assertEquals(
			listOf(Point2D(120, 100), Point2D(190, 100), Point2D(190, 200), Point2D(200, 200)),
			newEv.polyline.getPoints(0, newEv.segmentPointCount)
		)
	}

	@Test
	fun shouldUndoMoveSegment() {
		moveSegment()

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
	fun shouldRedoMoveSegment() {
		moveSegment()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertSegmentMoved()
	}

	/** Test for fix of bug GitHub#48.*/
	@Test
	fun shouldAnnihilateUShapedSegments() {
		val ev = builder.graphView.getEdgeViews().first()
		ev.moveSegment(2, Point2D(150, 200), Point2D(150, 300))

		mouseMoveTo(190, 250)
		pressMouseAt(190, 250)
		dragMouseTo(160, 250)
		releaseMouseAt(160, 250)

		assertEquals(4, ev.segmentPointCount)
	}
}