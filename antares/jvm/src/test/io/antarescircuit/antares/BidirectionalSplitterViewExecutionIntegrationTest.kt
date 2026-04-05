package io.antarescircuit.antares

import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.net.BidirectionalSplitter
import io.antarescircuit.antares.model.net.BranchCount
import io.antarescircuit.antares.model.signal.*
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.net.BidirectionalSplitterView
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.GraphView
import kotlin.test.*

class BidirectionalSplitterViewExecutionIntegrationTest : AbstractJvmCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var splitterView: BidirectionalSplitterView
	private lateinit var a: DigitalCircuitInOutView
	private lateinit var b1: DigitalCircuitInOutView
	private lateinit var b2: DigitalCircuitInOutView

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		splitterView = BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2))
		a = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "A", bitWidth = BitWidth.BW_2, portType = PortType.INOUT))
		b1 = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "B1", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
		b2 = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "B2", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))

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

		assertEquals(DigitalSignalFactory.allOf(BitWidth.BW_2, Bit.Undefined), a.model.getOutput<DigitalSignal>().net?.signal)
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), b1.model.getInput<DigitalSignal>(1).net?.signal)
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), b2.model.getInput<DigitalSignal>(1).net?.signal)
	}

	@Test
	fun shouldForwardSignalFromWideToNarrow() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		a.model.setIncomingSignal(DigitalSignalFactory.ofBits(listOf(Bit.Undefined, Bit.True)), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(Bit.Undefined), b1.model.signal)
		assertEquals(DigitalSignalFactory.of(Bit.True), b2.model.signal)
		assertNull(b1.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(b2.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(a.model.getOutput<DigitalSignal>().net?.executionError)
	}

	@Test
	fun shouldForwardSignalFromNarrowToWide() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		b2.model.setIncomingSignal(DigitalSignalFactory.of(Bit.True), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.ofBits(listOf(Bit.Undefined, Bit.True)), a.model.signal)
		assertNull(b2.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(a.model.getOutput<DigitalSignal>().net?.executionError)
	}

	@Test
	fun shouldPropagateConflictErrorBeyondConcentrator() {
		startSimulation()
		proceedUntilQueueIsEmpty()
		a.model.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_2, 2), scheduler)
		proceedUntilQueueIsEmpty()

		b2.model.setIncomingSignal(DigitalSignalFactory.of(Bit.False), scheduler)
		proceedUntilQueueIsEmpty()

		assertNull(b1.model.getOutput<DigitalSignal>().net?.executionError)
		assertNotNull(b2.model.getOutput<DigitalSignal>().net?.executionError)
		assertNotNull(a.model.getOutput<DigitalSignal>().net?.executionError)
	}

	@Test
	fun shouldPropagateConflictErrorBeyondSplitter() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		b1.model.setIncomingSignal(DigitalSignalFactory.of(Bit.True), scheduler)
		proceedUntilQueueIsEmpty()

		a.model.setIncomingSignal(DigitalSignalFactory.ofBits(listOf(Bit.False, Bit.Undefined)), scheduler)
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
		a.model.setIncomingSignal(DigitalSignalFactory.ofBits(listOf(Bit.True, Bit.Undefined)), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(true), splitterView.model.getOutput<DigitalSignal>(2).getOutgoingSignal())
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), splitterView.model.getOutput<DigitalSignal>(3).getOutgoingSignal())
		// Must have been synchronized
		assertEquals(DigitalSignalFactory.of(true), splitterView.model.getInput<DigitalSignal>(2).getIncomingSignal())

		// Send 0 on channel 1 through Concentrator
		b2.model.setIncomingSignal(DigitalSignalFactory.of(Bit.False), scheduler)
		proceedUntilQueueIsEmpty()

		assertNull(b1.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(b2.model.getOutput<DigitalSignal>().net?.executionError)
		assertNull(a.model.getOutput<DigitalSignal>().net?.executionError)
	}
}