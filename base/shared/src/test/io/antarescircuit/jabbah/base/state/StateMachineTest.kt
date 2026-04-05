package io.antarescircuit.jabbah.base.state

import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.state.UnhandledEventBehaviour.*
import dev.mokkery.MockMode.autofill
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlin.test.*

class StateMachineTest {

	@BeforeTest
	fun setup() {
		BaseModule.require()
	}

	private val entryA = mock<Action<String>>(autofill)
	private val exitA = mock<Action<String>>(autofill)
	private val transitionActionAB = mock<Action<String>>(autofill)
	private val transitionActionAC = mock<Action<String>>(autofill)
	private val entryB = mock<Action<String>>(autofill)

	private fun buildStateMachine(behaviour: UnhandledEventBehaviour = Strict): StateMachine<String> {

		return stateMachine(behaviour) {
			ignoreEvent { it == "ignored"}

			state("A") {
				onEntry(entryA)
				onExit(exitA)
				transitTo("B") {
					given { it == "eventB" }
					onTransit(transitionActionAB)
				}
				transitTo("C") {
					given { it == "eventC" }
					onTransit(transitionActionAC)
				}
				transitTo("A") {
					given { it == "eventA" }
				}
				stayIf { it == "stay"}
			}

			state("B") {
				onEntry(entryB)
			}

			state("C")
		}
	}

	@Test
	fun shouldRejectToStartEmptyStateMachine() {
		assertFailsWith(IllegalStateException::class) { StateMachine<Any>().start("START") }
	}

	@Test
	fun shouldEnterStartState() {
		val sm = buildStateMachine().start("START")

		assertEquals("A", sm.currentState.name)
		verify { entryA.invoke(any()) }
	}

	@Test
	fun stateNamesShouldBeUnique() {
		val sm = buildStateMachine()
		assertFailsWith(IllegalArgumentException::class) { sm.state("A")}
	}

	@Test
	fun shouldTransit() {
		val sm = buildStateMachine().start("START")

		val handled = sm.handle("eventB")

		assertTrue(handled)
		verify(exactly(1)) { transitionActionAB.invoke(any()) }
		verify(exactly(1)) { exitA.invoke(any())}
		verify(exactly(1)) { entryB.invoke(any()) }
		assertEquals("B", sm.currentState.name)
	}

	@Test
	fun shouldStay() {
		val sm = buildStateMachine().start("START")

		val handled = sm.handle("stay")

		assertTrue(handled)
		assertEquals("A", sm.currentState.name)
	}

	@Test
	fun selfTransitionShouldNotTriggerAction() {
		val sm = buildStateMachine().start("START")

		val handled = sm.handle("eventA")

		assertTrue(handled)
		verify(exactly(1)) { entryA.invoke(any())} // from init
		verify(exactly(0)) { exitA.invoke(any()) }
		assertEquals("A", sm.currentState.name)
	}

	@Test
	fun strictStateMachineShouldRejectUnsupportedEvent() {
		val sm = buildStateMachine().start("START")

		assertFailsWith(IllegalArgumentException::class) { sm.handle("unsupported")}
	}

	@Test
	fun nonStrictStateMachineShouldIgnoreUnsupportedEvent() {
		val sm = buildStateMachine(Unhandled).start("START")

		val handled = sm.handle("unsupported")

		assertFalse(handled)
		assertEquals("A", sm.currentState.name)
	}

	@Test
	fun shouldStayInStateWithIgnoredEvent() {
		val sm = buildStateMachine(Unhandled).start("START")

		val handled = sm.handle("ignored")

		assertTrue(handled)
		assertEquals("A", sm.currentState.name)
	}
}