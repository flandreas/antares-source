package ch.scorpion.antares

import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.net.BidirectionalSplitter
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.inout.CircuitInOutView
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
	private val a0 = CircuitInOutView(model = CircuitInOutImpl(name = "A0", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
	private val a1 = CircuitInOutView(model = CircuitInOutImpl(name = "A1", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
	private val b0 = CircuitInOutView(model = CircuitInOutImpl(name = "B0", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
	private val b1 = CircuitInOutView(model = CircuitInOutImpl(name = "B1", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))

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
	fun shouldSendA0toB0() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		a0.model.setIncomingSignal(Word.of(true), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(Word.of(true), b0.model.signal)
		assertEquals(Word.of(Bit.Undefined), a1.model.signal)
		assertEquals(Word.of(Bit.Undefined), b1.model.signal)
	}

	@Test
	fun shouldSendA1toB1() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		a1.model.setIncomingSignal(Word.of(true), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(Word.of(true), b1.model.signal)
		assertEquals(Word.of(Bit.Undefined), a0.model.signal)
		assertEquals(Word.of(Bit.Undefined), b0.model.signal)
	}

	@Test
	fun shouldSendB0toA0() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		b0.model.setIncomingSignal(Word.of(true), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(Word.of(true), a0.model.signal)
		assertEquals(Word.of(Bit.Undefined), a1.model.signal)
		assertEquals(Word.of(Bit.Undefined), b1.model.signal)
	}

	@Test
	fun shouldSendB1toA1() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		b1.model.setIncomingSignal(Word.of(true), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(Word.of(true), a1.model.signal)
		assertEquals(Word.of(Bit.Undefined), a0.model.signal)
		assertEquals(Word.of(Bit.Undefined), b0.model.signal)
	}
}