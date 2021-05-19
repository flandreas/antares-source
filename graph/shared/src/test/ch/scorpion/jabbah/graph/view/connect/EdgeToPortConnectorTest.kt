package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.ALT_MASK
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.state.StateMachine
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.*

class EdgeToPortConnectorTest
	: AbstractInputEventHandlerTest(GraphViewModule.edgeToPortConnector.handler) {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val ev = GraphViewModule.graphViewConnectService.addConnection<Boolean>(builder.graphView, v1, v2)
	private val v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))

	@BeforeTest
	fun initialize() {
		editor.commandManager.reset()
	}

	@Test
	fun shouldConnectToPortView() {
		shouldConnectToPortViewImpl()
	}

	@Test
	fun shouldConnectToPortViewInBlockStyle() {
		ev.netView?.style = NetViewStyle.BLOCK
		shouldConnectToPortViewImpl()

		val context = mockk<DrawContext>(relaxed = true)
		every { context.castedAppContext<GraphApplicationContext>() } returns mockk(relaxed = true)

		builder.graphView.draw(context)
	}

	private fun shouldConnectToPortViewImpl() {
		mouseMoveTo(150, 100, modifiers = ALT_MASK)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		verify { view.setCursor(Cursor.CROSSHAIR) }

		pressMouseAt(150, 100)
		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)

		dragMouseTo(150, 200)

		dragMouseTo(190, 200)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		releaseMouseAt(190, 200)

		assertEdgeToPortConnected(builder.graphView.getVerticeView("v3")!!.model.getInput())
	}

	@Test
	fun shouldConnectToUndefinedOutputPort() {
		val v4 = TestVertice(name = "v4", canBeUndefined = true)
		val vv4 = TestVerticeView(
			vertice = v4,
			loc = Point2D(200, 300),
			inputDirection = Direction.WEST,
			outputDirection = Direction.EAST,
			width = WIDTH
		)
		builder.addVerticeView(vv4)

		mouseMoveTo(150, 100, modifiers = ALT_MASK)
		pressMouseAt(150, 100)
		dragMouseTo(230, 300)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		releaseMouseAt(230, 300)

		assertEdgeToPortConnected(builder.graphView.getVerticeView("v4")!!.model.getOutput(),8)
	}

	// 3 VerticeViews, 1 NodeView, 3 EdgeViews
	private fun assertEdgeToPortConnected(destPort: Port<out Boolean>, drawablesCount: Int = 7) {
		// Instances have been recreated while replaying from undo snapshot
		val nodeView = builder.graphView.getDrawable { it is NodeView<*> } as NodeView<Boolean>
		val v1 = builder.graphView.getVerticeView("v1")!!
		val v2 = builder.graphView.getVerticeView("v2")!!

		assertTrue(nodeView.model.isConnectedWith(v1.model.getOutput()))
		assertTrue(nodeView.model.isConnectedWith(v2.model.getInput()))
		assertTrue(nodeView.model.isConnectedWith(destPort))

		assertEquals(drawablesCount, builder.graphView.drawablesCount)
	}

	/**
	 * There are systems (Windows generally?) that generate multiple key pressed events when moving/dragging
	 * the mouse while the key is being hold. Since [EdgeToPortConnector] is initiated by pressing the ALT key,
	 * the ALT key is typically still pressed while the StateMachine is already active. Make sure that
	 * [EdgeToPortConnector]'s [StateMachine]
	 */
	@Test
	fun shouldConnectWithInterferingKeyPressedEvents() {
		mouseMoveTo(150, 100, modifiers = ALT_MASK)
		pressAlt()
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		verify { view.setCursor(Cursor.CROSSHAIR) }

		pressMouseAt(150, 100)
		pressAlt()
		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)

		dragMouseTo(150, 200)

		dragMouseTo(190, 200)
		pressAlt()
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		releaseMouseAt(190, 200)
		pressAlt()

		assertTrue(ev.model.isConnectedWith(v1.model.getOutput()))
		assertTrue(ev.model.isConnectedWith(v2.model.getInput()))
		assertTrue(ev.model.isConnectedWith(v3.model.getInput()))

		// 3 VerticeViews, 1 NodeView, 3 EdgeViews
		assertEquals(7, builder.graphView.drawablesCount)
	}

	@Test
	fun shouldUndoConnectToPortView() {
		mouseMoveTo(150, 100, modifiers = ALT_MASK)
		pressMouseAt(150, 100)
		dragMouseTo(190, 200)
		releaseMouseAt(190, 200)

		editor.commandManager.undo()

		assertUnconnected()
	}

	private fun assertUnconnected() {
		// Instances have been recreated while replaying from undo snapshot
		val anyEv = builder.graphView.getEdgeViews().first()
		val newV1 = builder.graphView.getVerticeView("v1")!!
		val newV2 = builder.graphView.getVerticeView("v2")!!
		val newV3 = builder.graphView.getVerticeView("v3")!!

		assertTrue(anyEv.model.isConnectedWith(newV1.model.getOutput()))
		assertTrue(anyEv.model.isConnectedWith(newV2.model.getInput()))
		assertFalse(anyEv.model.isConnectedWith(newV3.model.getInput()))

		// 3 VerticeViews, 1 EdgeView
		assertEquals(4, builder.graphView.drawablesCount)
	}

	@Test
	fun shouldRedoConnectToPortView() {
		mouseMoveTo(150, 100, modifiers = ALT_MASK)
		pressMouseAt(150, 100)
		dragMouseTo(190, 200)
		releaseMouseAt(190, 200)

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertEdgeToPortConnected(builder.graphView.getVerticeView("v3")!!.model.getInput())
	}

	@Test
	fun shouldConnectOpenEnded() {
		connectOpenEnded()

		assertConnectedOpenEnded()
	}

	private fun connectOpenEnded() {
		mouseMoveTo(150, 100, modifiers = ALT_MASK)
		pressMouseAt(150, 100)
		dragMouseTo(150, 200)
		releaseMouseAt(150, 200)
	}

	private fun assertConnectedOpenEnded() {
		// Instances have been recreated while replaying from undo snapshot
		val nodeView = builder.graphView.getDrawable { it is NodeView<*> } as NodeView<Boolean>
		val newV1 = builder.graphView.getVerticeView("v1")!!
		val newV2 = builder.graphView.getVerticeView("v2")!!
		val newV3 = builder.graphView.getVerticeView("v3")!!

		assertTrue(nodeView.model.isConnectedWith(newV1.model.getOutput()))
		assertTrue(nodeView.model.isConnectedWith(newV2.model.getInput()))
		assertFalse(nodeView.model.isConnectedWith(newV3.model.getInput()))
		assertEquals(Point2D(150, 200), nodeView.getEdgeView(Direction.SOUTH)!!.polyline.getLastPoint())

		// 3 VerticeViews, 3 EdgeView, 1 NodeView
		assertEquals(7, builder.graphView.drawablesCount)
	}

	@Test
	fun shouldUndoConnectOpenEnded() {
		connectOpenEnded()

		editor.commandManager.undo()

		assertUnconnected()
	}

	@Test
	fun shouldRedoConnectOpenEnded() {
		connectOpenEnded()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertConnectedOpenEnded()
	}

	@Test
	fun shouldCancelReleaseAtStartLocation() {
		mouseMoveTo(150, 100, modifiers = ALT_MASK)
		pressMouseAt(150, 100)
		releaseMouseAt(150, 100)

		// 3 VerticeViews, 1 EdgeView
		assertEquals(4, builder.graphView.drawablesCount)
	}
}