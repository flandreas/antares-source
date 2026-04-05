package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import javax.swing.JMenu
import javax.swing.JMenuItem

open class OpenRecentMenu(
	protected val application: DesktopApplication,
	protected val eventBus: EventBus = BaseModule.eventBus
) : JMenu(Translations.getString("file.action.openRecent.name")) {

	private val savableHistoryHandler: EventHandler<SavableHistoryEvent> = { updateContent() }
	private val currentSavableEvent: EventHandler<CurrentSavableEvent> = { it.savable?.let { updateContent() } }

	init {
		eventBus.register(SavableHistoryEvent::class, savableHistoryHandler)
		eventBus.register(CurrentSavableEvent::class, currentSavableEvent)
		updateEnabledness()
	}

	open fun dispose() {
		eventBus.unregister(savableHistoryHandler)
		eventBus.unregister(currentSavableEvent)
	}

	protected open val calculateEnabled: Boolean get() = itemCount > 0

	protected fun updateEnabledness() {
		isEnabled = calculateEnabled
	}

	private fun updateContent() {
		removeAll()
		application.controller.mostRecentSavables.savables.forEach {
			if (it != application.controller.data?.savable) {
				add(JMenuItem(ActionWrapperSwing(OpenRecentFileAction(it, application))))
			}
		}
		updateEnabledness()
	}
}