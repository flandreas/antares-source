package ch.scorpion.jabbah.execution.scheduler

import ch.scorpion.jabbah.base.Issue
import ch.scorpion.jabbah.base.Status
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor

/**
 * A [Scheduler] receives requests of [Actor]s that want to be acting at a specific time in the future.
 * A [Scheduler] stores these requests in a time-based queue and schedules the [Actor]s for acting when their
 * time has come.
 */
interface Scheduler : SignalHandler {

    val signalHandler: SignalHandler

    val numberOfRemainingSlots: Int

    var isActive: Boolean

    override var isPaused: Boolean

	val isInBreakpoint: Boolean

	val isQueueEmpty: Boolean

    /** Determines whether this [Scheduler] stops execution when an [Issue] occurs while executing. */
    var isStopOnIssue: Boolean

	/**
	 * Determines whether this [Scheduler] updates [Status] with the relative simulation time event when not
	 * in stepping mode.
	 * </p>
	 * Normally, this feature is only enabled in stepping mode due to performance considerations.
	 * When setting this flag, the [Status] gets also updated when not in stepping mode.
	 */
	var isSimulationTimeStatusEnabled: Boolean

	var isSoftBreakpointsEnabled: Boolean

	fun dispose()

	/** Resumes simulation after it has been suspended by a breakpoint. */
	fun resume()

	/** Repeatedly called by a [SchedulerTask] to drive the execution. */
	fun execute(): ExecutionStepResult

    /**
     * Proceed executing scheduling requests until there are no more left, or until the specified execution time has
     * been reached.
     * @param time the relative execution time (in nanoseconds) to proceed to
     */
    fun proceedTo(time: Long)

    /** Prints the pending scheduling request to the INFO log. Should only be used on explicit demand when debugging. */
    fun printSchedule()
}

/**
 * Represents the result of a single execution step performed by a [Scheduler].
 * @property recalculated `true`if at least one [Actor] has been recalculated
 * @property breakpoint TODO Documentation
 */
data class ExecutionStepResult(val recalculated: Boolean, val breakpoint: Boolean)

/** Posted by a [Scheduler] during execution phase.*/
class SchedulerEvent(val type: Type, val scheduler: Scheduler, val actor: Actor) {

    enum class Type {
        /** An event of this type is sent after an [Actor] has requested scheduling*/
        REQUESTED,

        /** An event of this type is sent after acting has been done.*/
        DONE
    }
}

/** Posted by an executed system to temporarily suspend execution. */
class BreakEvent

/** Posted by a [Scheduler] when the execution depth changes.*/
data class ExecutionDepthEvent(val scheduler: Scheduler, val deepSimulation: Boolean)

/** Posted by a [Scheduler] when the [Scheduler.isStopOnIssue] property changes.*/
data class StopOnIssueEvent(val scheduler: Scheduler, val isStopOnIssue: Boolean)

/** Posted by a [Scheduler] when the [Scheduler.isSimulationTimeStatusEnabled] property changes.*/
data class SimulationTimeStatusEnabledEvent(val scheduler: Scheduler)

/** Posted by a [Scheduler] if execution had been stopped due to an [Issue]. */
data class ExecutionStoppedOnIssueEvent(val issue: Issue, val scheduler: Scheduler)

/** Posted by a [Scheduler] when its property [Scheduler.isSoftBreakpointsEnabled] has changed.*/
data class EnableSoftBreakpointsEvent(val scheduler: Scheduler)

/** Posted by a [Scheduler] when its property [Scheduler.isInBreakpoint] has changed.*/
data class BreakpointEvent(val scheduler: Scheduler)