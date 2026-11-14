package io.antarescircuit.antares.view.inout

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.TestLibraryBuilder
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation
import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.library.LibraryImpl
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.LibraryPersistenceService
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.StoringGraphActorData
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.view.port.AbstractPortView
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphDisplayMode
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import io.antarescircuit.jabbah.graph.library.LibraryHolder
import io.antarescircuit.jabbah.graph.library.LibraryHolderImpl
import io.antarescircuit.jabbah.graph.library.LibraryService
import io.antarescircuit.jabbah.base.event.EventBusImpl
import kotlin.test.assertEquals

class CircuitInOutViewTest {

	private lateinit var savedPersistence: LibraryPersistenceService
	private lateinit var savedLibraryHolder: LibraryHolder
	private lateinit var savedLibraryService: LibraryService

	@BeforeTest
	fun rememberLibrary() {
		savedPersistence = LibraryModule.userLibraryPersistenceService
		savedLibraryHolder = LibraryModule.libraryHolder
		savedLibraryService = LibraryModule.libraryService
		val eventBus = EventBusImpl()
		LibraryModule.libraryHolder = LibraryHolderImpl(eventBus = eventBus)
		LibraryModule.libraryService = LibraryService(eventBus = eventBus)
	}

	@AfterTest
	fun restoreLibrary() {
		LibraryModule.userLibraryPersistenceService = savedPersistence
		LibraryModule.libraryHolder.l?.dispose()
		LibraryModule.libraryHolder = savedLibraryHolder
		LibraryModule.libraryService = savedLibraryService
	}

	private val signalHandler = mock<SignalHandler>(MockMode.autofill)

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldPlaceEmbeddedCircuitInputsAndOutputsOnTheirExternalSides() {
		val persistence = mock<LibraryPersistenceService>(MockMode.autofill)
		LibraryModule.userLibraryPersistenceService = persistence
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
		val library = LibraryModule.libraryHolder.library
		for (orientation in Direction.entries) {
			val builder = TestCircuitBuilder("HalfAdderPorts${orientation.name}")
			val a = builder.addInput("A").also { it.location = Point2D(0, 0); it.orientation = orientation }
			val b = builder.addInput("B").also { it.location = Point2D(40, 60); it.orientation = orientation }
			val sum = builder.addOutput("S").also { it.location = Point2D(200, 0); it.orientation = orientation }
			val carry = builder.addOutput("C").also { it.location = Point2D(240, 60); it.orientation = orientation }
			val meta = TestLibraryBuilder().addGraphView(builder.graphView, library)
			every { persistence.loadMetaGraph(any(), meta.uuid) } returns meta
			val vertex = library.getContainerLibraryElement(meta.uuid)!!.getNewInstance<Vertice>() as SubGraphVerticeViewImpl
			vertex.displayMode = SubGraphDisplayMode.GRAPH
			kotlin.test.assertTrue(vertex.minimumWidth > 0)
			for (inner in listOf(a, b, sum, carry)) {
				val outer = vertex.getPortViews().first { it.port.name == inner.model.name }
				val expected = if (inner.portType == PortType.INPUT) orientation.opposite() else orientation
				assertEquals(expected, outer.direction)
				when (expected) {
					Direction.WEST -> assertEquals(0.0, outer.location.x)
					Direction.EAST -> assertEquals(vertex.shape.width, outer.location.x)
					Direction.NORTH -> assertEquals(0.0, outer.location.y)
					Direction.SOUTH -> assertEquals(vertex.shape.height, outer.location.y)
				}
			}
			val ports = vertex.getPortViews().associateBy { it.port.name }
			val inputs = listOf(ports.getValue("A"), ports.getValue("B"))
			val outputs = listOf(ports.getValue("S"), ports.getValue("C"))
			if (orientation.dx != 0) {
				assertEquals(60.0, inputs[1].location.y - inputs[0].location.y)
				assertEquals(60.0, outputs[1].location.y - outputs[0].location.y)
			} else {
				assertEquals(40.0, inputs[1].location.x - inputs[0].location.x)
				assertEquals(40.0, outputs[1].location.x - outputs[0].location.x)
			}
			vertex.dispose()
		}
	}

