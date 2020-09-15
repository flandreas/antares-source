package ch.scorpion.jabbah.execution.scheduler

/**
 * Represents the running state of a [Scheduler].
 */
enum class SchedulerRunningState {

    /** The [Scheduler] is running as fast as possible.*/
    RUNNING,

    /** The [Scheduler] is paused, i.e. in single step mode*/
    PAUSED;

	companion object {
		fun ofPausedFlag(paused: Boolean): SchedulerRunningState
			= if (paused) PAUSED else RUNNING
	}
}

/** An event being posted on a [Scheduler]'s [EventBus] whenever its [SchedulerRunningState] changes.*/
data class SchedulerRunningStateEvent(val scheduler: Scheduler)