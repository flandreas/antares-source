package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import javax.swing.Action

/** A base class for action implementations that are only enabled in [ApplicationMode.EDIT].*/
abstract class AbstractApplicationModeEditAction(
	actionBaseName: String,
	initialMode: ApplicationMode = ApplicationMode.EDIT,
	protected val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(actionBaseName) {

	private val applicationModeHandler: EventHandler<ApplicationModeEvent> = {
		applicationMode = it.applicationMode
	}

	/** Contains the [ApplicationMode] received with the most recent [ApplicationModeEvent].*/
	protected var applicationMode: ApplicationMode = initialMode
		set(value) {
			if (value != field) {
				field = value
				updateEnabledness()
			}
		}

	init {
		enabled = initialMode.isEdit()
		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(applicationModeHandler)
	}

	protected fun updateEnabledness() {
		enabled = applicationMode.isEdit() && calculateEnabledness()
	}

	/** Implemented by subclasses to further decide whether this [Action] should be enabled. */
	protected abstract fun calculateEnabledness(): Boolean
}