package ch.scorpion.antares

import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull


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
	private val actorListener = mockk<ActorListener>(relaxed = true)
	private val library get() = LibraryModule.libraryHolder.library

	private lateinit var subGraphVV: SubGraphVerticeView<out SubGraphVertice>
	private val io1 = CircuitInOutView(model = CircuitInOutImpl(name = "IO1", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
	private val io2 = CircuitInOutView(model = CircuitInOutImpl(name = "IO2", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		setupLibrary()

		TestLibraryBuilder().addInOutToOut(library)
		subGraphVV = (library.get(TestLibraryBuilder.INOUT_TO_OUT) as LibraryElement).getNewInstance<SubGraphVerticeRef>()
			as SubGraphVerticeView<out SubGraphVertice>
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(io1)
		builder.addVerticeView(subGraphVV)
		builder.addVerticeView(io2)

		builder.connect(io1, subGraphVV, subGraphVV.model.getInput("IO"))
		builder.connect(subGraphVV, subGraphVV.model.getOutput("O"), io2)

		circuitView = builder.build()
	}

	@Test
	fun shouldSetErrorOnCombinedNet() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		io2.model.setIncomingSignal(Word.of(true), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		io1.model.setIncomingSignal(Word.of(false), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertNotNull(io2.model.getPort<DigitalSignal>().net?.executionError)
		assertNotNull(io1.model.getPort<DigitalSignal>().net?.executionError)
		assertNotNull(subGraphVV.model.getGraphIfPresent()!!.graphInOuts.first().getOutput<Boolean>().net?.executionError)
	}
}