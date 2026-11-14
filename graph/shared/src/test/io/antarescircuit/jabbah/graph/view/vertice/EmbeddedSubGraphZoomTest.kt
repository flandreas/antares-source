package io.antarescircuit.jabbah.graph.view.vertice

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.antarescircuit.jabbah.base.geom.*
import io.antarescircuit.jabbah.draw.InputEventHandlerAdapter
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import io.antarescircuit.jabbah.edit.model.rectangle.ResizeRectangleCommand
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.snap.GridImpl
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.graph.TestLibraryBuilder
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.view.CompositeTestGraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphElementViewWrapper
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.TestGraphPortView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.io.StorableCloner
import kotlin.math.round
import kotlin.test.*

class EmbeddedSubGraphZoomTest {
	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
	}

	private fun vertex(extraPorts: Boolean = false): SubGraphVerticeViewImpl {
		val library = LibraryModule.libraryHolder.library
		val meta = TestLibraryBuilder().addInnerCustomComponent(library, "Zoom")
		val graph = meta.graph.graphView
		graph.add(GraphElementViewWrapper(RectangleComponent(-100.0, -70.0, 800.0, 500.0)))
		graph.getGraphPortView("I")!!.location = Point2D(-100, 30)
		graph.getGraphPortView("O")!!.location = Point2D(700, 90)
		if (extraPorts) {
			for (index in 0 until 8) {
				val input = TestGraphPortView.input<Boolean>("Extra$index").also { it.location = Point2D(-100, 32 + index * 2) }
				graph.add(input)
				val port = GraphModelModule.portFactory.createSubGraphPort(input.model, graph.graph!!.type)
				val factory = GraphViewModule.portViewFactory
				meta.containerDrawing.add(factory.createPortViewComponent(factory.createPortView(port)))
			}
			// Rebuild the symbol ports after extending the model's interface.
			val element = library.getContainerLibraryElement(meta.uuid)!!
			LibraryModule.libraryService.updateContainerLibraryElement(library, meta, element)
		}
		return library.getContainerLibraryElement(meta.uuid)!!.getNewInstance<Vertice>() as SubGraphVerticeViewImpl
	}

	@Test
	fun shouldFitUniformlyAndNeverEnlargeBeyondNativeSize() {
		val vertex = vertex()
		val presentation = EmbeddedSubGraphPresentation(vertex, vertex.createSubGraphView(null))
		vertex.setFrame(0.0, 0.0, presentation.preferredWidth, presentation.preferredHeight)
		presentation.layoutPorts()
		assertEquals(1.0, presentation.zoom, 0.00001)
		vertex.setFrame(0.0, 0.0, presentation.preferredWidth / 2, presentation.preferredHeight / 2)
		presentation.layoutPorts()
		assertTrue(presentation.zoom in 0.01..0.99)
		val a = presentation.fromInner(Point2D(-50, -30))
		val b = presentation.fromInner(Point2D(150, 70))
		assertEquals(2.0, (b.x - a.x) / (b.y - a.y), 0.00001)
		val restored = presentation.toInner(a)
		assertEquals(-50.0, restored.x, 0.00001)
		assertEquals(-30.0, restored.y, 0.00001)
		vertex.setFrame(0.0, 0.0, presentation.preferredWidth * 2, presentation.preferredHeight * 2)
		assertEquals(1.0, presentation.zoom)
		presentation.dispose()
	}

	@Test
	fun shouldKeepEndpointsOnTheContainingGridWhenShrunkRotatedMirroredAndMoved() {
		val vertex = vertex()
		val builder = GraphViewBuilder<Boolean>()
		val grid = GridImpl(chosenDistance = 13.0)
		builder.graphView.snapper = grid
		builder.add(vertex)
		vertex.displayMode = SubGraphDisplayMode.GRAPH
		for (rotation in Rotation.entries) {
			for (mirrored in listOf(false, true)) {
				vertex.rotation = rotation
				vertex.isHorizontallyMirrored = mirrored
				vertex.isVerticallyMirrored = !mirrored
				vertex.setFrame(23.5, -18.25, vertex.minimumWidth, vertex.minimumHeight)
				vertex.location = Point2D(41.5, 35.25)
				vertex.boundingBox
				val points = vertex.getPortViews().map { vertex.getPortConnectionPoint(it.port) }
				for (point in points) {
					assertEquals(round(point.x / grid.distance), point.x / grid.distance, 0.00001)
					assertEquals(round(point.y / grid.distance), point.y / grid.distance, 0.00001)
				}
				assertEquals(points.size, points.distinct().size)
			}
		}
		grid.distance = 7.0
		vertex.boundingBox
		vertex.getPortViews().forEach {
			val point = vertex.getPortConnectionPoint(it.port)
			assertEquals(round(point.x / 7), point.x / 7, 0.00001)
			assertEquals(round(point.y / 7), point.y / 7, 0.00001)
		}
		vertex.dispose()
		grid.dispose()
	}

	@Test
	fun shouldKeepDensePortsDistinctAndOrderedAtTheMinimumSize() {
		val vertex = vertex(extraPorts = true)
		val builder = GraphViewBuilder<Boolean>()
		val grid = GridImpl(chosenDistance = 7.0)
		builder.graphView.snapper = grid
		builder.add(vertex)
		val inner = vertex.createSubGraphView(null)
		inner.getGraphPortView("I")!!.location = Point2D(-100, 30)
		inner.getGraphPortView("O")!!.location = Point2D(-100, 90)
		for (index in 0 until 8) inner.getGraphPortView("Extra$index")!!.location = Point2D(-100, 32 + index * 2)
		val presentation = EmbeddedSubGraphPresentation(vertex, inner)
		vertex.setFrame(0.0, 0.0, presentation.minimumWidth, presentation.minimumHeight)
		presentation.layoutPorts()
		val names = listOf("I") + (0 until 8).map { "Extra$it" } + "O"
		val positions = names.map { name -> vertex.getPortConnectionPoint(vertex.getPortViews().first { it.port.name == name }.port).y }
		positions.zipWithNext().forEach { (a, b) -> assertTrue(b - a >= grid.distance - 0.00001, "Port positions: $positions") }
		presentation.dispose()
		vertex.dispose()
		grid.dispose()
	}

	@Test
	fun shouldDispatchSimulationEventsInInnerCoordinatesAtReducedZoom() {
		val vertex = vertex()
		val inner = vertex.createSubGraphView(null)
		val actor = inner.getVerticeViews().filterIsInstance<TestControlVerticeView>().first()
		actor.location = Point2D(250, 200)
		var received: Point2D? = null
		actor.actorInteractionHandler = object : InputEventHandlerAdapter<ActorInteractionContext>() {
			override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
				received = context.location
				return this
			}
		}
		val presentation = EmbeddedSubGraphPresentation(vertex, inner)
		vertex.rotation = Rotation.R90
		vertex.isHorizontallyMirrored = true
		vertex.setFrame(100.0, 100.0, presentation.minimumWidth + 20, presentation.minimumHeight + 20)
		presentation.layoutPorts()
		assertTrue(presentation.zoom < 1)
		val point = Point2D(270, 220)
		val world = vertex.handleToWorld(presentation.fromInner(point).add(vertex.location))
		val context = ActorInteractionContext(mock(MockMode.autofill), DrawingViewMockBuilder().build<GraphElementView<*>, GraphView>(), x = world.x, y = world.y)
		presentation.interactionHandler.mousePressed(context)
		assertNotNull(received)
		assertEquals(point.x, received.x, 0.00001)
		assertEquals(point.y, received.y, 0.00001)
		presentation.dispose()
	}

	@Test
	fun shouldReleaseInnerCaptureSoOutsideButtonsCanReceiveTheNextPress() {
		val vertex = vertex()
		val inner = vertex.createSubGraphView(null)
		val actor = inner.getVerticeViews().filterIsInstance<TestControlVerticeView>().first()
		actor.location = Point2D(250, 200)
		var innerPresses = 0
		var innerClicks = 0
		var outsidePresses = 0
		actor.actorInteractionHandler = object : InputEventHandlerAdapter<ActorInteractionContext>() {
			override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler {
				innerPresses++
				return this
			}
			override fun mouseReleased(context: ActorInteractionContext): ActorInteractionHandler? = null
			override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler {
				innerClicks++
				return this
			}
		}
		val outside = object : InputEventHandlerAdapter<ActorInteractionContext>() {
			override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
				outsidePresses++
				return null
			}
		}
		val presentation = EmbeddedSubGraphPresentation(vertex, inner)
		vertex.rotation = Rotation.R90
		vertex.isHorizontallyMirrored = true
		vertex.setFrame(100.0, 100.0, presentation.minimumWidth + 20, presentation.minimumHeight + 20)
		presentation.layoutPorts()
		val view = DrawingViewMockBuilder().build<GraphElementView<*>, GraphView>()
		fun context(point: Point2D) = ActorInteractionContext(mock(MockMode.autofill), view, x = point.x, y = point.y)
		val inside = context(vertex.handleToWorld(presentation.fromInner(Point2D(270, 220)).add(vertex.location)))
		val away = context(vertex.handleToWorld(Point2D(vertex.graphWidth + 200, vertex.graphHeight + 200).add(vertex.location)))
		var capture: ActorInteractionHandler? = null
		// Follow GraphViewExecutionHandler's routing: try capture, then hit-test a new actor.
		fun press(c: ActorInteractionContext, atPoint: ActorInteractionHandler) {
			if (capture != null) {
				capture = capture?.mousePressed(c)
				if (capture != null) return
			}
			capture = atPoint.mousePressed(c)
		}
		for (moveFirst in listOf(true, false)) {
			press(inside, presentation.interactionHandler)
			assertSame(presentation.interactionHandler, capture)
			capture = capture?.mouseReleased(inside)
			assertNull(capture)
			capture = presentation.interactionHandler.mouseClicked(inside)
			assertSame(presentation.interactionHandler, capture)
			if (moveFirst) {
				capture = capture?.mouseMoved(away)
				assertNull(capture)
			}
			press(away, outside)
		}
		assertEquals(2, innerPresses)
		assertEquals(2, innerClicks)
		assertEquals(2, outsidePresses)
		assertNull(presentation.interactionHandler.mouseClicked(away))
		presentation.dispose()
	}

	@Test
	fun shouldHonorChildHandlerTransitionsAndReleaseADragOutsideTheFrame() {
		val vertex = vertex()
		val inner = vertex.createSubGraphView(null)
		val actor = inner.getVerticeViews().filterIsInstance<TestControlVerticeView>().first()
		actor.location = Point2D(250, 200)
		var dragged = 0
		var released = 0
		val dragging = object : InputEventHandlerAdapter<ActorInteractionContext>() {
			override fun mouseDragged(context: ActorInteractionContext): ActorInteractionHandler {
				dragged++
				return this
			}
			override fun mouseReleased(context: ActorInteractionContext): ActorInteractionHandler? {
				released++
				return null
			}
		}
		actor.actorInteractionHandler = object : InputEventHandlerAdapter<ActorInteractionContext>() {
			override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler = dragging
		}
		val presentation = EmbeddedSubGraphPresentation(vertex, inner)
		vertex.setFrame(100.0, 100.0, presentation.minimumWidth + 20, presentation.minimumHeight + 20)
		presentation.layoutPorts()
		val view = DrawingViewMockBuilder().build<GraphElementView<*>, GraphView>()
		val point = presentation.fromInner(Point2D(270, 220)).add(vertex.location)
		val inside = ActorInteractionContext(mock(MockMode.autofill), view, x = point.x, y = point.y)
		val away = inside.withXY(10000.0, 10000.0)
		val capture = presentation.interactionHandler.mousePressed(inside)!!
		assertSame(capture, capture.mouseDragged(away))
		assertNull(capture.mouseReleased(away))
		assertNull(capture.mouseDragged(away))
		assertEquals(1, dragged)
		assertEquals(1, released)
		presentation.dispose()
	}

	@Test
	fun shouldKeepConnectedEdgesOnTheGridWhileShrinkingAndRestoringTheFrame() {
		val vertex = vertex()
		val builder = CompositeTestGraphViewBuilder("outer")
		val grid = GridImpl(chosenDistance = 7.0)
		builder.graphView.snapper = grid
		val graph = builder.buildOuterCustomComponent(vertex)
		val ports = vertex.getPortViews().toList()
		val edges = ports.map { graph.getEdgeView(it.port)!! }
		vertex.displayMode = SubGraphDisplayMode.GRAPH
		val original = Rectangle2D(vertex.shape)
		vertex.setFrame(23.5, 11.25, vertex.minimumWidth + 20, vertex.minimumHeight + 20)
		val smaller = Rectangle2D(vertex.shape)
		@Suppress("UNCHECKED_CAST")
		val drawing = graph as Drawing<Component>
		val editor = mock<Editor>(MockMode.autofill)
		every { editor.drawing } returns drawing
		val command = ResizeRectangleCommand(editor, vertex.id, original, smaller)
		for (restore in listOf(true, false)) {
			if (restore) command.undo() else command.execute()
			assertEquals(if (restore) original else smaller, vertex.shape)
			for ((index, port) in ports.withIndex()) {
				val edge = edges[index]
				assertSame(port, vertex.getPortViews()[index])
				assertSame(edge, graph.getEdgeView(port.port))
				val point = vertex.getPortConnectionPoint(port.port)
				val endpoint = if (edge.origin?.connectableView === vertex) edge.getSegmentPoint(0) else edge.getSegmentPoint(edge.segmentPointCount - 1)
				assertEquals(point, endpoint)
				assertEquals(round(point.x / 7), point.x / 7, 0.00001)
				assertEquals(round(point.y / 7), point.y / 7, 0.00001)
			}
		}
		vertex.dispose()
		grid.dispose()
	}

	@Test
	fun shouldPreserveASmallerFrameAcrossSerialization() {
		val vertex = vertex()
		vertex.displayMode = SubGraphDisplayMode.GRAPH
		vertex.setFrame(0.0, 0.0, vertex.minimumWidth + 25, vertex.minimumHeight + 25)
		val clone = StorableCloner.clone(vertex)
		assertEquals(vertex.shape.width, clone.shape.width, 0.001)
		assertEquals(vertex.shape.height, clone.shape.height, 0.001)
		assertEquals(SubGraphDisplayMode.GRAPH, clone.displayMode)
	}
}
