package ch.scorpion.antares

import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.net.BidirectionalSplitter
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.antares.view.net.BidirectionalSplitterView
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Integration test for combining two [BidirectionalSplitterView] as splitter and concentrator in series.
 */
class BidirectionalSplitterConcentratorTest : AbstractJvmCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var splitterView: BidirectionalSplitterView
	private lateinit var concentratorView: BidirectionalSplitterView
	private lateinit var a: DigitalCircuitInOutView
	private lateinit var b: DigitalCircuitInOutView

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		splitterView = BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2))
		concentratorView = BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2))
		a = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "A", bitWidth = BitWidth.BW_2, portType = PortType.INOUT))
		b = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "B", bitWidth = BitWidth.BW_2, portType = PortType.INOUT))

		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(a)
		builder.addVerticeView(splitterView)
		builder.addVerticeView(concentratorView)
		builder.addVerticeView(b)

		builder.connect(a, splitterView, splitterView.model.getInput(1))
		builder.connect(splitterView, splitterView.model.getOutput(2), concentratorView, concentratorView.model.getInput(2))
		builder.connect(splitterView, splitterView.model.getOutput(3), concentratorView, concentratorView.model.getInput(3))
		builder.connect(concentratorView, concentratorView.model.getOutput(1), b)

		circuitView = builder.build()
	}

	@Test
	fun shouldBuildCombinedNets() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(2, a.model.getOutput<DigitalSignal>().combinedNets.size)
		assertEquals(2, b.model.getOutput<DigitalSignal>().combinedNets.size)
	}

	@Test
	fun shouldSendAtoB() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		a.model.setIncomingSignal(DigitalSignalFactory.ofBits(listOf(Bit.False, Bit.True)), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals((DigitalSignalFactory.ofBits(listOf(Bit.False, Bit.True))), b.model.signal)
	}

	@Test
	fun shouldSendBtoA() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		b.model.setIncomingSignal(DigitalSignalFactory.ofBits(listOf(Bit.False, Bit.True)), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals((DigitalSignalFactory.ofBits(listOf(Bit.False, Bit.True))), a.model.signal)
	}
}