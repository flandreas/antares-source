package io.antarescircuit.jabbah.app.action

import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.ApplicationDataEvent
import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.Command

/**
 * A base class of [Command]s that depend on the entire [ApplicationData].
 *
 * @param requireEditable `true` if the [ApplicationData] needs to be editable in order for this
 * [AbstractApplicationDataEditAction] to be enabled.
 */
abstract class AbstractApplicationDataEditAction(
	baseName: String,
	protected val applicationDataHolder: ApplicationDataHolder,
	protected val eventBus: EventBus = BaseModule.eventBus,
	private val requireEditable: Boolean = true
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

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled()
			&& applicationDataHolder.data != null
			&& (!requireEditable || applicationDataHolder.data!!.savable.editable)
}