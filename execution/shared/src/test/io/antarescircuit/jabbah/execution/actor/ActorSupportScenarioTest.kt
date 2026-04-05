package io.antarescircuit.jabbah.execution.actor

import io.antarescircuit.jabbah.execution.ExecutionTestRule
import io.antarescircuit.jabbah.execution.SignalHandler
import dev.mokkery.MockMode.autofill
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
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

	val signalHandler: SignalHandler = mock(autofill)

	@Test
	fun shouldRequestActing() {
		val actorSupport = ActorSupport(mock())
		val view1: ActorListener = mock(autofill)
		actorSupport.addListener(view1)
		val view2: ActorListener = mock(autofill)
		actorSupport.addListener(view2)

		actorSupport.requestActingAfter(signalHandler, 0, mock())

		verify { signalHandler.requestActingAfter(any(), any(), any()) }
		verify { view1.actingRequested(any(), any(), any()) }
		verify { view2.actingRequested(any(), any(), any()) }
	}

	@Test
	fun shouldAct() {
		val actor: Actor = mock()
		val actorSupport = ActorSupport(actor)
		val view1: ActorListener = mock(autofill)
		actorSupport.addListener(view1)
		val view2: ActorListener = mock(autofill)
		actorSupport.addListener(view2)

		val actorData: ActorData = mock()
		actorSupport.requestActingAfter(signalHandler, 0, actorData)
		actorSupport.notifyActed(signalHandler, actorData)

		verify { view1.acted(actor, signalHandler, actorData) }
		verify { view2.acted(actor, signalHandler, actorData) }
	}

	@Test
	fun shouldWaitForAllVisualizations() {
		val actor: Actor = mock()
		val actorSupport = ActorSupport(actor)
		val view1: ActorListener = mock(autofill)
		actorSupport.addListener(view1)
		val view2: ActorListener = mock(autofill)
		actorSupport.addListener(view2)

		val actorData: ActorData = mock()
		actorSupport.requestActingAfter(signalHandler, 0, actorData)
		actorSupport.notifyActed(signalHandler, mock())
		verify(exactly(0)) {signalHandler.actingDone(actor, actorData) }

		actorSupport.actingVisualized(signalHandler, view1, actorData)
		verify(exactly (0)) { signalHandler.actingDone(actor, actorData) }

		actorSupport.actingVisualized(signalHandler, view2, actorData)
		verify(exactly(1)) { signalHandler.actingDone(actor, actorData) }
	}
}
