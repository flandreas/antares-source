package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.OpenRecentMenu
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder

/**
 * Extends [OpenRecentMenu] to be disabled during simulation.
 */
class OpenRecentGraphMenu(
	application: DesktopApplication,
	private val applicationModeHolder: ApplicationModeHolder,
	eventBus: EventBus = BaseModule.eventBus,
) : OpenRecentMenu(application, eventBus) {

	private val applicationModeHandler: EventHandler<ApplicationModeEvent> = { updateEnabledness() }

	init {
		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(applicationModeHandler)
	}

	override val calculateEnabled: Boolean
		get() = super.calculateEnabled && applicationModeHolder.currentMode == ApplicationMode.EDIT
}