package ch.scorpion.jabbah.execution.actor

import ch.scorpion.jabbah.execution.ExecutionTestRule
import ch.scorpion.jabbah.execution.SignalHandler
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Scenario tests for [ActorSupport].
 */
class ActorSupportScenarioTest {

	@BeforeTest
	fun setup() {
		ExecutionTestRule.configure()
	}

	val signalHandler: SignalHandler = mockk(relaxed = true)

	@Test
	fun shouldRequestActing() {
		val actorSupport = ActorSupport(mockk())
		val view1: ActorListener = mockk(relaxed = true)
		actorSupport.addListener(view1)
		val view2: ActorListener = mockk(relaxed = true)
		actorSupport.addListener(view2)

		actorSupport.requestActingAfter(signalHandler, 0, mockk())

		verify { signalHandler.requestActingAfter(any(), any(), any()) }
		verify { view1.actingRequested(any(), any(), any()) }
		verify { view2.actingRequested(any(), any(), any()) }
	}

	@Test
	fun shouldAct() {
		val actor: Actor = mockk()
		val actorSupport = ActorSupport(actor)
		val view1: ActorListener = mockk(relaxed = true)
		actorSupport.addListener(view1)
		val view2: ActorListener = mockk(relaxed = true)
		actorSupport.addListener(view2)

		val actorData: ActorData = mockk()
		actorSupport.requestActingAfter(signalHandler, 0, actorData)
		actorSupport.notifyActed(signalHandler, actorData)

		verify { view1.acted(actor, signalHandler, actorData) }
		verify { view2.acted(actor, signalHandler, actorData) }
	}

	@Test
	fun shouldWaitForAllVisualizations() {
		val actor: Actor = mockk()
		val actorSupport = ActorSupport(actor)
		val view1: ActorListener = mockk(relaxed = true)
		actorSupport.addListener(view1)
		val view2: ActorListener = mockk(relaxed = true)
		actorSupport.addListener(view2)

		val actorData: ActorData = mockk()
		actorSupport.requestActingAfter(signalHandler, 0, actorData)
		actorSupport.notifyActed(signalHandler, mockk())
		verify(exactly = 0) {signalHandler.actingDone(actor) }

		actorSupport.actingVisualized(signalHandler, view1)
		verify(exactly = 0) { signalHandler.actingDone(actor) }

		actorSupport.actingVisualized(signalHandler, view2)
		verify(exactly = 1) {signalHandler.actingDone(actor) }
	}
}
