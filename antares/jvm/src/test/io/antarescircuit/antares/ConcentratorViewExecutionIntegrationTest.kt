package io.antarescircuit.antares

import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.net.BranchCount
import io.antarescircuit.antares.model.net.Concentrator
import io.antarescircuit.antares.model.signal.*
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.net.ConcentratorView
import io.antarescircuit.jabbah.execution.actor.ActorListener
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.GraphView
import dev.mokkery.MockMode
import dev.mokkery.mock
import org.junit.Test
import kotlin.test.assertEquals

class ConcentratorViewExecutionIntegrationTest : AbstractJvmCircuitTest() {

	private lateinit var circuitView: GraphView
	private val actorListener = mock<ActorListener>(MockMode.autofill)

	private lateinit var circuitOutputView: DigitalCircuitInOutView
	private lateinit var concentratorView: ConcentratorView
	private lateinit var switchView1: SwitchView
	private lateinit var switchView2: SwitchView

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		circuitOutputView = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.OUTPUT, bitWidth = BitWidth.BW_2))
		concentratorView = ConcentratorView(model = Concentrator(BitWidth.BW_2, BranchCount.BC_2))
		switchView1 = SwitchView()
		switchView2 = SwitchView()

		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(switchView1)
		builder.addVerticeView(switchView2)
		builder.addVerticeView(concentratorView)
		builder.addVerticeView(circuitOutputView)

		builder.connect(switchView1, concentratorView, toPort = concentratorView.model.getInput(2))
		builder.connect(switchView2, concentratorView, toPort = concentratorView.model.getInput(3))
		builder.connect(concentratorView, circuitOutputView)

		circuitView = builder.build()
	}

	@Test
	fun shouldSetOutputEdgeViewsToDefaultSignal() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(DigitalSignalFactory.allOf(BitWidth.BW_2, Bit.False), concentratorView.model.getOutput<DigitalSignal>().net!!.signal)
	}

	@Test
	fun shouldSplit() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)
		assertEquals(DigitalSignalFactory.allOf(BitWidth.BW_2, Bit.False), circuitOutputView.model.signal)

		switchView1.model.toggle(scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(DigitalSignalFactory.ofBits(listOf(Bit.True, Bit.False)), circuitOutputView.model.signal)
	}
}