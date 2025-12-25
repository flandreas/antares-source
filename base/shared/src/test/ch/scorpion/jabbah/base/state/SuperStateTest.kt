package ch.scorpion.jabbah.base.state

import ch.scorpion.jabbah.base.module.BaseModule
import dev.mokkery.MockMode.autofill
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SuperStateTest {

	@BeforeTest
	fun setup() {
		BaseModule.require()
	}

	private val entrySuperstate = mock<Action<String>>(autofill)
	private val entrySubstateA = mock<Action<String>>(autofill)
	private val entrySubstateB = mock<Action<String>>(autofill)

	private fun buildStateMachine(behaviour: UnhandledEventBehaviour = UnhandledEventBehaviour.Strict): StateMachine<String> {

		return stateMachine(behaviour) {

			state("A") {
				transitTo("superstate") {
					given { it == "eventSuperstate"}
				}
			}

			superstate("superstate") {
				onEntry(entrySuperstate)
				stateMachine(behaviour) {

					state("substateA") {
						onEntry(entrySubstateA)
						transitTo("substateB") {
							given { it == "eventSubstateB" }
						}
						transitTo("A") {
							given { it == "eventBack" }
						}
					}

					state("substateB") {
						onEntry(entrySubstateB)
					}
				}
			}
		}
	}

	@Test
	fun shouldEnterSubStateMachine() {
		val sm = buildStateMachine().start("START")

		val handled = sm.handle("eventSuperstate")

		assertTrue(handled)
		assertEquals("superstate", sm.currentState.name)
		assertEquals("substateA", (sm.currentState as SuperState<String>).stateMachine.currentState.name)
		verify(exactly(1)) { entrySuperstate.invoke(any()) }
		verify(exactly( 1)) { entrySubstateA.invoke(any()) }
	}

	@Test
	fun shouldForwardEventToSubStateMachine() {
		val sm = buildStateMachine().start("START")

		sm.handle("eventSuperstate")
		sm.handle("eventSubstateB")

		assertEquals("substateB", (sm.currentState as SuperState<String>).stateMachine.currentState.name)
		verify(exactly(1)) { entrySubstateB.invoke(any()) }
	}

	@Test
	fun shouldTransitionBackToOuterStateMachine() {
		val sm = buildStateMachine().start("START")
		sm.handle("eventSuperstate")

		sm.handle("eventBack")

		assertEquals("A", sm.currentState.name)
	}
}