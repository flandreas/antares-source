package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.net.BidirectionalSplitter
import io.antarescircuit.antares.model.net.BranchCount
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.net.BidirectionalSplitterView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.graph.view.GraphView
import kotlin.test.Test
import kotlin.test.assertTrue

class RealSwitchWithBidiSplitterTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var circuitInOutView: DigitalCircuitInOutView
	private lateinit var bidiSplitterView: BidirectionalSplitterView
	private lateinit var realSwitchView: RealSwitchView
	private lateinit var ledView: LEDView

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		circuitInOutView = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(bitWidth = BitWidth.BW_2)))
		bidiSplitterView = builder.addVerticeView(BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, branchCount = BranchCount.BC_2)))
		realSwitchView = builder.addVerticeView(RealSwitchView())
		ledView = builder.addVerticeView(LEDView())
		builder.connect(circuitInOutView, bidiSplitterView, toPort = bidiSplitterView.model.wideSidePort)
		builder.connect(bidiSplitterView, fromPort = bidiSplitterView.model.getOutput(2), to = realSwitchView, toPort = realSwitchView.model.getInput(1))
		builder.connect(realSwitchView, realSwitchView.model.getOutput(2), ledView)
		circuitView = builder.build()
	}

	@Test
	fun shouldRegisterAsNetTopologyChanger() {
		startSimulation()
		assertTrue(circuitInOutView.model.getOutput<DigitalSignal>().combinedNets.any { it.netTopologyChanger.contains(realSwitchView.model) })
	}

	@Test
	fun shouldForwardSignalOnStateChange() {
		startSimulation()
		proceedUntilQueueIsEmpty()
		circuitInOutView.model.toggleBit(0, false, scheduler)
		proceedUntilQueueIsEmpty()

		realSwitchView.model.on(scheduler)
		proceedUntilQueueIsEmpty()

		assertTrue(circuitInOutView.model.getOutput<DigitalSignal>().combinedNets.any { it.netTopologyChanger.contains(realSwitchView.model) })
		assertTrue(ledView.model.isOn)
	}
}