package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.*
import javax.swing.Action

/**
 * Base class for implementing [Action]s for controlling the [Scheduler].
 */
abstract class AbstractSchedulerAction(
	name: String,
	protected val eventBus: EventBus
) : AbstractAction(name)

/** Toggles the [SchedulerActivationState] of a [Scheduler]. */
class PauseExecutionAction(
	val scheduler: Scheduler = ExecutionModule.scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.pause", eventBus) {

	private val schedulerRunningStateHandler: EventHandler<SchedulerRunningStateEvent> = { updateState() }

	init {
		eventBus.register(SchedulerRunningStateEvent::class, schedulerRunningStateHandler)
		updateState()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(schedulerRunningStateHandler)
	}

	private fun updateState() {
		selected = scheduler.isPaused
	}

	override fun execute(event: ActionEvent) {
		scheduler.isPaused = !scheduler.isPaused
	}
}

/** Performs a single execution step.*/
class StepExecutionAction(
	val scheduler: Scheduler = ExecutionModule.scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.step", eventBus) {

	private val schedulerRunningStateHandler: EventHandler<SchedulerRunningStateEvent> = { updateEnabledness(scheduler.numberOfRemainingSlots) }
	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = { updateEnabledness(scheduler.numberOfRemainingSlots) }
	private val schedulerStateHandler: EventHandler<SchedulerStateEvent> = { updateEnabledness(it.numberOfRemainingSlots) }

	init {
		eventBus.register(SchedulerRunningStateEvent::class, schedulerRunningStateHandler)
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.register(SchedulerStateEvent::class, schedulerStateHandler)
		enabled = false
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(schedulerRunningStateHandler)
		eventBus.unregister(schedulerActivationStateHandler)
		eventBus.unregister(schedulerStateHandler)
	}

	override fun execute(event: ActionEvent) {
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
) : AbstractSchedulerAction("execution.action.deepSimulation", eventBus) {

	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = { updateState() }
	private val executionDepthHandler: EventHandler<ExecutionDepthEvent> = { updateState() }

	init {
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.register(ExecutionDepthEvent::class, executionDepthHandler)
		updateState()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(schedulerActivationStateHandler)
		eventBus.unregister(executionDepthHandler)
	}

	private fun updateState() {
		selected = scheduler.isDeepExecution
		enabled = !scheduler.isActive
	}

	override fun execute(event: ActionEvent) {
		scheduler.isDeepExecution = selected
	}
}

/** Toggles the [Scheduler.isStopOnIssue] property. */
class StopOnIssueAction(
	private val scheduler: Scheduler = ExecutionModule.scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.stopOnIssue", eventBus) {

	private val stopOnIssueHandler: EventHandler<StopOnIssueEvent> = { updateState() }

	init {
		eventBus.register(StopOnIssueEvent::class, stopOnIssueHandler)
		updateState()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(stopOnIssueHandler)
	}

	override fun execute(event: ActionEvent) {
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
) : AbstractSchedulerAction("execution.action.enableSimulationTimeStatus", eventBus) {

	private val simulationTimeStatusEnabledHandler: EventHandler<SimulationTimeStatusEnabledEvent> = { updateState() }

	init {
		eventBus.register(SimulationTimeStatusEnabledEvent::class, simulationTimeStatusEnabledHandler)
		updateState()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(simulationTimeStatusEnabledHandler)
	}

	override fun execute(event: ActionEvent) {
		scheduler.isSimulationTimeStatusEnabled = !scheduler.isSimulationTimeStatusEnabled
	}

	private fun updateState() {
		selected = scheduler.isSimulationTimeStatusEnabled
	}
}

class PrintScheduleAction(
	private val scheduler: Scheduler = ExecutionModule.scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.print", eventBus) {

	override fun execute(event: ActionEvent) {
		scheduler.printSchedule()
	}
}