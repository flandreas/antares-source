package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule

abstract class AbstractRepaintingObserverAction(
	name: String,
	protected val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(name) {

	private val enabledHandler: EventHandler<RepaintingObserverEnabledEvent> = { updateState() }
	private val runningHandler: EventHandler<RepaintingObserverRunningEvent> = { updateState() }

	init {
		eventBus.register(RepaintingObserverEnabledEvent::class, enabledHandler)
		eventBus.register(RepaintingObserverRunningEvent::class, runningHandler)
		updateState()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(enabledHandler)
		eventBus.unregister(runningHandler)

	}

	protected abstract fun updateState()
}

class EnableRepaintingObserverAction : AbstractRepaintingObserverAction("view.action.repaintingObserver.enable") {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		RepaintingObserver.isEnabled = !RepaintingObserver.isEnabled
	}

	override fun updateState() {
		selected = RepaintingObserver.isEnabled
		enabled = !RepaintingObserver.isRunning
	}
}

class RunRepaintingObserverAction : AbstractRepaintingObserverAction("view.action.repaintingObserver.run") {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		RepaintingObserver.isRunning = RepaintingObserver.isRunning.not()
	}

	override fun updateState() {
		selected = RepaintingObserver.isRunning
		enabled = RepaintingObserver.isEnabled
	}
}

class PreviousRepaintingObserverLogAction : AbstractRepaintingObserverAction("view.action.repaintingObserver.log.previous") {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		RepaintingObserver.previousLogEntry()
	}

	override fun updateState() {
		enabled = RepaintingObserver.isEnabled
			&& !RepaintingObserver.isRunning
			&& RepaintingObserver.logIndex > 0
	}
}

class NextRepaintingObserverLogAction : AbstractRepaintingObserverAction("view.action.repaintingObserver.log.next") {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		RepaintingObserver.nextLogEntry()
	}

	override fun updateState() {
		enabled = RepaintingObserver.isEnabled
			&& !RepaintingObserver.isRunning
			&& RepaintingObserver.logIndex < RepaintingObserver.logSize - 1
	}
}
