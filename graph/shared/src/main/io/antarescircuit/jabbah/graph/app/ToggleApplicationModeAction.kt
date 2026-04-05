package io.antarescircuit.jabbah.graph.app

import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.ApplicationDataEvent
import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.library.AbstractContainerLibraryElementSavable

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