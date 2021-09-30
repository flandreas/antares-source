package ch.scorpion.antares

import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * Integration tests for [SubGraphVerticeRef] working as [NetCombiner].
 */
class SubGraphVerticeRefNetCombinerTest : AbstractJvmCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView
	private val library get() = LibraryModule.libraryHolder.library

	private lateinit var subGraphVV: SubGraphVerticeView<out SubGraphVertice>
	private val a = CircuitInOutView(model = CircuitInOutImpl(name = "A", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
	private val b = CircuitInOutView(model = CircuitInOutImpl(name = "B", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
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
}