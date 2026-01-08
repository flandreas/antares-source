package ch.scorpion.antares

import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.net.Concentrator
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.net.ConcentratorView
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
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