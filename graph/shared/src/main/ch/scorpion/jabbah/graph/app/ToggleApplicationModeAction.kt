package ch.scorpion.jabbah.graph.app

import ch.scorpion.jabbah.app.ApplicationData
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
import ch.scorpion.jabbah.graph.library.AbstractContainerLibraryElementSavable

/**
 * An [Action] for toggling the [ApplicationMode] of the [ApplicationModeHolder].
 * If the optional [ApplicationDataHolder] is set, this [ToggleApplicationModeAction] is only enabled
 * if [ApplicationData] is present.
 */
class ToggleApplicationModeAction(
	private val applicationDataHolder: ApplicationDataHolder?,
	private val applicationModeHolder: ApplicationModeHolder,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("execution.action.execute") {

	private val applicationModeHandler: EventHandler<ApplicationModeEvent> = {
		if (it.source === applicationModeHolder) {
			updateState()
		}
	}

	private val applicationDataHandler: EventHandler<ApplicationDataEvent> = { updateState() }

	init {
		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
		eventBus.register(ApplicationDataEvent::class, applicationDataHandler)
		updateState()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(applicationModeHandler)
		eventBus.unregister(applicationDataHandler)
	}

	override fun execute(event: ActionEvent) {
		if (applicationModeHolder.currentMode.isExecute()) {
			applicationModeHolder.setMode(ApplicationMode.EDIT)
			updateState()
		} else {
			InvocationHandler.invoke {
				applicationModeHolder.setMode(ApplicationMode.EXECUTE)
				updateState()
			}
		}
	}

	private fun updateState() {
		enabled = applicationDataHolder == null || applicationDataHolder.data?.savable is AbstractContainerLibraryElementSavable

		when (applicationModeHolder.currentMode) {
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