package ch.scorpion.jabbah.base.state

import ch.scorpion.jabbah.base.module.BaseModule
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.*

class SuperStateTest {

	@BeforeTest
	fun setup() {
		BaseModule.require()
	}

	private val entrySuperstate = mockk<Action<String>>(relaxed = true)
	private val entrySubstateA = mockk<Action<String>>(relaxed = true)
	private val entrySubstateB = mockk<Action<String>>(relaxed = true)

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
		val sm = buildStateMachine().start()

		val handled = sm.handle("eventSuperstate")

		assertTrue(handled)
		assertEquals("superstate", sm.currentState.name)
		assertEquals("substateA", (sm.currentState as SuperState<String>).stateMachine.currentState.name)
		verify(exactly = 1) { entrySuperstate.invoke(any()) }
		verify(exactly = 1) { entrySubstateA.invoke(any()) }
	}

	@Test
	fun shouldForwardEventToSubStateMachine() {
		val sm = buildStateMachine().start()

		sm.handle("eventSuperstate")
		sm.handle("eventSubstateB")

		assertEquals("substateB", (sm.currentState as SuperState<String>).stateMachine.currentState.name)
		verify(exactly = 1) { entrySubstateB.invoke(any()) }
	}
}