package ch.scorpion.jabbah.base.state

import ch.scorpion.jabbah.base.exception.IllegalArgumentException

/**
 * Defines a simple state machine consisting of a current [State], whose outgoing [Transition]
 * lead to other [States][State].
 *
 * @param T the type of events handled by this [StateMachine]
 * @param states the [States][State] this [StateMachine] consists of. This must be at least one [State].
 * The first [State] is regarded as the initial [State] and is automatically entered.
 * @property strict `true` if this [StateMachine] throws an exception if an event cannot be
 * handled by the current [State].
 */
class StateMachine<T : Any>(
	vararg states: State<T>,
	val strict: Boolean = true
) {

	private val states: List<State<T>> = listOf(*states)

	/**
	 * The [State] whose [Transitions][Transition] receive incoming events.
	 * Typically not used by clients, but only by testing code.
	 */
	lateinit var currentState: State<T>
		private set

	init {
		if (states.isEmpty()) {
			throw IllegalArgumentException("StateMachine must have at least one state")
		}
		enter(states.first(), event = null)
	}

	/**
	 * Tries to handle the specified event by matching it with the first outgoing [Transition]
	 * of the current [State] and proceeding to the corresponding next [State].
	 *
	 * @return `true` if [event] was accepted by the current [State], or `false` otherwise AND
	 * this [StateMachine] is not strict
	 * @throws IllegalArgumentException if this [StateMachine] is strict and the event wasn't handled
	 * by the current [State]
	 */
	fun handle(event: T): Boolean {
		currentState.match(event)?.let { proceedTo(event, it) }
			?: return if (strict) throw IllegalArgumentException("Unhandled event $event") else false
		return true
	}

	private fun enter(state: State<T>, event: T?) {
		state.enter(event)
		currentState = state
	}

	private fun proceedTo(event: T, transition: Transition<T>) {
		transition.action.invoke(event)
		if (transition.destination !== currentState) {
			enter(transition.destination, event)
		}
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