package io.antarescircuit.antares

import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.net.BranchCount
import io.antarescircuit.antares.model.net.Splitter
import io.antarescircuit.antares.model.signal.*
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.net.SplitterView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.execution.actor.ActorListener
import io.antarescircuit.jabbah.graph.view.GraphView
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.*

class SplitterViewExecutionIntegrationTest : AbstractJvmCircuitTest() {

	private lateinit var circuitView: GraphView
	private val actorListener = mock<ActorListener>(MockMode.autofill)

	private lateinit var circuitInputView: DigitalCircuitInOutView
	private lateinit var splitterView: SplitterView
	private lateinit var ledView1: LEDView
	private lateinit var ledView2: LEDView

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		circuitInputView = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(bitWidth = BitWidth.BW_2))
		splitterView = SplitterView(model = Splitter(BitWidth.BW_2, BranchCount.BC_2))
		ledView1 = LEDView()
		ledView2 = LEDView()

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

		assertEquals(DigitalSignalFactory.of(Bit.False), splitterView.model.getOutput<DigitalSignal>(2).net!!.signal)
		assertEquals(DigitalSignalFactory.of(Bit.False), splitterView.model.getOutput<DigitalSignal>(3).net!!.signal)
	}

	@Test
	fun shouldSplit() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)
		assertFalse(ledView1.model.isOn)
		assertFalse(ledView2.model.isOn)

		circuitInputView.model.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_2, 2L), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertFalse(ledView1.model.isOn)
		assertTrue(ledView2.model.isOn)
	}
}