package ch.scorpion.jabbah.base.state


class Transition<T>(
    val destinationStateName: String,
    condition: Condition<T>? = null
) {

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