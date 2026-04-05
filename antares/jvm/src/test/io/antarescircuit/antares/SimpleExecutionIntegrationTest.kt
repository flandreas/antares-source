package io.antarescircuit.antares

import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.math.MILLION
import io.antarescircuit.jabbah.execution.actor.ActorListener
import io.antarescircuit.jabbah.execution.actor.ActorState
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Integration tests for Antares components (built using graph module) and the execution module.
 * Tests a simple circuit consisting of a [SwitchView] directly connected to an [LEDView].
 */
class SimpleExecutionIntegrationTest : AbstractJvmCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var edgeView: EdgeView<*>
	private val switchView = SwitchView()
	private val ledView = LEDView()
	private val actorListener = mock<ActorListener>(MockMode.autofill)

	override fun getCircuitView(): GraphView {
		return circuitView
	}

	@BeforeTest
	fun setupCircuit() {
		setupLibrary()

		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		builder.addVerticeView(switchView).model.addActorListener(actorListener)
		builder.addVerticeView(ledView).model.addActorListener(actorListener)
		edgeView = builder.connect(switchView, ledView)
		edgeView.model.addActorListener(actorListener)

		// Note that SchedulerImpl uses a timer interval of 10 ms when running at full speed,
		// thus the distance of two time samples must be more than 10 ms to be recognizable by the test
		switchView.model.propagationDelay = LongValueImpl(1000 * MILLION)
		edgeView.model.propagationDelay = LongValueImpl(100 * MILLION)

		circuitView = builder.build()
	}

	@Test
	@Ignore
	fun shouldRunThrough() {
		startSimulation()
		assertEquals(ActorState.Waiting, switchView.model.state)

		proceedToNanos(1000 * MILLION)
		assertEquals(ActorState.Acting, switchView.model.state)
		assertEquals(ActorState.Waiting, edgeView.model.state)
		assertEquals(2, scheduler.numberOfRemainingSlots)

		switchView.model.actingVisualized(scheduler, actorListener)
		assertEquals(ActorState.Idle, switchView.model.state)
		assertEquals(ActorState.Waiting, edgeView.model.state)

		// EdgeView uses frozen time
		proceedFrozenTimeToNanos(1100 * MILLION)
		scheduler.printSchedule()
		assertEquals(ActorState.Acting, edgeView.model.state)
		assertEquals(1, scheduler.numberOfRemainingSlots)

		edgeView.model.actingVisualized(scheduler, actorListener)
		ledView.model.actingVisualized(scheduler, actorListener, ledView.model.createActorData(ledView.model.getInput<DigitalSignal>()))
		assertEquals(ActorState.Idle, edgeView.model.state)
		assertEquals(ActorState.Idle, ledView.model.state)
	}
}