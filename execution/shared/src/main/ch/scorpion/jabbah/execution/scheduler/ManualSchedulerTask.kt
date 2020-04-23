package ch.scorpion.jabbah.execution.scheduler

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule

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

	private var switch: SwitchableSchedulerTask? = null
	private val switchableSchedulerTaskHandler: EventHandler<SwitchableSchedulerTaskEvent> = {
		switch = it.switch
		updateState()
	}

	init {
		eventBus.register(SwitchableSchedulerTaskEvent::class, switchableSchedulerTaskHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(switchableSchedulerTaskHandler)
	}

	override fun execute(event: ActionEvent) {
		task.execute()
	}

	private fun updateState() {
		enabled = switch != null && switch!!.current === task
	}
}