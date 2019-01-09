package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.ApplicationModeEvent
import javax.swing.Action

/** A base class for action implementations that are only enabled in [ApplicationMode.EDIT].*/
abstract class AbstractApplicationModeEditAction(
	actionBaseName: String,
	initialMode: ApplicationMode = ApplicationMode.EDIT,
	protected val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(actionBaseName) {

	/** Contains the [ApplicationMode] received with the most recent [ApplicationModeEvent].*/
	protected var applicationMode: ApplicationMode = initialMode
		set(value) {
			if (value != field) {
				field = value
				updateEnabledness()
			}
		}

	init {
		enabled = initialMode == ApplicationMode.EDIT
		eventBus.register(ApplicationModeEvent::class) {
			applicationMode = it.applicationMode
		}
	}

	protected fun updateEnabledness() {
		enabled = applicationMode == ApplicationMode.EDIT && calculateEnabledness()
	}

	/** Implemented by subclasses to further decide whether this [Action] should be enabled. */
	protected abstract fun calculateEnabledness(): Boolean
}