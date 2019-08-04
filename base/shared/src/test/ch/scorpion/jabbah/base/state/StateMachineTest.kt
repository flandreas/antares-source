package ch.scorpion.jabbah.base.state

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import kotlin.test.Test
import kotlin.test.assertFailsWith

class StateMachineTest {

	private val entryActionA = mockk<Action<String>>(relaxed = true)
	private val exitActionA = mockk<Action<String>>(relaxed = true)
	private val stateA = State(name = "A", entryAction = entryActionA, exitAction = exitActionA)
	private val transitionActionAB = mockk<Action<String>>(relaxed = true)
	private val transitionActionAC = mockk<Action<String>>(relaxed = true)

	private val entryActionB = mockk<Action<String>>(relaxed = true)
	private val stateB = State<String>("B", entryAction = entryActionB)

	private val stateC = State<String>("C")

	private lateinit var stateMachine : StateMachine<String>

	init {
		stateA.add(Transition(destination = stateB, condition = { it == "eventB"}, action = transitionActionAB ))
		stateA.add(Transition(destination = stateC, condition = { it == "eventC"}, action = transitionActionAC ))
		stateA.add(Transition(destination = stateA, condition = { it == "eventA"}))
	}

	private fun buildStrictStateMachine() {
		buildStateMachine(strict = true)
	}

	private fun buildNonStrictStateMachine() {
		buildStateMachine(strict = false)
	}

	private fun buildStateMachine(strict: Boolean) {
		stateMachine = StateMachine(stateA, stateB, stateC, strict = strict)
	}

	@Test
	fun shouldRejectNoStates() {
		buildStrictStateMachine()

		assertFailsWith(IllegalArgumentException::class) { StateMachine<String>() }
	}

	@Test
	fun shouldEnterStartState() {
		buildStrictStateMachine()

		assertEquals(stateA, stateMachine.currentState)
		verify(exactly = 1) { entryActionA.invoke(any()) }
	}

	@Test
	fun shouldTransit() {
		buildStrictStateMachine()

		val handled = stateMachine.handle("eventB")

		assertTrue(handled)
		verify(exactly = 1) { transitionActionAB.invoke(any()) }
		verify(exactly = 1) { entryActionB.invoke(any()) }
		assertEquals(stateB, stateMachine.currentState)
	}

	@Test
	fun strictStateMachineShouldRejectUnsupportedEvent() {
		buildStrictStateMachine()

		assertFailsWith(IllegalArgumentException::class) { stateMachine.handle("unsupported") }
	}

	@Test
	fun nonStrictStateMachineShouldIgnoreUnsupportedEvent() {
		buildNonStrictStateMachine()

		val handled = stateMachine.handle("unsupported")

		assertFalse(handled)
		assertEquals(stateA, stateMachine.currentState)
	}

	@Test
	fun selfTransitionShouldNotTriggerActions() {
		buildStrictStateMachine()

		val handled = stateMachine.handle("eventA")

		assertTrue(handled)
		verify(exactly = 1) { entryActionA.invoke(any()) }
		assertEquals(stateA, stateMachine.currentState)
	}
}