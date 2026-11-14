package io.antarescircuit.jabbah.graph.view.vertice

import dev.mokkery.MockMode
import dev.mokkery.mock
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.TestLibraryBuilder
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryImpl
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.MemoryLibraryPersistenceService
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.view.CompositeTestGraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.io.StorableCloner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
class SubGraphVerticeViewImplIntegrationTest {

	private val signalHandler = mock<SignalHandler>(MockMode.autofill)

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
	}

	@Test
	fun shouldResizeEmbeddedGraphAndPreservePortsAcrossModeSwitches() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		val ports = vv.getPortViews().toList()
		val locations = ports.map { it.location }
		vv.displayMode = SubGraphDisplayMode.GRAPH
		kotlin.test.assertTrue(vv.minimumWidth > 0)
		kotlin.test.assertTrue(vv.minimumHeight > 0)
		vv.setFrame(100.0, 200.0, 1.0, 1.0)
		assertEquals(vv.minimumWidth, vv.shape.width)
		assertEquals(vv.minimumHeight, vv.shape.height)
		ports.forEachIndexed { i, port -> kotlin.test.assertSame(port, vv.getPortViews()[i]) }
		vv.displayMode = SubGraphDisplayMode.SYMBOL
		ports.forEachIndexed { i, port ->
			kotlin.test.assertSame(port, vv.getPortViews()[i])
			assertEquals(locations[i], port.location)
		}
	}

	@Test
	fun shouldStoreEmbeddedModeAndFrameSize() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		vv.displayMode = SubGraphDisplayMode.GRAPH
		vv.setFrame(100.0, 200.0, vv.minimumWidth + 100, vv.minimumHeight + 100)
		val clone = StorableCloner.clone(vv)
		assertEquals(SubGraphDisplayMode.GRAPH, clone.displayMode)
		assertEquals(vv.location, clone.location)
		assertEquals(vv.shape.width, clone.shape.width, 0.001)
		assertEquals(vv.shape.height, clone.shape.height, 0.001)
	}

	@Test
	fun shouldKeepConnectedEdgeEndpointsWhenSwitchingAndResizing() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		val builder = CompositeTestGraphViewBuilder("outer")
		val graph = builder.buildOuterCustomComponent(vv)
		val ports = vv.getPortViews().toList()
		val edges = ports.map { graph.getEdgeView(it.port)!! }
		vv.displayMode = SubGraphDisplayMode.GRAPH
		vv.setFrame(100.0, 200.0, vv.minimumWidth + 100, vv.minimumHeight + 100)
		for ((index, port) in ports.withIndex()) {
			val edge = edges[index]
			kotlin.test.assertSame(port, vv.getPortViews()[index])
			kotlin.test.assertSame(edge, graph.getEdgeView(port.port))
			val point = if (edge.origin?.connectableView === vv) edge.getSegmentPoint(0) else edge.getSegmentPoint(edge.segmentPointCount - 1)
			assertEquals(vv.getPortConnectionPoint(port.port), point)
		}
	}

	@Test
	fun shouldAlignPortsForEveryInnerConnectorDirection() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		vv.displayMode = SubGraphDisplayMode.GRAPH
		val inner = vv.createSubGraphView(null)
		val portView = inner.getGraphPortView("I")!!
		portView.location = Point2D(25, 45)
				val presentation = EmbeddedSubGraphPresentation(vv, inner)
		val outer = vv.getPortViews().first { it.port.name == "I" }
		for (direction in Direction.entries) {
			portView.getPortViews().first().direction = direction.opposite()
			presentation.layoutPorts()
			assertEquals(direction, outer.direction)
			when (direction) {
				Direction.WEST -> assertEquals(0.0, outer.location.x)
				Direction.EAST -> assertEquals(vv.graphWidth, outer.location.x)
				Direction.NORTH -> assertEquals(0.0, outer.location.y)
				Direction.SOUTH -> assertEquals(vv.graphHeight, outer.location.y)
			}
			if (direction == Direction.NORTH || direction == Direction.SOUTH) {
				assertEquals(presentation.fromInner(portView.location).x, outer.location.x, 0.00001)
			} else {
				assertEquals(presentation.fromInner(portView.location).y, outer.location.y, 0.00001)
			}
		}
		presentation.dispose()
	}

	@Test
	fun shouldKeepInnerContentBeyondTheInwardPortBounds() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		val original = vv.getPortViews().first { it.port.name == "I" }
		vv.removePortView(original)
		@Suppress("UNCHECKED_CAST")
		val port = object : io.antarescircuit.jabbah.graph.view.port.PortView<Any> by (original as io.antarescircuit.jabbah.graph.view.port.PortView<Any>) {
			override val boundingBox: io.antarescircuit.jabbah.base.geom.RectangularShape get() = when (direction) {
				Direction.WEST -> Rectangle2D(location.x, location.y - 5, 120.0, 10.0)
				Direction.EAST -> Rectangle2D(location.x - 120, location.y - 5, 120.0, 10.0)
				Direction.NORTH -> Rectangle2D(location.x - 5, location.y, 10.0, 120.0)
				Direction.SOUTH -> Rectangle2D(location.x - 5, location.y - 120, 10.0, 120.0)
			}
		}
		vv.addPortView(port)
		val inner = vv.createSubGraphView(null)
		val connector = inner.getGraphPortView("I")!!.getPortViews().first()
		val presentation = EmbeddedSubGraphPresentation(vv, inner)
		for (direction in Direction.entries) {
			connector.direction = direction.opposite()
			presentation.layoutPorts()
			vv.setFrame(0.0, 0.0, presentation.minimumWidth, presentation.minimumHeight)
			presentation.layoutPorts()
			val content = presentation.contentBox
			val label = port.boundingBox
			when (direction) {
				Direction.WEST -> kotlin.test.assertTrue(content.minX >= label.maxX + Look.SCALE)
				Direction.EAST -> kotlin.test.assertTrue(content.maxX <= label.minX - Look.SCALE)
				Direction.NORTH -> {
					val title = presentation.titleBounds
					kotlin.test.assertTrue(title.minY >= label.maxY + Look.SCALE)
					kotlin.test.assertTrue(title.minY < label.maxY + 2 * Look.SCALE)
					assertEquals(2 * Look.SCALE.toDouble(), content.minY - title.maxY, 0.00001)
				}
				Direction.SOUTH -> kotlin.test.assertTrue(content.maxY <= label.minY - Look.SCALE)
			}
		}
		presentation.dispose()
	}

	@Test
	fun shouldKeepOppositeCornerFixedDuringTransformedResize() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		vv.displayMode = SubGraphDisplayMode.GRAPH
		for (rotation in io.antarescircuit.jabbah.base.geom.Rotation.entries) {
			for (mirrored in listOf(false, true)) {
				vv.rotation = rotation
				vv.isHorizontallyMirrored = mirrored
				vv.isVerticallyMirrored = mirrored
				vv.setFrame(100.0, 200.0, vv.minimumWidth + 100, vv.minimumHeight + 100)
				val previous = io.antarescircuit.jabbah.base.geom.Rectangle2D(vv.shape)
				val opposite = vv.handleToWorld(previous.bottomRight)
				vv.resizeFrame(previous.x + 10, previous.y + 20, previous.width - 10, previous.height - 20)
				val after = vv.handleToWorld(vv.shape.bottomRight)
				assertEquals(opposite.x, after.x, 0.00001)
				assertEquals(opposite.y, after.y, 0.00001)
				vv.setFrame(previous)
				assertEquals(previous, vv.shape)
			}
		}
	}

	@Test
	fun shouldYieldNonOverwrittenLabel() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl

		vv.label = null

		assertEquals("TEST", vv.label!!.getTranslation())
		assertEquals("TEST", vv.getLabelComponent()!!.text.getTranslation())
	}

	@Test
	fun shouldOverwriteLabel() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl

		vv.label = TranslatableText("CHANGED")

		assertEquals("CHANGED", vv.label!!.getTranslation())
		assertEquals("CHANGED", vv.getLabelComponent()!!.text.getTranslation())
	}

	@Test
	fun shouldStoreOverwrittenLabel() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		vv.label = TranslatableText("CHANGED")

		val clone = StorableCloner.clone(vv)

		assertEquals("CHANGED", clone.label!!.getTranslation())
		assertEquals("CHANGED", clone.getLabelComponent()!!.text.getTranslation())
	}

	@Test
	fun shouldNotStoreUnchangedLabel() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl

		val data = StorableCloner.serialize(vv)

		assertFalse(data.contains("TEST"))
	}

	@Test
	fun shouldResetLabelToDefault() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		vv.label = TranslatableText("")

		val clone = StorableCloner.clone(vv)

		assertEquals("TEST", clone.label!!.getTranslation())
		assertEquals("TEST", clone.getLabelComponent()!!.text.getTranslation())
	}

	@Test
	fun shouldClearLabelWithBlank() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		vv.label = TranslatableText(" ")

		val clone = StorableCloner.clone(vv)

		assertEquals(" ", clone.label!!.getTranslation())
		assertEquals(" ", clone.getLabelComponent()!!.text.getTranslation())
	}

	@Test
	fun shouldUseExecutionLabel() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		vv.model.bind(true, LibraryModule.libraryHolder.library)
		vv.model.executionInitialize(signalHandler)
		vv.model.executionStart(signalHandler)
		vv.executionStarted(signalHandler)

		vv.executionLabel = TranslatableText("EXEC")

		assertEquals("EXEC", vv.getLabelComponent()!!.label.text)
	}

	@Test
	fun shouldResetExecutionLabel() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		vv.model.bind(true, LibraryModule.libraryHolder.library)
		vv.model.executionInitialize(signalHandler)
		vv.model.executionStart(signalHandler)
		vv.executionStarted(signalHandler)
		vv.executionLabel = TranslatableText("EXEC")

		vv.model.executionStopped(signalHandler)
		vv.executionStopped(signalHandler)

		assertEquals("TEST", vv.getLabelComponent()!!.label.text)
	}

	private fun createLibraryElementWithLabel(label: String): ContainerLibraryElement {
		val library = LibraryModule.libraryHolder.library
		val metaGraph = TestLibraryBuilder().addInnerCustomComponent(library, label)
		return library.getContainerLibraryElement(metaGraph.uuid)!!
	}
}
