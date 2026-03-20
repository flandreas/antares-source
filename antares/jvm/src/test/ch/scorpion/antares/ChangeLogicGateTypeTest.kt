package ch.scorpion.antares

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.gate.NonUnaryLogicGateType
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.antares.view.gate.LogicGateViewRenderers
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.graph.view.GraphView
import dev.mokkery.MockMode
import dev.mokkery.mock
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChangeLogicGateTypeTest : AbstractJvmCircuitTest() {

	private lateinit var circuitView: GraphView
	private val actorListener = mock<ActorListener>(MockMode.autofill)

	private lateinit var gateView: LogicGateView
	private lateinit var switchView1: SwitchView
	private lateinit var switchView2: SwitchView
	private lateinit var ledView: LEDView

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		gateView = LogicGateView.andGateView()
		switchView1 = SwitchView()
		switchView2 = SwitchView()
		ledView = LEDView()

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
		assertEquals(LogicGateViewRenderers.Nor.text, gateView.internalLabelText)

		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertTrue(ledView.model.isOn)
	}
}