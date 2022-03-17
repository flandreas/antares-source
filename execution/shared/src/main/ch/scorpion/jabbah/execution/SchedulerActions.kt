package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeedPauseEvent
import ch.scorpion.jabbah.execution.scheduler.*

/**
 * Base class for implementing [Action]s for controlling the [Scheduler].
 */
abstract class AbstractSchedulerAction(
	name: String,
	protected val eventBus: EventBus
) : AbstractAction(name)

/** Toggles property [Scheduler.isSingleStepMode]. */
class SingleStepModeAction(
	val scheduler: Scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.singleStepMode", eventBus) {

	private val schedulerSingleStepModeHandler: EventHandler<SchedulerSingleStepModeEvent> = { updateState() }

	init {
		eventBus.register(SchedulerSingleStepModeEvent::class, schedulerSingleStepModeHandler)
		updateState()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(schedulerSingleStepModeHandler)
	}

	private fun updateState() {
		selected = scheduler.isSingleStepMode
	}

	override fun execute(event: ActionEvent) {
		scheduler.isSingleStepMode = !scheduler.isSingleStepMode
	}
}

/**
 * Pauses the running [Scheduler], or resumes it if already paused.
 */
class PauseOrResumeAction(
	val scheduler: Scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.pause", eventBus) {

	companion object {
		private const val INACTIVE_ICON = "/img/pause24.png"
		private const val ACTIVE_ICON = "/img/pause-active24.png"
	}

	private val pausedHandler: EventHandler<SystemSpeedPauseEvent> = {
		if (it.source === scheduler.systemSpeedCategory.systemSpeed) {
			updateSelected()
		}
	}

	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = {
		if (it.scheduler === scheduler) {
			updateState()
			updateSelected()
		}
	}

	private val breakpointHandler: EventHandler<BreakpointEvent> = {
		if (it.scheduler === scheduler) {
			updateIcon()
		}
	}

	init {
		eventBus.register(SystemSpeedPauseEvent::class, pausedHandler)
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.register(BreakpointEvent::class, breakpointHandler)

		updateState()
		updateSelected()
		updateIcon()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(pausedHandler)
		eventBus.unregister(schedulerActivationStateHandler)
		eventBus.unregister(breakpointHandler)
	}

	override fun execute(event: ActionEvent) {
		if (scheduler.systemSpeedCategory.systemSpeed.isPaused) {
			scheduler.systemSpeedCategory.systemSpeed.resume()
		} else {
			scheduler.systemSpeedCategory.systemSpeed.pause()
		}
	}

	private fun updateSelected() {
		selected = scheduler.isActive && scheduler.systemSpeedCategory.systemSpeed.isPaused
		description = if (selected) {
			Translations.getString("execution.action.resume.desc")
		} else {
			Translations.getString("execution.action.pause.desc")
		}
	}

	private fun updateState() {
		enabled = scheduler.isActive
	}

	private fun updateIcon() {
		val newImagePath = if (scheduler.isInBreakpoint) ACTIVE_ICON else INACTIVE_ICON
		if (newImagePath != imagePath) {
			imagePath = newImagePath
		}
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

	private val executionDepthHandler: EventHandler<ExecutionDepthEvent> = {
		if (it.scheduler === scheduler) {
			updateState()
		}
	}

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

	private val stopOnIssueHandler: EventHandler<StopOnIssueEvent> = {
		if (it.scheduler === scheduler) {
			updateState()
		}
	}

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

	private val simulationTimeStatusEnabledHandler: EventHandler<SimulationTimeStatusEnabledEvent> = {
		if (it.scheduler === scheduler) {
			updateState()
		}
	}

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

	private val handler: EventHandler<EnableSoftBreakpointsEvent> = {
		if (it.scheduler === scheduler) {
			updateState()
		}
	}

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