package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.ApplicationModeEvent
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/** An [Action] for toggling the [ApplicationMode] of a [GraphPanel]. */
class ToggleApplicationModeAction(
	eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("execution.action.execute") {

	init {
		eventBus.register(ApplicationModeEvent::class) { updateState() }
		selected = false
	}

	override fun execute(event: ActionEvent) {
		GraphViewModule.applicationModeHolder.setMode(
			if (GraphViewModule.applicationModeHolder.currentMode.isExecute())
				ApplicationMode.EDIT
			else
				ApplicationMode.EXECUTE)
	}

	private fun updateState() {
		when (GraphViewModule.applicationModeHolder.currentMode) {
			ApplicationMode.EDIT -> {
				description = Translations.getString("execution.action.start.desc")
				selected = false
			}
			ApplicationMode.EXECUTE, ApplicationMode.EXEC_USECASE -> {
				description = Translations.getString("execution.action.stop.desc")
				selected = true
			}
		}
	}
}