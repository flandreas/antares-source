package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Command

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