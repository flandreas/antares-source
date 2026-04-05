package io.antarescircuit.jabbah.graph.ui.logview

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.execution.scheduler.SchedulerActivationStateEvent
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.model.LogEvent

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