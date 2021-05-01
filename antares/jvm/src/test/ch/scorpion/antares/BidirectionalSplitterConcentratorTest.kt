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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Integration test for combining two [BidirectionalSplitterView] as splitter and concentrator in series.
 */
class BidirectionalSplitterConcentratorTest : AbstractJvmCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView
	private val splitterView = BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2))
	private val concentratorView = BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2))
	private val a = CircuitInOutView(model = CircuitInOutImpl(name = "A", bitWidth = BitWidth.BW_2, portType = PortType.INOUT))
	private val b = CircuitInOutView(model = CircuitInOutImpl(name = "B", bitWidth = BitWidth.BW_2, portType = PortType.INOUT))

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
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

		a.model.setIncomingSignal(Word(listOf(Bit.False, Bit.True)), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals((Word(listOf(Bit.False, Bit.True))), b.model.signal)
	}

	@Test
	fun shouldSendBtoA() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		b.model.setIncomingSignal(Word(listOf(Bit.False, Bit.True)), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals((Word(listOf(Bit.False, Bit.True))), a.model.signal)
	}
}