package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.app.ApplicationModeObserver

/** A base class for action implementations that are only enabled in [ApplicationMode.EDIT].*/
abstract class AbstractApplicationModeEditAction(
	actionBaseName: String,
	private val applicationModeHolder: ApplicationModeHolder,
	protected val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(actionBaseName) {

	private val applicationModeObserver = ApplicationModeObserver(applicationModeHolder, eventBus) {
		updateEnabledness()
	}

	init {
		enabled = applicationModeHolder.currentMode.isEdit()
	}

	override fun dispose() {
		super.dispose()
		applicationModeObserver.dispose()
	}

	protected fun updateEnabledness() {
		enabled = applicationModeHolder.currentMode.isEdit() && calculateEnabledness()
	}

	/** Implemented by subclasses to further decide whether this [Action] should be enabled. */
	protected abstract fun calculateEnabledness(): Boolean
}