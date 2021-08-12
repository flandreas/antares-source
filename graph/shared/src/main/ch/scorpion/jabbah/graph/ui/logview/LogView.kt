package ch.scorpion.jabbah.graph.ui.logview

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.model.LogEvent

interface LogView : UIView {

	/** Called by [LogViewController] whenever the view model [LogEventHistory] has changed. */
	fun refresh(oldColumnsCount: Int)
}

/** Displays collected [LogEvent]s.*/
class LogViewController(
	private val applicationContextHolder: GraphApplicationContextHolder,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<LogView>() {

	/** Contains the [LogEvent]s collected by this [LogViewController].*/
	val logEventHistory = LogEventHistory()

	val clearAction: Action = ClearAction()

	private val logEventHandler: EventHandler<LogEvent> = { handle(it) }
	private val activationHandler: EventHandler<SchedulerActivationStateEvent> = { handle(it) }

	init {
		eventBus.register(LogEvent::class, logEventHandler)
		eventBus.register(SchedulerActivationStateEvent::class, activationHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(logEventHandler)
		eventBus.unregister(activationHandler)
	}

	private fun clear() {
		val oldColumnsCount = logEventHistory.eventColumnsCount
		logEventHistory.clear()
		view.refresh(oldColumnsCount)
	}

	private fun handle(event: LogEvent) {
		val oldColumnsCount = logEventHistory.eventColumnsCount
		logEventHistory.add(event)
		view.refresh(oldColumnsCount)
	}

	private fun handle(event: SchedulerActivationStateEvent) {
		if (event.scheduler === applicationContextHolder.scheduler) {
			if (event.scheduler.isActive) {
				clear()
			}
		}
	}

	private inner class ClearAction : AbstractAction("graph.action.clearLogPanel", imagePath = "/img/trash-16.png") {
		override fun execute(event: ActionEvent) {
			clear()
		}
	}
}