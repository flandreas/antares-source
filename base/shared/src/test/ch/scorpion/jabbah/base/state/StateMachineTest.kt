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

	private val stateMachine = StateMachine(stateA, stateB, stateC)

	init {
		stateA.add(Transition(destination = stateB, condition = { it == "eventB"}, action = transitionActionAB ))
		stateA.add(Transition(destination = stateC, condition = { it == "eventC"}, action = transitionActionAC ))
	}

	@Test
	fun shouldRejectNoStates() {
		assertFailsWith(IllegalArgumentException::class) { StateMachine<String>() }
	}

	@Test
	fun shouldEnterStartState() {
		assertEquals(stateA, stateMachine.currentState)
		verify(exactly = 1) { entryActionA.invoke(any()) }
	}

	@Test
	fun shouldTransit() {
		stateMachine.handle("eventB")

		verify(exactly = 1) { transitionActionAB.invoke(any()) }
		verify(exactly = 1) { entryActionB.invoke(any()) }
		assertEquals(stateB, stateMachine.currentState)
	}
}