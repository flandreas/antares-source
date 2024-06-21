package ch.scorpion.antares

import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.net.BidirectionalSplitter
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.antares.view.net.BidirectionalSplitterView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Integration tests for [BidirectionalSplitterView] used with [VerticeView]s that use defined signals
 */
class BidirectionalSplitterViewDefinedTest : AbstractJvmCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView
	private val actorListener = mock<ActorListener>(MockMode.autofill)
	private val bidiSplitterView = BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2))
	private val circuitInputView = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "A", bitWidth = BitWidth.BW_2, portType = PortType.INPUT))
	private val ledView1 = LEDView()
	private val ledView2 = LEDView()

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(circuitInputView)
		builder.addVerticeView(bidiSplitterView)
		builder.addVerticeView(ledView1)
		builder.addVerticeView(ledView2)

		builder.connect(circuitInputView, bidiSplitterView, toPort = bidiSplitterView.model.wideSidePort)
		builder.connect(bidiSplitterView, fromPort = bidiSplitterView.model.getOutput(2), ledView1)
		builder.connect(bidiSplitterView, fromPort = bidiSplitterView.model.getOutput(3), ledView2)

		circuitView = builder.build()
	}

	@Test
	fun shouldPropagateDefinedSignalAtStartup() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(DigitalSignalFactory.ofBits(listOf(Bit.False, Bit.False)), circuitInputView.model.signal)
		assertEquals(DigitalSignalFactory.ofBits(listOf(Bit.False, Bit.False)), circuitInputView.model.getOutput<DigitalSignal>().net?.signal)
		assertEquals(DigitalSignalFactory.of(Bit.False), bidiSplitterView.model.getOutput<DigitalSignal>(2).net?.signal)
		assertEquals(DigitalSignalFactory.of(Bit.False), bidiSplitterView.model.getOutput<DigitalSignal>(3).net?.signal)
	}
}