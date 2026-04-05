package io.antarescircuit.antares

import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.math.MILLION
import io.antarescircuit.jabbah.execution.actor.ActorListener
import io.antarescircuit.jabbah.execution.actor.ActorState
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVertice
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Integration tests for Antares components (built using graph module) and the execution module.
 * Tests a circuit consisting of a [SwitchView] connected via a NOP component to an [LEDView].
 */
class SingleNestedExcecutionIntegrationTest : AbstractJvmCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var edgeView1: EdgeView<*>
	private lateinit var edgeView2: EdgeView<*>
	private lateinit var nop: SubGraphVerticeView<out SubGraphVertice>
	private lateinit var input: DigitalCircuitInOut
	private lateinit var output: DigitalCircuitInOut
	private lateinit var innerNet: Net<DigitalSignal>
	private val switchView = SwitchView()
	private val ledView = LEDView()
	private val actorListener = mock<ActorListener>(MockMode.autofill)

	override fun getCircuitView(): GraphView {
		return circuitView
	}

	@BeforeTest
	fun setupCircuit() {
		setupLibrary()

		TestLibraryBuilder().addNOP(LibraryModule.libraryHolder.library)
		nop = (LibraryModule.libraryHolder.library.get(TestLibraryBuilder.NOP) as LibraryElement).getNewInstance<SubGraphVerticeRef>()
			as SubGraphVerticeView<out SubGraphVertice>

		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		builder.addVerticeView(nop).model.addActorListener(actorListener)
		builder.addVerticeView(switchView).model.addActorListener(actorListener)
		builder.addVerticeView(ledView).model.addActorListener(actorListener)
		edgeView1 = builder.connect(switchView, nop)
		edgeView1.model.addActorListener(actorListener)
		edgeView2 = builder.connect(nop, ledView)
		edgeView2.model.addActorListener(actorListener)

		// Note that SchedulerImpl uses a timer interval of 10 ms when running at full speed,
		// thus the distance of two time samples must be more than 10 ms to be recognizable by the test
		switchView.model.propagationDelay = LongValueImpl(1000 * MILLION)
		edgeView1.model.propagationDelay = LongValueImpl(100 * MILLION)
		edgeView2.model.propagationDelay = LongValueImpl(100 * MILLION)
		nop.model.propagationDelay = LongValueImpl(300 * MILLION)

		// Set propagation delay of input CircuitInOut
		input = nop.model.getGraph().withId(1) as DigitalCircuitInOut
		input.propagationDelay = LongValueImpl(100 * MILLION)
		// Set propagation delay of output CircuitInOut
		output = nop.model.getGraph().withId(2) as DigitalCircuitInOut
		output.propagationDelay = LongValueImpl(100 * MILLION)
		// Set propagation delay of Net
		innerNet = nop.model.getGraph().withId(3) as Net<DigitalSignal>
		innerNet.propagationDelay = LongValueImpl(100 * MILLION)

		circuitView = builder.build()
	}

	@Test
	@Ignore
	fun shouldRunThrough() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)
		assertEquals(0, scheduler.numberOfRemainingSlots)
		scheduler.isSingleStepMode = false

		switchView.model.on(scheduler)
		// 2_400 Switch
		assertEquals(ActorState.Waiting, switchView.model.state)

		proceedToNanos(2_400 * MILLION)
		assertEquals(ActorState.Acting, switchView.model.state)
		switchView.model.actingVisualized(scheduler, actorListener)
		// 2_500 DigitalNet between Switch and NOP
		assertEquals(ActorState.Idle, switchView.model.state)
		assertEquals(ActorState.Waiting, edgeView1.model.state)

		proceedFrozenTimeToNanos(2_500 * MILLION)
		assertEquals(ActorState.Acting, edgeView1.model.state)
		edgeView1.model.actingVisualized(scheduler, actorListener)
		// 2_600 CircuitInOut "I1"
		// 2_800 NOP for stopping to glow
		assertEquals(ActorState.Idle, edgeView1.model.state)
		assertEquals(ActorState.Waiting, input.state)
		assertEquals(ActorState.Waiting, nop.subGraphVertice!!.state)

		proceedToNanos(2_600 * MILLION)
		// 2_700 DigitalNet between "I1" and "O1"
		// 2_800 NOP for stopping to glow
		assertEquals(ActorState.Idle, input.state)
		assertEquals(ActorState.Waiting, innerNet.state)
		assertEquals(ActorState.Waiting, nop.subGraphVertice!!.state)
		scheduler.printSchedule()

		proceedFrozenTimeToNanos(2_700 * MILLION)
		scheduler.printSchedule()
		// 2_800 NOP for stopping to glow
		// 2_800 CircuitInOut "O1"
		assertEquals(ActorState.Idle, innerNet.state)
		assertEquals(ActorState.Waiting, output.state)
		assertEquals(ActorState.Waiting, nop.subGraphVertice!!.state)

		proceedToNanos(2_800 * MILLION)
		nop.model.actingVisualized(scheduler, actorListener)
		// 2_900 DigitalNet between NOP and LED
		assertEquals(ActorState.Idle, output.state)
		assertEquals(ActorState.Idle, nop.subGraphVertice!!.state)
		assertEquals(ActorState.Waiting, edgeView2.model.state)

		proceedFrozenTimeToNanos(2_900 * MILLION)
		// LED has a propagation delay of 0 and does react directly to input changes
		edgeView2.model.actingVisualized(scheduler, actorListener)
		assertEquals(0, scheduler.numberOfRemainingSlots)
	}
}