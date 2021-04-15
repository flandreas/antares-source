package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.AbstractSchedulerAction
import ch.scorpion.jabbah.graph.model.module.GraphModelModule

/**
 * Represents a conflict of signals clashing while trying to forward a signal
 * along a [SignalPropagationChain].
 */
data class SignalConflict<T: Any>(
	val convertedSignal: T?,
	val chain: SignalPropagationChain<T>
)

enum class SignalConflictBehaviour(
	val customName: String,
	val actionBaseName: String
) {

	IGNORE("ignore", "graph.action.signalConflictBehaviour.ignore"),
	ISSUE_WARNING("warning", "graph.action.signalConflictBehaviour.warning"),
	ISSUE_ERROR("error", "graph.action.signalConflictBehaviour.error");

	companion object {

		/** The name of the [String] property in [Properties] designating the custom name of the [SignalConflictBehaviour] to use.*/
		const val PROP_SIGNAL_CONFLICT_BEHAVIOUR = "ch.scorpion.jabbah.graph.model.net.SignalConflictBehaviour"

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