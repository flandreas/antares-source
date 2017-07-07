package ch.scorpion.jabbah.execution.scheduler

/**
 * Represents the activation state of a [Scheduler].
 */
enum class SchedulerActivationState {

    /** The [Scheduler] has been startet.*/
    ACTIVE,

    /** The [Scheduler] has not yet been started, or has been stopped again.*/
    PASSIVE
}

/** An event being posted on a [Scheduler]'s [EventBus] whenever its [SchedulerActivationState] has changed.*/
data class SchedulerActivationStateEvent(val scheduler: Scheduler)