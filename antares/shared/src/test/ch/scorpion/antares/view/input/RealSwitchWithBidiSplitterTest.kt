package ch.scorpion.antares.view.input

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.net.BidirectionalSplitter
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.net.BidirectionalSplitterView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class RealSwitchWithBidiSplitterTest : AbstractCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView
	private lateinit var circuitInOutView: CircuitInOutView
	private lateinit var bidiSplitterView: BidirectionalSplitterView
	private lateinit var realSwitchView: RealSwitchView
	private lateinit var ledView: LEDView

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		circuitInOutView = builder.addVerticeView(CircuitInOutView(model = CircuitInOutImpl(bitWidth = BitWidth.BW_2)))
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
	fun shouldHaveCombinedNetTowardsCircuitInOut() {
		startSimulation()
		assertTrue(realSwitchView.model.getOutput<DigitalSignal>(1).combinedNets.any { it.accesses.any { it.port === circuitInOutView.model.getOutput<DigitalSignal>() } })
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