package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import java.awt.event.ActionEvent
import javax.swing.Action

class EnableRepaintingObserverAction(
		eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("view.action.repaintingObserver.enable") {

	init {
		eventBus.register(RepaintingObserverEnabledEvent::class, { updateState() })
		eventBus.register(RepaintingObserverRunningEvent::class, { updateState() })
		updateState()
	}

	override fun actionPerformed(e: ActionEvent?) {
		RepaintingObserver.isEnabled = !RepaintingObserver.isEnabled
	}

	private fun updateState() {
		putValue(Action.SELECTED_KEY, RepaintingObserver.isEnabled)
		isEnabled = !RepaintingObserver.isRunning
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

	override fun actionPerformed(e: ActionEvent?) {
		RepaintingObserver.isRunning = RepaintingObserver.isRunning.not()
	}

	private fun updateState() {
		putValue(Action.SELECTED_KEY, RepaintingObserver.isRunning)
		isEnabled = RepaintingObserver.isEnabled
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

	override fun actionPerformed(e: ActionEvent?) {
		RepaintingObserver.previousLogEntry()
	}

	private fun updateState() {
		isEnabled = RepaintingObserver.isEnabled
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

	override fun actionPerformed(e: ActionEvent?) {
		RepaintingObserver.nextLogEntry()
	}

	private fun updateState() {
		isEnabled = RepaintingObserver.isEnabled
				&& !RepaintingObserver.isRunning
				&& RepaintingObserver.logIndex < RepaintingObserver.logSize - 1
	}
}
