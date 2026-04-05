package io.antarescircuit.antares

import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.net.NetCombiner
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVertice
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Integration tests for [SubGraphVerticeRef] working as [NetCombiner].
 */
class SubGraphVerticeRefNetCombinerTest : AbstractJvmCircuitTest() {

	private lateinit var circuitView: GraphView
	private val library get() = LibraryModule.libraryHolder.library

	private lateinit var subGraphVV: SubGraphVerticeView<out SubGraphVertice>
	private lateinit var a: DigitalCircuitInOutView
	private lateinit var b: DigitalCircuitInOutView

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		a = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "A", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
		b = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "B", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))

		setupLibrary()

		TestLibraryBuilder().addInOutToInOut(library)
		subGraphVV = (library.get(TestLibraryBuilder.INOUT_TO_INOUT) as LibraryElement).getNewInstance<SubGraphVerticeRef>()
			as SubGraphVerticeView<out SubGraphVertice>
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(a)
		builder.addVerticeView(subGraphVV)
		builder.addVerticeView(b)

		builder.connect(a, subGraphVV, subGraphVV.model.getInput("IO1"))
		builder.connect(subGraphVV, subGraphVV.model.getOutput("IO2"), b)

		circuitView = builder.build()
	}

	@Test
	fun shouldForwardAtoB() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		a.model.setIncomingSignal(DigitalSignalFactory.of(true), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(true), b.model.signal)
	}

	@Test
	fun shouldForwardBToA() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		b.model.setIncomingSignal(DigitalSignalFactory.of(true), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(true), a.model.signal)
	}

	@Test
	fun shouldSetErrorOnCombinedNet() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		b.model.setIncomingSignal(DigitalSignalFactory.of(true), scheduler)
		proceedUntilQueueIsEmpty()

		a.model.setIncomingSignal(DigitalSignalFactory.of(false), scheduler)
		proceedUntilQueueIsEmpty()

		assertNotNull(b.model.getPort<DigitalSignal>().net?.executionError)
		assertNotNull(a.model.getPort<DigitalSignal>().net?.executionError)
		assertNotNull(subGraphVV.model.getGraphIfPresent()!!.graphInOuts.first().getOutput<Boolean>().net?.executionError)
	}
}