package ch.scorpion.antares

import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.net.BidirectionalSplitter
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.net.BidirectionalSplitterView
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.view.GraphView
import io.mockk.mockk
import kotlin.test.*

class BidirectionalSplitterViewExecutionIntegrationTest : AbstractJvmCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView
	private val actorListener = mockk<ActorListener>(relaxed = true)
	private val bidiSplitterView = BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2))
	private val circuitInOutViewWide = CircuitInOutView(model = CircuitInOutImpl(name = "A", bitWidth = BitWidth.BW_2, portType = PortType.INOUT))
	private val circuitInOutViewNarrow1 = CircuitInOutView(model = CircuitInOutImpl(name = "B1", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
	private val circuitInOutViewNarrow2 = CircuitInOutView(model = CircuitInOutImpl(name = "B2", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(circuitInOutViewWide)
		builder.addVerticeView(bidiSplitterView)
		builder.addVerticeView(circuitInOutViewNarrow1)
		builder.addVerticeView(circuitInOutViewNarrow2)

		builder.connect(circuitInOutViewWide, bidiSplitterView, bidiSplitterView.model.getInput(1))
		builder.connect(bidiSplitterView, bidiSplitterView.model.getOutput(2), circuitInOutViewNarrow1)
		builder.connect(bidiSplitterView, bidiSplitterView.model.getOutput(3), circuitInOutViewNarrow2)

		circuitView = builder.build()
	}

	@Test
	fun shouldCombineNetsFromWideSide() {
		val combinedNet = CombinedNet.fromOutputPort(circuitInOutViewWide.model.getOutput<DigitalSignal>())

		assertEquals(3, combinedNet.outputPorts.size)
	}

	@Test
	fun shouldCombineNetsFromNarrowSide() {
		val combinedNet = CombinedNet.fromOutputPort(circuitInOutViewNarrow1.model.getOutput<DigitalSignal>())

		assertEquals(2, combinedNet.outputPorts.size)
	}

	@Test
	fun shouldNetsBeUndefinedAtStartup() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(Word.allOf(BitWidth.BW_2, Bit.Undefined), circuitInOutViewWide.model.getOutput<DigitalSignal>().net?.signal)
		assertEquals(Word.of(Bit.Undefined), circuitInOutViewNarrow1.model.getInput<DigitalSignal>(1).net?.signal)
		assertEquals(Word.of(Bit.Undefined), circuitInOutViewNarrow2.model.getInput<DigitalSignal>(1).net?.signal)
	}

	@Test
	fun shouldForwardSignalFromWideToNarrow() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		circuitInOutViewWide.model.setIncomingSignal(Word(listOf(Bit.Undefined, Bit.True)), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(Word.of(Bit.Undefined), circuitInOutViewNarrow1.model.signal)
		assertEquals(Word.of(Bit.True), circuitInOutViewNarrow2.model.signal)
		assertNull(circuitInOutViewNarrow1.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(circuitInOutViewNarrow2.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(circuitInOutViewWide.model.getOutput<DigitalSignal>().net?.executionError)
	}

	@Test
	fun shouldForwardSignalFromNarrowToWide() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		circuitInOutViewNarrow2.model.setIncomingSignal(Word.of(Bit.True), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(Word(listOf(Bit.Undefined, Bit.True)), circuitInOutViewWide.model.signal)
		assertNull(circuitInOutViewNarrow2.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(circuitInOutViewWide.model.getOutput<DigitalSignal>().net?.executionError)
	}

	@Test
	fun combinedNetsShouldBeConsistent() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		circuitInOutViewNarrow1.model.setIncomingSignal(Word.of(true), scheduler)
		circuitInOutViewNarrow2.model.setIncomingSignal(Word.of(false), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		// This is useless, as the signal is already present on the Net and therefore doesn't get propagated
		// to the narrow CircuitInOutViews. Is that generally the case for consistent Nets?
		circuitInOutViewWide.model.setIncomingSignal(Word(listOf(Bit.True, Bit.False)), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertNull(circuitInOutViewWide.model.getPort<DigitalSignal>().net!!.executionError)
		assertNull(circuitInOutViewNarrow1.model.getPort<DigitalSignal>().net!!.executionError)
		assertNull(circuitInOutViewNarrow2.model.getPort<DigitalSignal>().net!!.executionError)
	}

	@Test
	fun shouldPropagateConflictErrorBeyondConcentrator() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)
		circuitInOutViewWide.model.setIncomingSignal(Word.of(BitWidth.BW_2, 2), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		circuitInOutViewNarrow2.model.setIncomingSignal(Word.of(Bit.False), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertNotNull(circuitInOutViewNarrow2.model.getOutput<DigitalSignal>().net?.executionError)
		assertNotNull(circuitInOutViewWide.model.getOutput<DigitalSignal>().net?.executionError)
	}

	@Test
	fun shouldPropagateConflictErrorBeyondSplitter() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)
		circuitInOutViewNarrow1.model.setIncomingSignal(Word.of(Bit.True), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		circuitInOutViewWide.model.setIncomingSignal(Word(listOf(Bit.False, Bit.Undefined)), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertNotNull(circuitInOutViewNarrow1.model.getOutput<DigitalSignal>().net?.executionError)
		assertNotNull(circuitInOutViewWide.model.getOutput<DigitalSignal>().net?.executionError)
	}

	@Test
	fun shouldBeBidirectionalAtTheSameTime() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		// Send 1 on channel 0 through Splitter
		circuitInOutViewWide.model.setIncomingSignal(Word(listOf(Bit.True, Bit.Undefined)), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)
		assertEquals(Word.of(true), bidiSplitterView.model.getOutput<DigitalSignal>(2).getOutgoingSignal())
		assertEquals(Word.of(Bit.Undefined), bidiSplitterView.model.getOutput<DigitalSignal>(3).getOutgoingSignal())
		// Must have been synchronized
		assertEquals(Word.of(true), bidiSplitterView.model.getInput<DigitalSignal>(2).getIncomingSignal())

		// Send 0 on channel 1 through Concentrator
		circuitInOutViewNarrow2.model.setIncomingSignal(Word.of(Bit.False), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertNull(circuitInOutViewNarrow1.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(circuitInOutViewNarrow2.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(circuitInOutViewWide.model.getOutput<DigitalSignal>().net?.executionError)
	}
}