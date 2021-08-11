package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.*

/**
 * Base class for implementing [Action]s for controlling the [Scheduler].
 */
abstract class AbstractSchedulerAction(
	name: String,
	protected val eventBus: EventBus
) : AbstractAction(name)

/** Toggles the [SchedulerActivationState] of a [Scheduler]. */
class PauseExecutionAction(
	val scheduler: Scheduler,
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

/** Resumes execution of a [Scheduler] after it has been suspended by a breakpoint.*/
class ResumeExecutionAction(
	val scheduler: Scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.resume", eventBus) {

	private val handler: EventHandler<BreakpointEvent> = { updateEnabledness() }

	init {
		eventBus.register(BreakpointEvent::class, handler)
		enabled = false
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(handler)
	}

	override fun execute(event: ActionEvent) {
		scheduler.resume()
	}

	private fun updateEnabledness() {
		enabled = scheduler.isInBreakpoint
	}
}

/** Toggles the [SignalHandler.isDeepExecution] property of a [Scheduler].*/
class ExecutionDepthAction(
	val scheduler: Scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.deepSimulation", eventBus) {

	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = {
		if (it.scheduler === scheduler) {
			updateState()
		}
	}
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
	private val scheduler: Scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.pauseOnIssue", eventBus) {

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
	private val scheduler: Scheduler,
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

class EnableSoftBreakpointsAction(
	private val scheduler: Scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.enableSoftBreakpoints", eventBus) {

	private val handler: EventHandler<EnableSoftBreakpointsEvent> = { updateState() }

	init {
		updateState()
		eventBus.register(EnableSoftBreakpointsEvent::class, handler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(handler)
	}

	override fun execute(event: ActionEvent) {
		scheduler.isSoftBreakpointsEnabled = !scheduler.isSoftBreakpointsEnabled
	}

	private fun updateState() {
		selected = scheduler.isSoftBreakpointsEnabled
	}
}

class PrintScheduleAction(
	private val scheduler: Scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.print", eventBus) {

	override fun execute(event: ActionEvent) {
		scheduler.printSchedule()
	}
}