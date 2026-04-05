package io.antarescircuit.jabbah.execution.scheduler

import io.antarescircuit.jabbah.base.event.EventBus

/**
 * Represents the activation state of a [Scheduler].
 */
enum class SchedulerActivationState {

    /** The [Scheduler] has been started.*/
    ACTIVE,

    /** The [Scheduler] has not yet been started, or has been stopped again.*/
    PASSIVE
}

/**
 * An event being posted on a [Scheduler]'s [EventBus] before it will become [SchedulerActivationState.ACTIVE],
 * and after it has become [SchedulerActivationState.PASSIVE], excluding all necessary setup and tear-down activities.
 */
data class SchedulerActivationStatePreparationEvent(val scheduler: Scheduler)

/** An event being posted on a [Scheduler]'s [EventBus] immediately after its [SchedulerActivationState] has changed.*/
data class SchedulerActivationStateEvent(val scheduler: Scheduler)