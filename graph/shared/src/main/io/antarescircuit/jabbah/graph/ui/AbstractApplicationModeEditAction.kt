package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder
import io.antarescircuit.jabbah.graph.app.ApplicationModeObserver

/** A base class for action implementations that are only enabled in [ApplicationMode.EDIT].*/
abstract class AbstractApplicationModeEditAction(
	actionBaseName: String,
	private val applicationModeHolder: ApplicationModeHolder,
	protected val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(actionBaseName) {

	private val applicationModeObserver = ApplicationModeObserver(applicationModeHolder, eventBus) {
		updateEnabled()
	}

	init {
		enabled = applicationModeHolder.currentMode.isEdit()
	}

	override fun dispose() {
		super.dispose()
		applicationModeObserver.dispose()
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && applicationModeHolder.currentMode.isEdit()
}