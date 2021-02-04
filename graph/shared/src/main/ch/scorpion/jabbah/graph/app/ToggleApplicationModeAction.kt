package ch.scorpion.jabbah.graph.app

import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/** An [Action] for toggling the [ApplicationMode] of the [ApplicationModeHolder]. */
class ToggleApplicationModeAction(
	private val applicationDataHolder: ApplicationDataHolder,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("execution.action.execute") {

	private val applicationModeHandler: EventHandler<ApplicationModeEvent> = { updateState() }
	private val applicationDataHandler: EventHandler<ApplicationDataEvent> = {
		if (it.newData == null) {
			// Stop simulation if application data has been closed during simulation
			GraphViewModule.applicationModeHolder.setMode(ApplicationMode.EDIT)
		}
		updateState()
	}

	init {
		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
		eventBus.register(ApplicationDataEvent::class, applicationDataHandler)
		selected = false
		enabled = false
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(applicationModeHandler)
		eventBus.unregister(applicationDataHandler)
	}

	override fun execute(event: ActionEvent) {
		if (GraphViewModule.applicationModeHolder.currentMode.isExecute()) {
			GraphViewModule.applicationModeHolder.setMode(ApplicationMode.EDIT)
			updateState()
		} else {
			InvocationHandler.invoke {
				GraphViewModule.applicationModeHolder.setMode(ApplicationMode.EXECUTE)
				updateState()
			}
		}
	}

	private fun updateState() {
		enabled = applicationDataHolder.data != null

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