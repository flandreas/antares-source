package ch.scorpion.jabbah.base.state

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger

typealias Action<T> = (T?) -> Unit

typealias Condition<T> = (T) -> Boolean

fun <T> stateMachine(strict: Boolean = true, init: StateMachine<T>.() -> Unit): StateMachine<T> {
	val sm = StateMachine<T>(strict)
	sm.init()
	return sm
}

/**
 * Defines a simple state machine consisting of a current [State], whose outgoing [Transitions][Transition]
 * lead to other [States][State].
 *
 * @param T the type of events handled by this [StateMachine]
 * The first [State] is regarded as the initial [State] and is automatically entered.
 * @property strict `true` if this [StateMachine] throws an exception if an event cannot be
 * handled by the current [State].
 */
class StateMachine<T>(val strict: Boolean = true) {

	companion object {
		private val LOG by logger(StateMachine::class)
	}

	private val states = mutableListOf<State<T>>()

	/**
	 * The [State] whose [Transitions][Transition] receive incoming events.
	 * Typically not used by clients, but only by testing code.
	 */
	lateinit var currentState: State<T>
		private set

	fun start(): StateMachine<T> {
		if (states.isEmpty()) {
			throw IllegalStateException("StateMachine must have at least 1 state")
		}
		LOG.debug("Start in state '${states.first().name}'")
		enter(states.first(), event = null)
		return this
	}

	private fun enter(state: State<T>, event: T?) {
		state.enter(event)
		currentState = state
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
		currentState.match(event)?.let { transferAlong(it, event) }
			?: return if (strict) throw IllegalArgumentException("Unhandled event $event") else false
		return true
	}

	private fun transferAlong(transition: Transition<T>, event: T) {
		val destinationState = stateWithName(transition.destinationStateName) ?: throw IllegalArgumentException("Undefined state '${transition.destinationStateName}'")

		if (destinationState !== currentState) {
			currentState.exit(event)
		}

		transition.transit(event)

		if (destinationState !== currentState) {
			LOG.debug("Enter state '${destinationState.name}'")
			currentState = destinationState
			currentState.enter(event)
		}
	}

	private fun stateWithName(name: String): State<T>? {
		return states.firstOrNull() { it.name == name }
	}

	/** ---- DSL methods */

	fun state(name: String, init: (State<T>.() -> Unit)? = null): State<T> {
		if (stateWithName(name) != null) {
			throw IllegalArgumentException("State name $name must be unique")
		}

		val state = State<T>(name)
		if (init != null) {
			state.init()
		}
		states.add(state)
		return state
	}
}

class State<T>(val name: String) {

	private val transitions = mutableListOf<Transition<T>>()
	private var entryAction: Action<T> = {}
	private var exitAction: Action<T> = {}

	fun enter(event: T?) {
		entryAction.invoke(event)
	}

	fun exit(event: T) {
		exitAction.invoke(event)
	}

	fun match(event: T): Transition<T>? {
		return transitions.firstOrNull { it.condition.invoke(event) }
	}

	/** ---- DSL methods */

	fun onEntry(action: Action<T>) {
		entryAction = action
	}

	fun onExit(action: Action<T>) {
		exitAction = action
	}

	fun transitTo(destinationStateName: String, init: Transition<T>.() -> Unit): Transition<T> {
		val transition = Transition<T>(destinationStateName)
		transition.init()
		transitions.add(transition)
		return transition
	}
}

class Transition<T>(val destinationStateName: String) {

	lateinit var condition: Condition<T>
		private set

	private var action: Action<T> = {}

	fun transit(event: T) {
		action.invoke(event)
	}

	/** ---- DSL methods */

	fun given(condition: Condition<T>) {
		this.condition = condition
	}

	fun onTransit(action: Action<T>) {
		this.action = action
	}
}