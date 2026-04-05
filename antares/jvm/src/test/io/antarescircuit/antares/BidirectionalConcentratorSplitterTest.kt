package io.antarescircuit.antares

import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.net.BidirectionalSplitter
import io.antarescircuit.antares.model.net.BranchCount
import io.antarescircuit.antares.model.signal.*
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.net.BidirectionalSplitterView
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.GraphView
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Integration test for combining two [BidirectionalSplitterView] as concentrator and splitter in series.
 */
class BidirectionalConcentratorSplitterTest : AbstractJvmCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var concentratorView: BidirectionalSplitterView
	private lateinit var splitterView: BidirectionalSplitterView
	private lateinit var a0: DigitalCircuitInOutView
	private lateinit var a1: DigitalCircuitInOutView
	private lateinit var b0: DigitalCircuitInOutView
	private lateinit var b1: DigitalCircuitInOutView

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()

		concentratorView = BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2))
		splitterView = BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2))
		a0 = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "A0", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
		a1 = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "A1", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
		b0 = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "B0", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
		b1 = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "B1", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))

		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(a0)
		builder.addVerticeView(a1)
		builder.addVerticeView(concentratorView)
		builder.addVerticeView(splitterView)
		builder.addVerticeView(b0)
		builder.addVerticeView(b1)

		builder.connect(a0, concentratorView, concentratorView.model.getInput(2))
		builder.connect(a1, concentratorView, concentratorView.model.getInput(3))
		builder.connect(concentratorView, concentratorView.model.getOutput(1), splitterView, splitterView.model.getInput(1))
		builder.connect(b0, splitterView, splitterView.model.getInput(2))
		builder.connect(b1, splitterView, splitterView.model.getInput(3))

		circuitView = builder.build()
	}

	@Test
	fun shouldBuildCombinedNets() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(1, a0.model.getOutput<DigitalSignal>().combinedNets.size)
		assertEquals(
			b0.model.getOutput(),
			a0.model.getOutput<DigitalSignal>().combinedNets.iterator().next().accesses
				.first { it.port !== a0.model.getOutput<DigitalSignal>() }
				.port)

		assertEquals(1, a1.model.getOutput<DigitalSignal>().combinedNets.size)
		assertEquals(
			b1.model.getOutput(),
			a1.model.getOutput<DigitalSignal>().combinedNets.iterator().next().accesses
				.first { it.port !== a1.model.getOutput<DigitalSignal>() }
				.port)

		assertEquals(1, b0.model.getOutput<DigitalSignal>().combinedNets.size)
		assertEquals(
			a0.model.getOutput(),
			b0.model.getOutput<DigitalSignal>().combinedNets.iterator().next().accesses
				.first { it.port !== b0.model.getOutput<DigitalSignal>() }
				.port)

		assertEquals(1, b1.model.getOutput<DigitalSignal>().combinedNets.size)
		assertEquals(
			a1.model.getOutput(),
			b1.model.getOutput<DigitalSignal>().combinedNets.iterator().next().accesses
				.first { it.port !== b1.model.getOutput<DigitalSignal>() }
				.port)
	}

	@Test
	fun shouldSendA0toB0() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		a0.model.setIncomingSignal(DigitalSignalFactory.of(true), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(true), b0.model.signal)
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), a1.model.signal)
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), b1.model.signal)
	}

	@Test
	fun shouldSendA1toB1() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		a1.model.setIncomingSignal(DigitalSignalFactory.of(true), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(true), b1.model.signal)
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), a0.model.signal)
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), b0.model.signal)
	}

	@Test
	fun shouldSendB0toA0() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		b0.model.setIncomingSignal(DigitalSignalFactory.of(true), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(true), a0.model.signal)
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), a1.model.signal)
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), b1.model.signal)
	}

	@Test
	fun shouldSendB1toA1() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		b1.model.setIncomingSignal(DigitalSignalFactory.of(true), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(true), a1.model.signal)
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), a0.model.signal)
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), b0.model.signal)
	}
}