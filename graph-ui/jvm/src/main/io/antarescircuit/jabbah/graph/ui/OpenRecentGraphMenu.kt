package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.app.DesktopApplication
import io.antarescircuit.jabbah.app.OpenRecentMenu
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ApplicationModeEvent
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder

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