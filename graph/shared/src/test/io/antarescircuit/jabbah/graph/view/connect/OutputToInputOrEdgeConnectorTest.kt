package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.event.Modifier
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.graph.health.GraphViewConsistencyCheck
import io.antarescircuit.jabbah.graph.view.AbstractInputEventHandlerTest
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointDenialCross
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.node.NodeView
import io.antarescircuit.jabbah.graph.view.port.TestPortView
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import dev.mokkery.verify
import kotlin.test.*

class OutputToInputOrEdgeConnectorTest : AbstractInputEventHandlerTest() {

	init {
		handler = GraphViewModule.outputToInputOrEdgeConnector.handler
		CurrentConnectMethod.defaultMethod = ConnectMethod.AutoLayout
	}

	@Test
	fun shouldConnectToInput() {
		mouseMoveTo(130, 100)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		verify { view.setCursor(Cursor.CROSSHAIR) }

		pressMouseAt(130, 100)
		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)

		dragMouseTo(150, 100)

		dragMouseTo(190, 100)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		releaseMouseAt(190, 100)

		assertConnected()
	}

	private fun assertConnected() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV1 = builder.graphView.getVerticeView("v1")!!
		val newV2 = builder.graphView.getVerticeView("v2")!!

		assertTrue(newEv.model.isConnectedWith(newV1.model.getOutput()))
		assertTrue(newEv.model.isConnectedWith(newV2.model.getInput()))

		GraphViewConsistencyCheck.execute(builder.graphView)
	}

	/**
	 * Consider unconnected [TestPortView.LENGTH] that gets completely replaced by [EdgeView].
	 */
	@Test
	fun shouldCancelWhenReleasingNearbyStart() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(130 - TestPortView.LENGTH, 100)
		releaseMouseAt(130 - TestPortView.LENGTH, 100)

		assertTrue(builder.graphView.getEdgeViews().isEmpty())
	}

	@Test
	fun shouldUndoConnect() {
		connect()

		editor.commandManager.undo()

		assertTrue(builder.graphView.getEdgeViews().isEmpty())
	}

	private fun connect() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(190, 100)
		releaseMouseAt(190, 100)
	}

	@Test
	fun shouldRedoConnect() {
		connect()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertConnected()
	}

	@Test
	fun shouldConnectOpenEnded() {
		connectOpenEnded()

		assertConnectOpenEnded()
	}

	private fun assertConnectOpenEnded() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV1 = builder.graphView.getVerticeView("v1")!!
		val newV2 = builder.graphView.getVerticeView("v2")!!

		assertTrue(newEv.model.isConnectedWith(newV1.model.getOutput()))
		assertFalse(newV2.model.getInput<Boolean>().isConnected)
		assertEquals(Point2D(150, 100), newEv.destinationEndpointView.location)
	}

	private fun connectOpenEnded() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(150, 100)
		releaseMouseAt(150, 100)
	}

	@Test
	fun shouldUndoConnectOpenEnded() {
		connectOpenEnded()

		editor.commandManager.undo()

		assertUnconnected()
	}

	private fun assertUnconnected() {
		assertTrue(builder.graphView.getEdgeViews().isEmpty())
	}

	@Test
	fun shouldRedoConnectOpenEnded() {
		connectOpenEnded()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertConnectOpenEnded()
	}

	@Test
	fun shouldCancelWithEscapeOutsideTarget() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(150, 100)

		pressEscape()

		assertTrue(builder.graphView.getEdgeViews().isEmpty())
		assertFalse(v1.model.getOutput<Boolean>().isConnected)
		assertFalse(v2.model.getInput<Boolean>().isConnected)
	}

	@Test
	fun shouldCancelWithEscapeInsideTarget() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(190, 100)

		pressEscape()

		assertTrue(builder.graphView.getEdgeViews().isEmpty())
		assertFalse(v1.model.getOutput<Boolean>().isConnected)
		assertFalse(v2.model.getInput<Boolean>().isConnected)
	}

	@Test
	fun shouldAdjustDestination() {
		connectAdjusted()

		assertConnected()
		assertAdjustedConnectionGeometry()
	}

	private fun connectAdjusted() {
		mouseMoveTo(130, 100)
		clickMouseAt(130, 100, modifiers = Modifier.Alt.mask)

		mouseMoveTo(150, 200)
		clickMouseAt(150, 200)

		mouseMoveTo(170, 200)
		assertEquals(4, draggedEdgeView.segmentPointCount)

		clickMouseAt(170, 200)
		assertEquals(5, draggedEdgeView.segmentPointCount)

		mouseMoveTo(190, 100)
		assertEquals(6, draggedEdgeView.segmentPointCount)

		clickMouseAt(190, 100)
	}

	private fun assertAdjustedConnectionGeometry() {
		val newEv = builder.graphView.getEdgeViews().first()

		assertEquals(
			listOf(
				Point2D(120, 100),
				Point2D(150, 100),
				Point2D(150, 200),
				Point2D(170, 200),
				Point2D(170, 100),
				Point2D(200, 100)),
			newEv.polyline.getPoints(0, newEv.segmentPointCount)
		)
	}

	@Test
	fun shouldUndoAdjustDestination() {
		connectAdjusted()

		editor.commandManager.undo()

		assertUnconnected()
	}

	@Test
	fun shouldRedoAdjustDestination() {
		connectAdjusted()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertConnected()
		assertAdjustedConnectionGeometry()
	}

	@Test
	fun shouldCancelAdjustWithEscapePressed() {
		mouseMoveTo(130, 100)
		clickMouseAt(130, 100, modifiers = Modifier.Alt.mask)

		mouseMoveTo(150, 200)
		clickMouseAt(150, 200)

		mouseMoveTo(170, 200)
		assertEquals(4, draggedEdgeView.segmentPointCount)

		pressEscape()
		assertEquals(3, draggedEdgeView.segmentPointCount)

		pressEscape()
		assertTrue(builder.graphView.getEdgeViews().isEmpty())
	}

	@Test
	fun shouldEndAdjustOpenWithDoubleClick() {
		mouseMoveTo(130, 100)
		clickMouseAt(130, 100, modifiers = Modifier.Alt.mask)

		mouseMoveTo(150, 200)
		clickMouseAt(150, 200)

		mouseMoveTo(170, 200)
		doubleClickMouseAt(170, 200)

		assertTrue(draggedEdgeView.model.isConnectedWith(v1.model.getOutput()))
		assertNull(draggedEdgeView.destination)
		assertTrue(editor.commandManager.canUndo())
	}

	@Test
	fun shouldCopeWithDeletingEdgeViewWhileCreating() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(160, 100)

		val edgeView = builder.graphView.getEdgeViews().first()

		// The design relies on the fact that EdgeViews under construction are not deletable.
		// Restriction to "deletable" is currently made by UI Actions, so we can't call
		// DrawingAppService.delete() here
		assertFalse(edgeView.deletable)

		dragMouseTo(170, 100)
	}

	@Test
	fun shouldConnectToEdgeView() {
		connectToEdgeView()
		assertConnectedToEdgeView()
	}

	@Test
	fun shouldDenyConnectToAnotherOutput() {
		builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 100, 200))

		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(130, 200)

		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		assertIs<ConnectionPointDenialCross>(ConnectionPointHighlighter.portViewHighlight)

		releaseMouseAt(130, 200)

		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)
		assertEquals(0, builder.graphView.getEdgeViews().size)
	}

	@Test
	fun shouldDenyConnectionToEdgeViewWithPresentOutputPort() {
		prepareConnection(outputCanBeUndefined = false)

		mouseMoveTo(130, 200)
		pressMouseAt(130, 200)
		dragMouseTo(150, 100)

		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		assertIs<ConnectionPointDenialCross>(ConnectionPointHighlighter.portViewHighlight)

		releaseMouseAt(130, 200)

		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)
		assertEquals(1, builder.graphView.getEdgeViews().size)
	}

	@Test
	fun shouldAdjustLayoutOrthogonalToLastSegment() {
		mouseMoveTo(130, 100)
		clickMouseAt(130, 100, modifiers = Modifier.Alt.mask)
		clickMouseAt(170, 100)

		mouseMoveTo(150, 200)

		assertEquals(Point2D(170, 200), draggedEdgeView.getSegmentPoint(2))
		assertEquals(Point2D(170, 100), draggedEdgeView.getSegmentPoint(1))
	}

	@Test
	fun shouldDenyAdjustToAnotherOutput() {
		val v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 100, 200))

		mouseMoveTo(130, 100, Modifier.Alt.mask)
		clickMouseAt(130, 100, modifiers = Modifier.Alt.mask)
		mouseMoveTo(130, 200)

		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		assertIs<ConnectionPointDenialCross>(ConnectionPointHighlighter.portViewHighlight)

		// Connect attempt to be rejected. Also, no intermediate point to be set.
		clickMouseAt(130, 200)

		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		assertIs<ConnectionPointDenialCross>(ConnectionPointHighlighter.portViewHighlight)
		assertFalse(v3.model.isConnected)
	}

	@Test
	fun shouldSwitchFromAdjustToAutoLayoutWithALT() {
		CurrentConnectMethod.defaultMethod = ConnectMethod.SetPoints

		mouseMoveTo(130, 100, modifiers = Modifier.Alt.mask)
		pressMouseAt(130, 100, modifiers = Modifier.Alt.mask)
		dragMouseTo(190, 100)
		clickMouseAt(190, 100)

		assertConnected()
	}

	private fun prepareConnection(outputCanBeUndefined: Boolean) {
		v1.model.getOutput<Boolean>().customCanBeUndefined = outputCanBeUndefined
		GraphViewModule.graphViewConnectService.addConnection<Boolean>(builder.graphView, v1, v2)
		builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 100, 200))
		editor.commandManager.reset()
	}

	private fun connectToEdgeView() {
		prepareConnection(outputCanBeUndefined = true)

		mouseMoveTo(130, 200)
		pressMouseAt(130, 200)
		dragMouseTo(150, 100)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		releaseMouseAt(150, 100)
	}

	private fun assertConnectedToEdgeView() {
		val nodeView = builder.graphView.getDrawable { it is NodeView<*> } as NodeView<*>
		val newV1 = builder.graphView.getVerticeView("v1")!!
		val newV2 = builder.graphView.getVerticeView("v2")!!
		val newV3 = builder.graphView.getVerticeView("v3")!!

		assertTrue(nodeView.model.isConnectedWith(newV1.model.getOutput()))
		assertTrue(nodeView.model.isConnectedWith(newV2.model.getInput()))
		assertTrue(nodeView.model.isConnectedWith(newV3.model.getOutput()))

		// 3 VerticeViews, 1 NodeView, 3 EdgeViews
		assertEquals(7, builder.graphView.drawables.size)
	}
}