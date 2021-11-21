package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder

/** A base class for action implementations that are only enabled in [ApplicationMode.EDIT].*/
abstract class AbstractApplicationModeEditAction(
	actionBaseName: String,
	private val applicationModeHolder: ApplicationModeHolder,
	protected val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(actionBaseName) {

	private val applicationModeHandler: EventHandler<ApplicationModeEvent> = {
		if (it.source === applicationModeHolder) {
			updateEnabledness()
		}
	}

	protected val applicationMode: ApplicationMode get() = applicationModeHolder.currentMode

	init {
		enabled = applicationModeHolder.currentMode.isEdit()
		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(applicationModeHandler)
	}

	protected fun updateEnabledness() {
		enabled = applicationModeHolder.currentMode.isEdit() && calculateEnabledness()
	}

	/** Implemented by subclasses to further decide whether this [Action] should be enabled. */
	protected abstract fun calculateEnabledness(): Boolean
}