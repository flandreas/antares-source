package io.antarescircuit.jabbah.execution

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.scheduler.*
import kotlin.js.JsExport

@JsExport
interface SchedulerActions {
	val executionDepthAction: Action
}

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

/** Toggles the [SignalHandler.isDeepExecution] property of a [Scheduler].*/
class ExecutionDepthAction(
	val scheduler: Scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.deepSimulation", eventBus) {

	companion object {
		private const val SETTING_EXECUTION_DEPTH = "execution.scheduler.deepExecution"
	}

	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = {
		if (it.scheduler === scheduler) {
			updateState()
		}
	}

	init {
		scheduler.isDeepExecution = BaseModule.settings.getBoolean(SETTING_EXECUTION_DEPTH, true)
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		updateState()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(schedulerActivationStateHandler)
		BaseModule.settings.set(SETTING_EXECUTION_DEPTH, scheduler.isDeepExecution)
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