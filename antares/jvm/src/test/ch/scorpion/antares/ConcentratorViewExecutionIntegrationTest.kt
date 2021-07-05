package ch.scorpion.antares

import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.net.Concentrator
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.net.ConcentratorView
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import io.mockk.mockk
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class ConcentratorViewExecutionIntegrationTest : AbstractJvmCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView
	private val actorListener = mockk<ActorListener>(relaxed = true)

	private val circuitOutputView = CircuitInOutView(model = CircuitInOutImpl(portType = PortType.OUTPUT, bitWidth = BitWidth.BW_2))
	private val concentratorView = ConcentratorView(model = Concentrator(BitWidth.BW_2, BranchCount.BC_2))
	private val switchView1 = SwitchView()
	private val switchView2 = SwitchView()

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
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