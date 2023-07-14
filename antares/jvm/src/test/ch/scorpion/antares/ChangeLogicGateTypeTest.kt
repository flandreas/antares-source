package ch.scorpion.antares

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.gate.NonUnaryLogicGateType
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.graph.view.GraphView
import io.mockk.mockk
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChangeLogicGateTypeTest : AbstractJvmCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView
	private val actorListener = mockk<ActorListener>(relaxed = true)

	private val gateView = LogicGateView.andGateView()
	private val switchView1 = SwitchView()
	private val switchView2 = SwitchView()
	private val ledView = LEDView()

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(gateView)
		builder.addVerticeView(switchView1)
		builder.addVerticeView(switchView2)
		builder.addVerticeView(ledView)

		builder.connect(switchView1, gateView, toPort = gateView.model.getInput(1))
		builder.connect(switchView2, gateView, toPort = gateView.model.getInput(2))
		builder.connect(gateView, ledView)

		circuitView = builder.build()
	}

	@Test
	fun shouldChangeOrGate() {
		gateView.logicGateType = NonUnaryLogicGateType.Nor

		assertEquals(Logic.NEGATIVE, (gateView.model.getOutput<DigitalSignal>() as DigitalPort).logic)

		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertTrue(ledView.model.isOn)
	}
}