package ch.scorpion.antares

import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.net.BidirectionalSplitter
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.net.BidirectionalSplitterView
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import io.mockk.mockk
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
	private val actorListener = mockk<ActorListener>(relaxed = true)
	private val bidiSplitterView = BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2))
	private val bidiConcentratorView = BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2))
	private val in0 = CircuitInOutView(model = CircuitInOutImpl(name = "I0", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
	private val in1 = CircuitInOutView(model = CircuitInOutImpl(name = "I1", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(in0)
		builder.addVerticeView(in1)
		builder.addVerticeView(bidiSplitterView)
		builder.addVerticeView(bidiConcentratorView)

		builder.connect(in0, bidiSplitterView, bidiSplitterView.model.getInput(2))
		builder.connect(in1, bidiSplitterView, bidiSplitterView.model.getInput(3))
		builder.connect(bidiSplitterView, bidiSplitterView.model.getOutput(1), bidiConcentratorView, bidiConcentratorView.model.getInput(1))

		circuitView = builder.build()
	}

	@Test
	fun shouldNotLoopEndlessly() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		in0.model.setIncomingSignal(Word.of(true), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		in1.model.setIncomingSignal(Word.of(true), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(Word.of(true), bidiConcentratorView.model.getOutput<DigitalSignal>(2).getOutgoingSignal())
		assertEquals(Word.of(true), bidiConcentratorView.model.getOutput<DigitalSignal>(3).getOutgoingSignal())
	}
}