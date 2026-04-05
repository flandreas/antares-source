package io.antarescircuit.jabbah.execution

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.event.PropertyChangeEvent
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.time.SystemSpeedPauseEvent
import io.antarescircuit.jabbah.execution.scheduler.BreakpointEvent
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.scheduler.SchedulerActivationStateEvent
import kotlin.js.JsExport
import kotlin.properties.Delegates

/**
 * Pauses the running [Scheduler], or resumes it if already paused.
 * Fires a [PropertyChangeEvent] with name [PROP_PAUSE_OR_RESUME_ACTION_IN_BREAKPOINT] when [inBreakpoint] changes.
 */
@JsExport
interface PauseOrResumeAction : Action {
    val inBreakpoint: Boolean
}

@JsExport
const val PROP_PAUSE_OR_RESUME_ACTION_IN_BREAKPOINT = "inBreakpoint"

class PauseOrResumeActionImpl(
    private val scheduler: Scheduler,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.pause", eventBus), PauseOrResumeAction {

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
            updateInBreakpoint()
        }
    }

    /** Set if the [scheduler] is suspended due to having run into a breakpoint.*/
    override var inBreakpoint: Boolean by Delegates.observable(false) { _, old, new ->
        changeSupport.fire(PROP_PAUSE_OR_RESUME_ACTION_IN_BREAKPOINT, old, new)
    }
        private set

    init {
        eventBus.register(SystemSpeedPauseEvent::class, pausedHandler)
        eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
        eventBus.register(BreakpointEvent::class, breakpointHandler)

        updateState()
        updateSelected()
        updateInBreakpoint()
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
        updateDescription()
    }

    private fun updateDescription() {
        description = if (selected) {
            if (inBreakpoint) {
                Translations.getString("execution.action.resumeFromBreakpoint.desc")
            } else {
                Translations.getString("execution.action.resume.desc")
            }
        } else {
            Translations.getString("execution.action.pause.desc")
        }
    }

    private fun updateState() {
        enabled = scheduler.isActive
    }

    private fun updateInBreakpoint() {
        inBreakpoint = scheduler.isInBreakpoint
        updateDescription()
    }
}