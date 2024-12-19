package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.app.action.AbstractApplicationDataEditAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder

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