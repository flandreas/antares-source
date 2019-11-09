package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.*
import javax.swing.Action

/**
 * Base class for implementing [Action]s for controlling the [Scheduler].
 */
abstract class AbstractSchedulerAction(name: String) : AbstractAction(name)

/** Toggles the [SchedulerActivationState] of a [Scheduler]. */
class PauseExecutionAction(
	val scheduler: Scheduler,
	eventBus: EventBus
) : AbstractSchedulerAction("execution.action.pause") {

	init {
		eventBus.register(SchedulerRunningStateEvent::class) { updateState() }
		updateState()
	}

	private fun updateState() {
		selected = scheduler.isPaused
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		scheduler.isPaused = !scheduler.isPaused
	}
}

/** Performs a single execution step.*/
class StepExecutionAction(
	val scheduler: Scheduler = ExecutionModule.scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.step") {

	init {
		eventBus.register(SchedulerRunningStateEvent::class) { updateEnabledness(scheduler.numberOfRemainingSlots) }
		eventBus.register(SchedulerActivationStateEvent::class) { updateEnabledness(scheduler.numberOfRemainingSlots) }
		eventBus.register(SchedulerStateEvent::class) { updateEnabledness(it.numberOfRemainingSlots) }
		enabled = false
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		scheduler.step()
	}

	private fun updateEnabledness(numberOfRemainingSlots: Int) {
		enabled = scheduler.isPaused && scheduler.isActive && numberOfRemainingSlots > 0
	}
}

/** Toggles the [SignalHandler.isDeepExecution] property of a [Scheduler].*/
class ExecutionDepthAction(
	val scheduler: Scheduler = ExecutionModule.scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.deepSimulation") {

	init {
		eventBus.register(SchedulerActivationStateEvent::class) { updateState() }
		eventBus.register(ExecutionDepthEvent::class) { updateState() }
		updateState()
	}

	private fun updateState() {
		selected = scheduler.isDeepExecution
		enabled = !scheduler.isActive
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		scheduler.isDeepExecution = selected
	}
}

/** Toggles the [Scheduler.isStopOnIssue] property. */
class StopOnIssueAction(
	private val scheduler: Scheduler = ExecutionModule.scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.stopOnIssue") {

	init {
		eventBus.register(StopOnIssueEvent::class) { updateState() }
		updateState()
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		scheduler.isStopOnIssue = selected
	}

	private fun updateState() {
		selected = scheduler.isStopOnIssue
		enabled = !scheduler.isActive
	}
}

/** Toggles the [Scheduler.isSimulationTimeStatusEnabled] property.*/
class SimulationTimeStatusEnabledAction(
	private val scheduler: Scheduler = ExecutionModule.scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.enableSimulationTimeStatus") {

	init {
		eventBus.register(SimulationTimeStatusEnabledEvent::class) { updateState() }
		updateState()
	}

	override fun execute(event: ActionEvent) {
		scheduler.isSimulationTimeStatusEnabled = !scheduler.isSimulationTimeStatusEnabled
	}

	private fun updateState() {
		selected = scheduler.isSimulationTimeStatusEnabled
	}
}

class PrintScheduleAction(
	private val scheduler: Scheduler = ExecutionModule.scheduler
) : AbstractSchedulerAction("execution.action.print") {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		scheduler.printSchedule()
	}
}