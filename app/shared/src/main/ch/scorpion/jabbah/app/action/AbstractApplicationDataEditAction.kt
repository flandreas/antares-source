package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Command

/** A base class of [Command]s that depend on the entire [ApplicationData]. */
abstract class AbstractApplicationDataEditAction(
	baseName: String,
	protected val application: Application,
	protected val eventBus: EventBus = BaseModule.eventBus
): AbstractAction(baseName) {

	private val applicationDataHandler: EventHandler<ApplicationDataEvent> = { updateEnabled() }

	init {
		eventBus.register(ApplicationDataEvent::class, applicationDataHandler)
		updateEnabled()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(applicationDataHandler)
	}

	protected fun updateEnabled() {
		enabled = calculateEnabled()
	}

	protected open fun calculateEnabled(): Boolean {
		return application.data != null && !application.data!!.savable.readOnly
	}
}