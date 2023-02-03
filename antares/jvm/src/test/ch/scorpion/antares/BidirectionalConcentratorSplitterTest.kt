package ch.scorpion.antares

import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.net.BidirectionalSplitter
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.antares.view.net.BidirectionalSplitterView
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Integration test for combining two [BidirectionalSplitterView] as concentrator and splitter in series.
 */
class BidirectionalConcentratorSplitterTest : AbstractJvmCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView
	private val concentratorView = BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2))
	private val splitterView = BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2))
	private val a0 = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "A0", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
	private val a1 = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "A1", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
	private val b0 = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "B0", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
	private val b1 = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "B1", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
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