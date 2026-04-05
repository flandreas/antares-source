package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.event.Modifier
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.health.GraphViewConsistencyCheck
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.TestVertice
import io.antarescircuit.jabbah.graph.view.AbstractInputEventHandlerTest
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.netview.NetViewStyle
import io.antarescircuit.jabbah.graph.view.net.node.NodeView
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import io.antarescircuit.jabbah.graphics.Graphics2DMockBuilder
import dev.mokkery.verify
import kotlin.test.*

class EdgeToPortConnectorTest : AbstractInputEventHandlerTest() {

	private lateinit var ev: EdgeView<*>
	private lateinit var v3: TestVerticeView

	@BeforeTest
	fun initialize() {
		handler = GraphViewModule.edgeToPortOrEdgeConnector.handler
		ev = GraphViewModule.graphViewConnectService.addConnection<Boolean>(builder.graphView, v1, v2)
		v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))

		editor.commandManager.reset()
		CurrentConnectMethod.defaultMethod = ConnectMethod.AutoLayout
	}

	@Test
	fun shouldConnectToPortView() {
		shouldConnectToPortViewImpl()
	}

	@Test
	fun shouldConnectToPortViewInBlockStyle() {
		ev.netView?.style = NetViewStyle.BLOCK
		shouldConnectToPortViewImpl()

		val appContext = GraphApplicationContext(CurrentSystemSpeedCategory(SystemSpeed()))
		val context = DrawContext(Graphics2DMockBuilder().build(), appContext = appContext)
		context.color = CompositeColor()
		builder.graphView.draw(context)
	}

	private fun shouldConnectToPortViewImpl() {
		mouseMoveTo(150, 100, modifiers = Modifier.Alt.mask)
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
			width = TestVerticeView.DEF_SIZE
		)
		builder.addVerticeView(vv4)

		mouseMoveTo(150, 100, modifiers = Modifier.Alt.mask)
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

		assertEquals(drawablesCount, builder.graphView.drawables.size)
	}

	@Test
	fun shouldConnectToSoleOutputPort() {
		val v4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v4", 100, 300))
		val ev = builder.connectInputOpen(v3, Point2D(150, 200))

		mouseMoveTo(170, 200, modifiers = Modifier.Alt.mask)
		pressMouseAt(170, 200)
		dragMouseTo(130, 300)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		releaseMouseAt(130, 300)

		assertSame(ev.net, v4.model.getOutput<Boolean>().net)
	}

	/**
	 * There are systems (Windows generally?) that generate multiple key pressed events when moving/dragging
	 * the mouse while the key is being hold. Since [EdgeToPortOrEdgeConnector] is initiated by pressing the ALT key,
	 * the ALT key is typically still pressed while the StateMachine is already active.
	 */
	@Test
	fun shouldConnectWithInterferingKeyPressedEvents() {
		mouseMoveTo(150, 100, modifiers = Modifier.Alt.mask)
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
		assertEquals(7, builder.graphView.drawables.size)
	}

	@Test
	fun shouldUndoConnectToPortView() {
		mouseMoveTo(150, 100, modifiers = Modifier.Alt.mask)
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
		assertEquals(4, builder.graphView.drawables.size)

		GraphViewConsistencyCheck.execute(builder.graphView)
	}

	@Test
	fun shouldRedoConnectToPortView() {
		mouseMoveTo(150, 100, modifiers = Modifier.Alt.mask)
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
		mouseMoveTo(150, 100, modifiers = Modifier.Alt.mask)
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
		assertEquals(7, builder.graphView.drawables.size)

		GraphViewConsistencyCheck.execute(builder.graphView)
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
		mouseMoveTo(150, 100, modifiers = Modifier.Alt.mask)
		pressMouseAt(150, 100)
		releaseMouseAt(150, 100)

		// 3 VerticeViews, 1 EdgeView
		assertEquals(4, builder.graphView.drawables.size)
	}

	/** Regression test of GitHub issue #125. */
	@Test
	fun shouldNotCreateRedoEntryWhenCancellingAtStartLocation() {
		editor.commandManager.reset()

		val offset = Point2D(100, 0)
		v3.moveBy(offset.x, offset.y)
		EditModule.drawingAppService.move(listOf(v3), offset, editor, register = true)

		mouseMoveTo(150, 100, modifiers = Modifier.Alt.mask)
		pressMouseAt(150, 100)
		releaseMouseAt(150, 100)

		editor.commandManager.undo()
		assertEquals(Point2D(200, 200), builder.graphView.getWithId(v3.id)!!.location)

		editor.commandManager.redo()
		assertEquals(Point2D(300, 200), builder.graphView.getWithId(v3.id)!!.location)

		assertFalse(editor.commandManager.canRedo())
		assertEquals(0, builder.graphView.getDrawables { it is NodeView<*> }.size)

		GraphViewConsistencyCheck.execute(builder.graphView)
	}
}