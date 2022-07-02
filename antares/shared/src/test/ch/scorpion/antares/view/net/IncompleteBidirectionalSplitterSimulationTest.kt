package ch.scorpion.antares.view.net

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.net.BidirectionalSplitter
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.signal.Bit.True
import ch.scorpion.antares.model.signal.Bit.Undefined
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_8
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IncompleteBidirectionalSplitterSimulationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private val splitterView = BidirectionalSplitterView(model = BidirectionalSplitter(BW_8, BranchCount.BC_8))
	private val a = CircuitInOutView(model = CircuitInOutImpl(name = "A", bitWidth = BW_8, portType = PortType.INOUT))
	private val b = CircuitInOutView(model = CircuitInOutImpl(name = "B", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(a)
		builder.addVerticeView(splitterView)
		builder.addVerticeView(b)

		builder.connect(a, splitterView, splitterView.model.getInput(1))
		builder.connect(splitterView, splitterView.model.getOutput(3), b)

		circuitView = builder.build()
	}

	/** Regression test of bug #412. */
	@Test
	fun shouldNotStoreBitsAtUnconnectedPorts() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		// Send 1 through concentration
		b.model.toggleBit(0, false, scheduler)
		proceedUntilQueueIsEmpty()
		assertEquals(DigitalSignalFactory.allOf(BW_8, Undefined).withBit(1, True), a.model.signal)

		// Undo by sending Z through concentrator
		b.model.toggleBit(0, true, scheduler)
		proceedUntilQueueIsEmpty()
		assertEquals(DigitalSignalFactory.allOf(BW_8, Undefined), a.model.signal)

		// Send a 1 at ANOTHER bit index through the splitter
		a.model.toggleBit(6, false, scheduler)
		proceedUntilQueueIsEmpty()
		assertEquals(DigitalSignalFactory.of(Undefined), b.model.signal)

		// Undo by sending Z through splitter
		a.model.toggleBit(6, true, scheduler)
		proceedUntilQueueIsEmpty()
		assertEquals(DigitalSignalFactory.of(Undefined), b.model.signal)

		// Send again the same 1 from step 1 through concentration
		// Bug #412: Signal bit 1 reappeared at position 6 of A
		b.model.toggleBit(0, false, scheduler)
		proceedUntilQueueIsEmpty()
		assertEquals(DigitalSignalFactory.allOf(BW_8, Undefined).withBit(1, True), a.model.signal)
	}
}