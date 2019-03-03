package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.ApplicationModeEvent
import ch.scorpion.jabbah.graph.ApplicationModeHolder

/** An [Action] for toggling the [ApplicationMode] of a [GraphPanel]. */
class ToggleApplicationModeAction(
	private val appModeHolder: ApplicationModeHolder,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("simulation.action.execute") {

	init {
		eventBus.register(ApplicationModeEvent::class) { updateState() }
		selected = false
	}

	override fun execute(event: ActionEvent) {
		appModeHolder.toggleMode()
	}

	private fun updateState() {
		when (appModeHolder.currentMode) {
			ApplicationMode.EDIT -> {
				description = Translations.getString("simulation.action.start.desc")
				selected = false
			}
			ApplicationMode.EXECUTE -> {
				description = Translations.getString("simulation.action.stop.desc")
				selected = true
			}
		}
	}
}