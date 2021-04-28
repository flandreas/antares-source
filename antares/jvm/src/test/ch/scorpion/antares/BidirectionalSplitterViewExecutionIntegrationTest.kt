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
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.*

class BidirectionalSplitterViewExecutionIntegrationTest : AbstractJvmCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView
	private val splitterView = BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2))
	private val a = CircuitInOutView(model = CircuitInOutImpl(name = "A", bitWidth = BitWidth.BW_2, portType = PortType.INOUT))
	private val b1 = CircuitInOutView(model = CircuitInOutImpl(name = "B1", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
	private val b2 = CircuitInOutView(model = CircuitInOutImpl(name = "B2", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(a)
		builder.addVerticeView(splitterView)
		builder.addVerticeView(b1)
		builder.addVerticeView(b2)

		builder.connect(a, splitterView, splitterView.model.getInput(1))
		builder.connect(splitterView, splitterView.model.getOutput(2), b1)
		builder.connect(splitterView, splitterView.model.getOutput(3), b2)

		circuitView = builder.build()
	}

	@Test
	fun shouldCombineNetsFromWideSide() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(2, a.model.getOutput<DigitalSignal>().combinedNets.size)
	}

	@Test
	fun shouldCombineNetsFromNarrowSide() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(1, b1.model.getOutput<DigitalSignal>().combinedNets.size)
	}

	@Test
	fun shouldNetsBeUndefinedAtStartup() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(Word.allOf(BitWidth.BW_2, Bit.Undefined), a.model.getOutput<DigitalSignal>().net?.signal)
		assertEquals(Word.of(Bit.Undefined), b1.model.getInput<DigitalSignal>(1).net?.signal)
		assertEquals(Word.of(Bit.Undefined), b2.model.getInput<DigitalSignal>(1).net?.signal)
	}

	@Test
	fun shouldForwardSignalFromWideToNarrow() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		a.model.setIncomingSignal(Word(listOf(Bit.Undefined, Bit.True)), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(Word.of(Bit.Undefined), b1.model.signal)
		assertEquals(Word.of(Bit.True), b2.model.signal)
		assertNull(b1.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(b2.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(a.model.getOutput<DigitalSignal>().net?.executionError)
	}

	@Test
	fun shouldForwardSignalFromNarrowToWide() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		b2.model.setIncomingSignal(Word.of(Bit.True), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(Word(listOf(Bit.Undefined, Bit.True)), a.model.signal)
		assertNull(b2.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(a.model.getOutput<DigitalSignal>().net?.executionError)
	}

	@Test
	fun shouldPropagateConflictErrorBeyondConcentrator() {
		startSimulation()
		proceedUntilQueueIsEmpty()
		a.model.setIncomingSignal(Word.of(BitWidth.BW_2, 2), scheduler)
		proceedUntilQueueIsEmpty()

		b2.model.setIncomingSignal(Word.of(Bit.False), scheduler)
		proceedUntilQueueIsEmpty()

		assertNull(b1.model.getOutput<DigitalSignal>().net?.executionError)
		assertNotNull(b2.model.getOutput<DigitalSignal>().net?.executionError)
		assertNotNull(a.model.getOutput<DigitalSignal>().net?.executionError)
	}

	@Test
	fun shouldPropagateConflictErrorBeyondSplitter() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		b1.model.setIncomingSignal(Word.of(Bit.True), scheduler)
		proceedUntilQueueIsEmpty()

		a.model.setIncomingSignal(Word(listOf(Bit.False, Bit.Undefined)), scheduler)
		proceedUntilQueueIsEmpty()

		assertNotNull(b1.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(b2.model.getOutput<DigitalSignal>().net?.executionError)
		assertNotNull(a.model.getOutput<DigitalSignal>().net?.executionError)
	}

	@Test
	fun shouldBeBidirectionalAtTheSameTime() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		// Send 1 on channel 0 through Splitter
		a.model.setIncomingSignal(Word(listOf(Bit.True, Bit.Undefined)), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(Word.of(true), splitterView.model.getOutput<DigitalSignal>(2).getOutgoingSignal())
		assertEquals(Word.of(Bit.Undefined), splitterView.model.getOutput<DigitalSignal>(3).getOutgoingSignal())
		// Must have been synchronized
		assertEquals(Word.of(true), splitterView.model.getInput<DigitalSignal>(2).getIncomingSignal())

		// Send 0 on channel 1 through Concentrator
		b2.model.setIncomingSignal(Word.of(Bit.False), scheduler)
		proceedUntilQueueIsEmpty()

		assertNull(b1.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(b2.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(a.model.getOutput<DigitalSignal>().net?.executionError)
	}
}