package io.antarescircuit.jabbah.execution.scheduler

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule

/** A [SchedulerTask] for executing a [Scheduler] step manually (mainly used for debugging). */
class ManualSchedulerTask : AbstractSchedulerTask("execution.task.manual") {

	private lateinit var scheduler: Scheduler

	fun execute() {
		scheduler.execute()
	}

	/** ---- [SchedulerTask] interface */

	override fun bind(scheduler: Scheduler) {
		this.scheduler = scheduler
	}

	override fun startIfNeeded() {
		// not used
	}

	override fun stop() {
		// not used
	}
}

class ManualSchedulerTaskAction(
	private val task: ManualSchedulerTask,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("execution.action.manualTask") {

	override fun execute(event: ActionEvent) {
		task.execute()
	}
}