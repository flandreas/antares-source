package ch.scorpion.jabbah.execution.scheduler

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.module.BaseModule

data class SwitchableSchedulerTaskEvent(val switch: SwitchableSchedulerTask)

/**
 * A meta [SchedulerTask] for switching between [SchedulerTask] implementations.
 * Posts a [SwitchableSchedulerTaskEvent] on [EventBus] whenever the current [SchedulerTask] changes.
 */
class SwitchableSchedulerTask(
	private val tasks: List<SchedulerTask>,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerTask("execution.task.switch") {

	init {
		checkArgument(tasks.isNotEmpty(), "tasks must not be empty")
	}

	var current: SchedulerTask = tasks.first()
		set(value) {
			if (field !== value) {
				if (!tasks.contains(value)) {
					throw IllegalArgumentException("unsupported SchedulerTask")
				}
				current.stop()
				field = value
				current.startIfNeeded()
				eventBus.post(SwitchableSchedulerTaskEvent(this))
			}
		}

	/** ---- [SchedulerTask] interface */

	override fun bind(scheduler: Scheduler) {
		tasks.forEach { it.bind(scheduler) }
	}

	override fun startIfNeeded() {
		current.startIfNeeded()
	}

	override fun stop() {
		current.stop()
	}
}

class SchedulerTaskSelectionAction(
	private val switch: SwitchableSchedulerTask,
	private val task: SchedulerTask,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(task.nameKey) {

	private val switchableSchedulerTaskHandler: EventHandler<SwitchableSchedulerTaskEvent> = { updateState() }

	init {
		eventBus.register(SwitchableSchedulerTaskEvent::class, switchableSchedulerTaskHandler)
		updateState()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(switchableSchedulerTaskHandler)
	}

	override fun execute(event: ActionEvent) {
		switch.current = task
	}

	private fun updateState() {
		selected = switch.current === task
	}
}