	@Test
	fun shouldReserveSpaceForLongInsidePortNamesInEveryOrientation() {
		val persistence = mock<LibraryPersistenceService>(MockMode.autofill)
		LibraryModule.userLibraryPersistenceService = persistence
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
		val library = LibraryModule.libraryHolder.library
		for (orientation in Direction.entries) {
			val builder = TestCircuitBuilder("LongPortLabels${orientation.name}")
			val input = builder.addInput("A very long input port name").also { it.orientation = orientation }
			val output = builder.addOutput("A very long output port name").also {
				it.orientation = orientation
				it.location = Point2D(200, 100)
			}
			val meta = TestLibraryBuilder().addGraphView(builder.graphView, library)
			every { persistence.loadMetaGraph(any(), meta.uuid) } returns meta
			val vertex = library.getContainerLibraryElement(meta.uuid)!!.getNewInstance<Vertice>() as SubGraphVerticeViewImpl
			// Start with hidden labels, then enable them on an already displayed graph.
			val ports = vertex.getPortViews().map { it as AbstractPortView<*> }
			ports.forEach { it.portLabelPosition = PortLabelPosition.HIDE }
			vertex.displayMode = SubGraphDisplayMode.GRAPH
			val before = Rectangle2D(vertex.shape)
			ports.forEach { it.portLabelPosition = PortLabelPosition.INTERNAL }
			// Accessing the geometry refreshes the inset and grows the frame when necessary.
			vertex.boundingBox
			val innerView = vertex.createSubGraphView(null)
			val content = innerView.boundingBox
			val base = 2 * Look.SCALE.toDouble()
			fun depth(port: AbstractPortView<*>): Double {
				val box = port.boundingBox
				return maxOf(base, Look.SCALE + when (port.direction) {
					Direction.WEST -> box.maxX - port.location.x
					Direction.EAST -> port.location.x - box.minX
					Direction.NORTH -> box.maxY - port.location.y
					Direction.SOUTH -> port.location.y - box.minY
				})
			}
			// Label space stays unscaled; the circuit may now shrink instead of growing the frame.
			kotlin.test.assertTrue(vertex.shape.width >= before.width)
			kotlin.test.assertTrue(vertex.shape.height >= before.height)
			val inputPort = ports.first { it.port.name == input.model.name }
			val outputPort = ports.first { it.port.name == output.model.name }
			val scale = if (orientation.dx != 0) (outputPort.location.y - inputPort.location.y) / 100.0
				else (outputPort.location.x - inputPort.location.x) / 200.0
			kotlin.test.assertTrue(scale > 0.0 && scale <= 1.00001)
			if (orientation.dx != 0) {
				kotlin.test.assertTrue(vertex.shape.width >= content.width * scale + ports.sumOf { depth(it) } - 0.001)
			} else {
				kotlin.test.assertTrue(vertex.shape.height >= content.height * scale + ports.sumOf { depth(it) } + base - 0.001)
			}
			innerView.dispose()
			vertex.dispose()
		}
	}

	@Test
	fun shouldNotChangeLocationWhenChangingBitWidthWhileConnected() {
		val builder = TestCircuitBuilder("test")
		val input1 = builder.addInput()
		val input2 = builder.addInput()
		input1.location = Point2D.ZERO
		input2.location = Point2D(0, 100)
		builder.connectOutputOpen(input1, Point2D(100, 0))

		input1.bitWidth = BitWidth.BW_2
		input2.bitWidth = BitWidth.BW_2

		assertEquals(Point2D.ZERO, input1.location)
		assertEquals(input2.boundingBox.minX, input1.boundingBox.minX)
	}

	@Test
	fun shouldNotConsumeUndefinedDecimalDigit() {
		val input = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(bitWidth = BitWidth.BW_8))
		input.signalRepresentation = DigitalSignalRepresentation.DECIMAL
		input.focusGained()
		input.consumeKey(KeyEvent.VK_Z, GraphApplicationContextHolder(mock(MockMode.autofill)), skipAnimation = true)
	}

	@Test
	fun shouldClearByUser() {
		val input = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(bitWidth = BitWidth.BW_8))
		input.signalRepresentation = DigitalSignalRepresentation.DECIMAL
		input.focusGained()
		input.model.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 42), signalHandler)

		input.clearByUser(signalHandler)
		input.model.act(signalHandler, StoringGraphActorData(null, DigitalSignalFactory.of(BitWidth.BW_8, 0)))

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 0), input.model.signal)
	}
}
