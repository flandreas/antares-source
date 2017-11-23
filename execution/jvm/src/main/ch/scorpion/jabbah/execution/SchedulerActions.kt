package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.*
import java.awt.event.ActionEvent
import javax.swing.Action

/**
 * Base class for implementing [Action]s for controlling the [Scheduler].
 */
abstract class AbstractSchedulerAction(name: String) : AbstractAction(name)

/** Toggles the [SchedulerActivationState] of a [Scheduler]. */
class PauseExecutionAction(
    val scheduler: Scheduler,
    eventBus: EventBus
) : AbstractSchedulerAction("simulator.action.pause") {

    init {
        eventBus.register(SchedulerRunningStateEvent::class, { updateState() })
        updateState()
    }

    private fun updateState() {
        putValue(Action.SELECTED_KEY, scheduler.isPaused)
    }

    override fun actionPerformed(e: ActionEvent?) {
        scheduler.isPaused = !scheduler.isPaused
    }
}

/** Performs a single execution step.*/
class StepExecutionAction(
    val scheduler: Scheduler,
    eventBus: EventBus
) : AbstractSchedulerAction("simulator.action.step") {

    init {
        eventBus.register(SchedulerRunningStateEvent::class, { updateEnabledness(scheduler.numberOfRemainingSlots) })
        eventBus.register(SchedulerActivationStateEvent::class, { updateEnabledness(scheduler.numberOfRemainingSlots) })
        eventBus.register(SchedulerStateEvent::class, { updateEnabledness(it.numberOfRemainingSlots) })
        isEnabled = false
    }

    override fun actionPerformed(e: ActionEvent?) {
        scheduler.step()
    }

    private fun updateEnabledness(numberOfRemainingSlots: Int) {
        isEnabled = scheduler.isPaused && scheduler.isActive && numberOfRemainingSlots > 0
    }
}

/** Toggles the [SignalHandler.isDeepExecution] property of a [Scheduler].*/
class ExecutionDepthAction(
    val scheduler: Scheduler = ExecutionModule.scheduler,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("simulator.action.deepSimulation") {

    init {
        eventBus.register(SchedulerActivationStateEvent::class, { updateState() })
        eventBus.register(ExecutionDepthEvent::class, { updateState() })
        updateState()
    }

    private fun updateState() {
        putValue(Action.SELECTED_KEY, scheduler.isDeepExecution)
        isEnabled = !scheduler.isActive
    }

    override fun actionPerformed(e: ActionEvent?) {
        scheduler.isDeepExecution = getValue(Action.SELECTED_KEY) as Boolean
    }
}

/** Toggles the [Scheduler.isStopOnIssue] property. */
class StopOnIssueAction(
    private val scheduler: Scheduler = ExecutionModule.scheduler,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("simulator.action.stopOnIssue") {

    init {
        eventBus.register(StopOnIssueEvent::class, { updateState() })
        updateState()
    }

    override fun actionPerformed(e: ActionEvent?) {
        scheduler.isStopOnIssue = getValue(Action.SELECTED_KEY) as Boolean
    }

    private fun updateState() {
        putValue(Action.SELECTED_KEY, scheduler.isStopOnIssue)
        isEnabled = !scheduler.isActive
    }
}

class PrintScheduleAction(
        private val scheduler: Scheduler = ExecutionModule.scheduler
) : AbstractSchedulerAction("simulator.action.print") {

    override fun actionPerformed(e: ActionEvent?) {
        scheduler.printSchedule()
    }
}