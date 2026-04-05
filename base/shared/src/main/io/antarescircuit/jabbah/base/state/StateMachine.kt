package io.antarescircuit.jabbah.base.state

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.state.UnhandledEventBehaviour.Strict

typealias Action<T> = (T) -> Unit

typealias Condition<T> = (T) -> Boolean

/**
 * DSL factory method for creating a defining a [StateMachine].
 *
 * @param T the type of events handled by the created [StateMachine]
 * @param behaviour defines how the created [StateMachine] behaves upon unhandled events
 * @param init the expression that defines the contents of the created [StateMachine]
 * @param outer set if this [StateMachine] belongs to a [SuperState], whose inner [States][State] delegate back
 * to a [State] in [outer]
 */
fun <T> stateMachine(behaviour: UnhandledEventBehaviour = Strict, outer: StateMachine<T>? = null, init: StateMachine<T>.() -> Unit): StateMachine<T> {
	val sm = StateMachine(behaviour, outer)
	sm.init()
	return sm
}

/** Defines how a [StateMachine] behaves if a [State] doesn't handle a received event.*/
enum class UnhandledEventBehaviour {

	/** Throws an [IllegalArgumentException] in [StateMachine.handle].*/
	Strict {
		override fun behave(event: Any): Boolean = throw IllegalArgumentException("Unhandled event $event")
	},

	/** Returns `false` in [StateMachine.handle]. */
	Unhandled {
		override fun behave(event: Any): Boolean = false
	};

	abstract fun behave(event: Any): Boolean
}

/**
 * Defines a simple state machine consisting of a current [State], whose outgoing [Transitions][Transition]
 * lead to other [States][State].
 * The first [State] is regarded as the initial [State] and is automatically entered.
 *
 * @param T the type of events handled by this [StateMachine]
 * @property behaviour defines how this [StateMachine] behaves on unhandled events
 * @property outer set if this [StateMachine] belongs to a [SuperState], whose inner [States][State] delegate back
 * to a [State] in [outer]
 */
class StateMachine<T>(
	val behaviour: UnhandledEventBehaviour = Strict,
	private val outer: StateMachine<T>? = null
) {

	companion object {
		private val LOG by logger(StateMachine::class)
	}

	private val states = mutableListOf<State<T>>()

	/**
	 * Designate events to be ignored. These [Condition]s are only evaluated if none of the current
	 * [State]'s [Transition] has fired.
	 */
	private val ignoreEventConditions = mutableListOf<Condition<T>>()

	/**
	 * The [State] whose [Transitions][Transition] receive incoming events.
	 * Typically not used by clients, only by testing code.
	 */
	lateinit var currentState: State<T>
		private set

	fun start(event: T): StateMachine<T> {
		if (states.isEmpty()) {
			throw IllegalStateException("StateMachine must have at least 1 state")
		}
		LOG.trace("Start in state '${states.first().name}'")
		enter(states.first(), event)
		return this
	}

	private fun enter(state: State<T>, event: T) {
		LOG.trace("Enter state '${state.name}'")
		currentState = state
		state.enter(event)
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
		if (LOG.isTraceEnabled()) {
			LOG.trace("Handle event $event")
		}

		try {
			val transition = currentState.match(event)
			if (transition != null) {
				transferAlong(transition, event)
				return true
			}

			if (currentState.handle(event)) {
				return true
			}
		} catch (e: Throwable) {
			LOG.error("Error when handling event $event in state ${currentState.name}: ${System.getClassName(e)}")
			throw e
		}

		if (ignoreEventConditions.any { it.invoke(event) }) {
			LOG.trace("Explicitly ignored event $event")
			return true
		}

		LOG.trace("Unhandled event $event")

		return behaviour.behave(event as Any)
	}

	private fun transferAlong(transition: Transition<T>, event: T) {
		val destinationState = stateWithName(transition.destinationStateName)

		if (destinationState == null && outer != null) {
			// Try to give control back to outer StateMachine
			val outerDestinationState = outer.stateWithName(transition.destinationStateName)
			if (outerDestinationState != null) {
				currentState.exit(event)
				outer.transitAndEnter(transition, outerDestinationState, event)
				return
			}
		}

		if (destinationState == null) {
			throw IllegalArgumentException("Undefined destination state '${transition.destinationStateName}' in ${currentState.name}")
		}

		if (destinationState !== currentState) {
			currentState.exit(event)
		}

		transitAndEnter(transition, destinationState, event)
	}

	private fun transitAndEnter(transition: Transition<T>, destinationState: State<T>, event: T) {
		transition.transit(event)

		if (destinationState !== currentState) {
			LOG.trace("Transferring to ${destinationState::class.simpleName} '${destinationState.name}'")
			enter(destinationState, event)
		} else {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Stay in ${currentState::class.simpleName} '${destinationState.name}' with event $event")
			}
		}
	}

	private fun stateWithName(name: String): State<T>? {
		return states.firstOrNull { it.name == name }
	}

	/** ---- DSL methods */

	/** DSL method for registering conditions for ignored events. */
	fun ignoreEvent(condition: Condition<T>) {
		ignoreEventConditions.add(condition)
	}

	/** DSL method for registering a new [State]. */
	fun state(name: String, init: (State<T>.() -> Unit)? = null): State<T> {
		ensureUniqueStateName(name)

		val state = State<T>(name)
		if (init != null) {
			state.init()
		}
		states.add(state)
		return state
	}

	/** DSL method for registering a new [SuperState] by an expression that defines its inner [StateMachine].*/
	fun superstate(name: String, init: (SuperState<T>.() -> Unit)? = null): SuperState<T> {
		ensureUniqueStateName(name)

		val state = SuperState<T>(name, this)
		if (init != null) {
			state.init()
		}
		states.add(state)
		return state
	}

	private fun ensureUniqueStateName(name: String) {
		if (stateWithName(name) != null) {
			throw IllegalArgumentException("State name $name must be unique")
		}
	}
}
