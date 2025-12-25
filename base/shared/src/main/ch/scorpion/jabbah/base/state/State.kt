package ch.scorpion.jabbah.base.state

open class State<T>(val name: String) {

    private val transitions = mutableListOf<Transition<T>>()
    private var entryAction: Action<T> = {}
    private var exitAction: Action<T> = {}

    /** Called by the [StateMachine] when this [State] is entered.*/
    open fun enter(event: T) {
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
     * the event in any other, possibly subclass-depending way. The default behavior of this [State] implementation
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
    fun stayIf(condition: Condition<T>, init: (Transition<T>.() -> Unit)? = null): Transition<T> =
        addTransition(Transition(name, condition), init)

    /** DSL method for registering a [Transition] that stays in this [State] if the given [Condition] is true.*/
    fun stayIf(condition: Condition<T>): Transition<T> = stayIf(condition, init = null)

    /**
     * DSL method for registering a [Transition] that always stays in this [State].
     * Should only be used as the last [Transition] in a [State], because subsequent ones would be unreachable.
     */
    fun stayOtherwise(init: (Transition<T>.() -> Unit)? = null): Transition<T> =
        addTransition(Transition(name) { true }, init)

    /**
     * DSL method for registering a [Transition] to another [State].
     * @param init the expression for defining the created [Transition], e.g. with a [Condition] and an [Action]
     */
    fun transitTo(destinationStateName: String, init: Transition<T>.() -> Unit): Transition<T> =
        addTransition(Transition(destinationStateName), init)

    private fun addTransition(transition: Transition<T>, init: (Transition<T>.() -> Unit)?): Transition<T> {
        init?.let { transition.init() }
        transitions.add(transition)
        return transition
    }
}