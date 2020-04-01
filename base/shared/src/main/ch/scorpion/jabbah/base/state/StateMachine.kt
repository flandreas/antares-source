package ch.scorpion.jabbah.base.state

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.state.UnhandledEventBehaviour.Strict

typealias Action<T> = (T?) -> Unit

typealias Condition<T> = (T) -> Boolean

/**
 * DSL factory method for creating an defining a [StateMachine].
 *
 * @param T the type of events handled by the created [StateMachine]
 * @param behaviour defines how the created [StateMachine] behaves upon unhandled events
 * @param init the expression that defines the contents of the created [StateMachine]
 */
fun <T> stateMachine(behaviour: UnhandledEventBehaviour = Strict, init: StateMachine<T>.() -> Unit): StateMachine<T> {
	val sm = StateMachine<T>(behaviour)
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
 */
class StateMachine<T>(val behaviour: UnhandledEventBehaviour = Strict) {

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
		LOG.debug("Enter state '${state.name}'")
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
		val transition = currentState.match(event)
		if (transition != null) {
			transferAlong(transition, event)
			return true
		}

		if (currentState.handle(event)) {
			return true
		}

		if (ignoreEventConditions.any { it.invoke(event) }) {
			LOG.debug("Explicitly ignored event $event")
			return true
		}

		LOG.debug("Unhandled event $event")
		return behaviour.behave(event as Any)
	}

	private fun transferAlong(transition: Transition<T>, event: T) {
		val destinationState = stateWithName(transition.destinationStateName)
			?: throw IllegalArgumentException("Undefined state '${transition.destinationStateName}'")

		if (destinationState !== currentState) {
			currentState.exit(event)
		}

		transition.transit(event)

		if (destinationState !== currentState) {
			enter(destinationState, event)
		} else {
			LOG.debug("Stay in state '${destinationState.name}' with event $event")
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

		val state = SuperState<T>(name)
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

open class State<T>(val name: String) {

	private val transitions = mutableListOf<Transition<T>>()
	private var entryAction: Action<T> = {}
	private var exitAction: Action<T> = {}

	/** Called by the [StateMachine] when this [State] is entered.*/
	open fun enter(event: T?) {
		entryAction.invoke(event)
	}

	/** Called by the [StateMachine] when this [State] is exited.*/
	fun exit(event: T) {
		exitAction.invoke(event)
	}

	/** Returns the [Transition] whose [Condition] matches the given event, if any.*/
	fun match(event: T): Transition<T>? {
		return transitions.firstOrNull { it.condition.invoke(event) }
	}

	/**
	 * Asks this [State] to handle the specified event. This method is called by the [StateMachine] if no
	 * [Transition] of this [State] matched the specified event, which gives this [State] the chance to handle
	 * the event in any other, possibly subclass-depending way. The default behaviour of this [State] implementation
	 * is to not handle the event.
	 *
	 * @return `true` if [event] was handled by this [State]
	 */
	open fun handle(event: T): Boolean {
		return false
	}

	/** ---- DSL methods */

	/** DSL method for registering the [Action] to be executed when this [State] is entered. */
	fun onEntry(action: Action<T>) {
		entryAction = action
	}

	/** DSL method for registering the [Action] to be executed when this [State] is existed. */
	fun onExit(action: Action<T>) {
		exitAction = action
	}

	/**
	 * DSL method for registering a [Transition] that stays in this [State] if the given [Condition] is true.
	 * @param init the expression for defining the created [Transition], e.g. with an [Action]
	 */
	fun stayIf(condition: Condition<T>, init: (Transition<T>.() -> Unit)? = null): Transition<T> {
		val transition = Transition(name, condition)
		init?.let { transition.init() }
		transitions.add(transition)
		return transition
	}

	/** DSL method for registering a [Transition] that stays in this [State] if the given [Condition] is true.*/
	fun stayIf(condition: Condition<T>): Transition<T> {
		return stayIf(condition, init = null)
	}

	/**
	 * DSL method for registering a [Transition] to another [State].
	 * @param init the expression for defining the created [Transition], e.g. with a [Condition] and an [Action]
	 */
	fun transitTo(destinationStateName: String, init: Transition<T>.() -> Unit): Transition<T> {
		val transition = Transition<T>(destinationStateName)
		transition.init()
		transitions.add(transition)
		return transition
	}
}

/** A [State] that contains an entire inner [StateMachine] to which events are forwarded. */
class SuperState<T>(name: String) : State<T>(name) {

	lateinit var stateMachine: StateMachine<T>
		private set

	override fun enter(event: T?) {
		super.enter(event)
		stateMachine.start()
	}

	override fun handle(event: T): Boolean {
		return stateMachine.handle(event)
	}

	/** ---- DSL methods */

	/** DSL method for defining the inner [StateMachine] of this [SuperState]. */
	fun stateMachine(behaviour: UnhandledEventBehaviour = Strict, init: StateMachine<T>.() -> Unit) {
		stateMachine = ch.scorpion.jabbah.base.state.stateMachine(behaviour, init)
	}
}

class Transition<T>(val destinationStateName: String, condition: Condition<T>? = null) {

	lateinit var condition: Condition<T>
		private set

	init {
		if (condition != null) {
			this.condition = condition
		}
	}

	private var action: Action<T> = {}

	fun transit(event: T) {
		action.invoke(event)
	}

	/** ---- DSL methods */

	/** DSL method for registering the [Condition] defining whether this [Transition] can be passed for a particular event. */
	fun given(condition: Condition<T>) {
		this.condition = condition
	}

	/** DSL method for registering the [Action] to be executed when this [Transition] gets passed. */
	fun onTransit(action: Action<T>) {
		this.action = action
	}
}