package ch.scorpion.jabbah.base.state

import ch.scorpion.jabbah.base.exception.IllegalArgumentException

class StateMachine<T : Any>(vararg states: State<T>) {

	private val states: List<State<T>> = listOf(*states)

	lateinit var currentState: State<T>
		private set

	init {
		if (states.isEmpty()) {
			throw IllegalArgumentException("StateMachine must have at least one state")
		}
		enter(states.first(), event = null)
	}

	fun handle(event: T) {
		currentState.match(event)?.let { proceedTo(event, it) } ?: throw IllegalArgumentException("Unhandled event $event")
	}

	private fun enter(state: State<T>, event: T?) {
		state.enter(event)
		currentState = state
	}

	private fun proceedTo(event: T, transition: Transition<T>) {
		transition.action.invoke(event)
		enter(transition.destination, event)
	}
}

typealias Action<T> = (T?) -> Unit

typealias TransitionCondition<T> = (T) -> Boolean

data class State<T : Any>(
	private val name: String,
	private val entryAction: Action<T> = {},
	private val exitAction: Action<T> = {}
) {

	private val transitions = mutableListOf<Transition<T>>()

	fun enter(event: T?) {
		entryAction.invoke(event)
	}

	fun add(transition: Transition<T>) {
		transitions.add(transition)
	}

	fun match(event: T): Transition<T>? {
		return transitions.firstOrNull { it.condition.invoke(event) }
	}
}

data class Transition<T : Any>(
	val destination: State<T>,
	val condition: TransitionCondition<T>,
	val action: Action<T> = {}
)