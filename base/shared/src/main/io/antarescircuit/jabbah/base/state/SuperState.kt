package io.antarescircuit.jabbah.base.state

import io.antarescircuit.jabbah.base.state.UnhandledEventBehaviour.Strict

/** A [State] that contains an entire inner [StateMachine] to which events are forwarded. */
class SuperState<T>(name: String, val outerStateMachine: StateMachine<T>) : State<T>(name) {

    lateinit var stateMachine: StateMachine<T>
        private set

    override fun enter(event: T) {
        super.enter(event)
        stateMachine.start(event)
    }

    override fun handle(event: T): Boolean =
        stateMachine.handle(event)

    /** ---- DSL methods */

    /** DSL method for defining the inner [StateMachine] of this [SuperState]. */
    fun stateMachine(behaviour: UnhandledEventBehaviour = Strict, init: StateMachine<T>.() -> Unit) {
        stateMachine = stateMachine(behaviour, outerStateMachine, init)
    }
}