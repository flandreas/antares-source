package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.app.action.AbstractApplicationDataEditAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ApplicationModeEvent
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder

/**
 * A base class of [Actions][Action] that are only enabled in [ApplicationMode.EDIT]
 * and existing [ApplicationData].
 */
abstract class AbstractApplicationDataEditModeAction(
	baseName: String,
	applicationDataHolder: ApplicationDataHolder,
	protected val applicationModeHolder: ApplicationModeHolder,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractApplicationDataEditAction(baseName, applicationDataHolder, eventBus) {

	private val applicationModeHandler: EventHandler<ApplicationModeEvent> = {
		if (it.source === applicationModeHolder) {
			updateEnabled()
		}
	}

	init {
		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(applicationModeHandler)
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && applicationModeHolder.currentMode.isEdit()
}