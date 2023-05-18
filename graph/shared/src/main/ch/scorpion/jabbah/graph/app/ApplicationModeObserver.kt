package ch.scorpion.jabbah.graph.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Observes the current [ApplicationMode] of an [ApplicationModeHolder] and calls
 * a callback notifier if it has changed.
 */
class ApplicationModeObserver(
	private val applicationModeHolder: ApplicationModeHolder,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val notifier: () -> Unit
) {

	val currentMode: ApplicationMode get() = applicationModeHolder.currentMode

	private val applicationModeHandler: EventHandler<ApplicationModeEvent> = {
		if (it.source === applicationModeHolder) {
			notifier()
		}
	}

	init {
		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
	}

	fun dispose() {
		eventBus.unregister(applicationModeHandler)
	}
}