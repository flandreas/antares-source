package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.net.BidirectionalSplitter
import io.antarescircuit.antares.model.net.BranchCount
import io.antarescircuit.antares.model.signal.Bit.True
import io.antarescircuit.antares.model.signal.Bit.Undefined
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_8
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.GraphView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IncompleteBidirectionalSplitterSimulationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private val a = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "A", bitWidth = BW_8, portType = PortType.INOUT))
	private val splitterView = BidirectionalSplitterView(model = BidirectionalSplitter(BW_8, BranchCount.BC_8))
	private val b = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "B", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))

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