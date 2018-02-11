package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

class EnableRepaintingObserverAction(
		eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("view.action.repaintingObserver.enable") {

	init {
		eventBus.register(RepaintingObserverEnabledEvent::class, { updateState() })
		eventBus.register(RepaintingObserverRunningEvent::class, { updateState() })
		updateState()
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		RepaintingObserver.isEnabled = !RepaintingObserver.isEnabled
	}

	private fun updateState() {
		selected = RepaintingObserver.isEnabled
		enabled = !RepaintingObserver.isRunning
	}
}

class RunRepaintingObserverAction(
		eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("view.action.repaintingObserver.run") {

	init {
		eventBus.register(RepaintingObserverEnabledEvent::class, { updateState() })
		eventBus.register(RepaintingObserverRunningEvent::class, { updateState()})
		updateState()
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		RepaintingObserver.isRunning = RepaintingObserver.isRunning.not()
	}

	private fun updateState() {
		selected = RepaintingObserver.isRunning
		enabled = RepaintingObserver.isEnabled
	}
}

class PreviousRepaintingObserverLogAction(
		eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("view.action.repaintingObserver.log.previous") {

	init {
		eventBus.register(RepaintingObserverRunningEvent::class, { updateState() })
		eventBus.register(RepaintingObserverLogEvent::class, { updateState() })
		updateState()
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		RepaintingObserver.previousLogEntry()
	}

	private fun updateState() {
		enabled = RepaintingObserver.isEnabled
				&& !RepaintingObserver.isRunning
				&& RepaintingObserver.logIndex > 0
	}
}

class NextRepaintingObserverLogAction(
		eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("view.action.repaintingObserver.log.next") {

	init {
		eventBus.register(RepaintingObserverRunningEvent::class, { updateState() })
		eventBus.register(RepaintingObserverLogEvent::class, { updateState() })
		updateState()
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		RepaintingObserver.nextLogEntry()
	}

	private fun updateState() {
		enabled = RepaintingObserver.isEnabled
				&& !RepaintingObserver.isRunning
				&& RepaintingObserver.logIndex < RepaintingObserver.logSize - 1
	}
}
