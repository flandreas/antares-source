package io.antarescircuit.jabbah.graph.model.net

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.AbstractSchedulerAction
import io.antarescircuit.jabbah.execution.ExecutionError
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.OutputPort
import io.antarescircuit.jabbah.graph.model.Net

/**
 * Represents a conflict of signals clashing when an [OutputPort] forwards its outgoing
 * signal to a [Net].
 *
 * @param signal the signal being asserted by the current [Actor]
 * @param combinedNet the [CombinedNet] [signal] is about to be sent into. Used for setting [ExecutionError]
 * @param destinationPort the [OutputPort] that is in conflict with [signal]. Used for error description generation.
 */
data class SignalConflict<T : Any>(
	val signal: T?,
	val combinedNet: CombinedNet<T>,
	val destinationPort: OutputPort<T>
)

/** Defined what to do when a [SignalConflict] occurs during execution. */
enum class SignalConflictBehaviour(
	val customName: String,
	val actionBaseName: String
) {

	IGNORE("ignore", "graph.action.signalConflictBehaviour.ignore"),
	ISSUE_WARNING("warning", "graph.action.signalConflictBehaviour.warning"),
	ISSUE_ERROR("error", "graph.action.signalConflictBehaviour.error");

	companion object {

		/** The name of the [String] property in [Properties] designating the custom name of the [SignalConflictBehaviour] to use.*/
		const val PROP_SIGNAL_CONFLICT_BEHAVIOUR = "io.antarescircuit.jabbah.graph.model.net.SignalConflictBehaviour"

		fun withName(customName: String): SignalConflictBehaviour =
			values().firstOrNull { it.customName == customName } ?:
				throw IllegalArgumentException("Unknown SignalConflictBehaviour '$customName'")
	}

	override fun toString(): String = Translations.getString("${actionBaseName}.name")
}

data class SignalConflictBehaviourEvent(val current: SignalConflictBehaviour)

class SignalConflictBehaviourHolder(
	current: SignalConflictBehaviour = SignalConflictBehaviour.withName(BaseModule.properties.getString(SignalConflictBehaviour.PROP_SIGNAL_CONFLICT_BEHAVIOUR)),
	private val eventBus: EventBus = BaseModule.eventBus
) {

	var current: SignalConflictBehaviour = current
		set(value) {
			field = value
			eventBus.post(field)
		}
}

class SignalConflictBehaviourAction(
	private val behaviour: SignalConflictBehaviour,
	private val holder: SignalConflictBehaviourHolder = GraphModelModule.signalConflictBehaviourHolder,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction(behaviour.actionBaseName, eventBus) {

	private val signalConflictBehaviourHandler: EventHandler<SignalConflictBehaviourEvent> = { updateState() }

	init {
		eventBus.register(SignalConflictBehaviourEvent::class, signalConflictBehaviourHandler)
		updateState()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(signalConflictBehaviourHandler)
	}

	override fun execute(event: ActionEvent) {
		BaseModule.properties.customize(SignalConflictBehaviour.PROP_SIGNAL_CONFLICT_BEHAVIOUR, behaviour.customName)
		holder.current = behaviour
	}

	private fun updateState() {
		selected = holder.current == behaviour
	}
}