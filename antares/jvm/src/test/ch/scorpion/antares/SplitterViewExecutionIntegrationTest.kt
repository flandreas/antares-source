package ch.scorpion.antares

import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.net.Splitter
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.net.SplitterView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.graph.view.GraphView
import io.mockk.mockk
import kotlin.test.*

class SplitterViewExecutionIntegrationTest : AbstractJvmCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView
	private val actorListener = mockk<ActorListener>(relaxed = true)

	private val circuitInputView = CircuitInOutView(model = CircuitInOutImpl(bitWidth = BitWidth.BW_2))
	private val splitterView = SplitterView(model = Splitter(BitWidth.BW_2, BranchCount.BC_2))
	private val ledView1 = LEDView()
	private val ledView2 = LEDView()

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(circuitInputView)
		builder.addVerticeView(splitterView)
		builder.addVerticeView(ledView1)
		builder.addVerticeView(ledView2)

		builder.connect(circuitInputView, splitterView)
		builder.connect(splitterView, splitterView.model.getOutput(2), ledView1)
		builder.connect(splitterView, splitterView.model.getOutput(3), ledView2)

		circuitView = builder.build()
	}

	@Test
	fun shouldSetOutputEdgeViewsToDefaultSignal() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(Word.of(Bit.False), splitterView.model.getOutput<DigitalSignal>(2).net!!.signal)
		assertEquals(Word.of(Bit.False), splitterView.model.getOutput<DigitalSignal>(3).net!!.signal)
	}

	@Test
	fun shouldSplit() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)
		assertFalse(ledView1.model.isOn)
		assertFalse(ledView2.model.isOn)

		circuitInputView.model.setIncomingSignal(Word.of(BitWidth.BW_2, 2L), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertFalse(ledView1.model.isOn)
		assertTrue(ledView2.model.isOn)
	}
}