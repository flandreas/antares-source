package ch.scorpion.jabbah.base.state

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.state.UnhandledEventBehaviour.*
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.*

class StateMachineTest {

	@BeforeTest
	fun setup() {
		BaseModule.require()
	}

	private val entryA = mockk<Action<String>>(relaxed = true)
	private val exitA = mockk<Action<String>>(relaxed = true)
	private val transitionActionAB = mockk<Action<String>>(relaxed = true)
	private val transitionActionAC = mockk<Action<String>>(relaxed = true)
	private val entryB = mockk<Action<String>>(relaxed = true)

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
		verify(exactly = 1) { entryA.invoke(any()) }
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
		verify(exactly = 1) { transitionActionAB.invoke(any()) }
		verify(exactly = 1) { exitA.invoke(any())}
		verify(exactly = 1) { entryB.invoke(any()) }
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
		verify(exactly = 1) { entryA.invoke(any())} // from init
		verify(exactly = 0) { exitA.invoke(any()) }
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