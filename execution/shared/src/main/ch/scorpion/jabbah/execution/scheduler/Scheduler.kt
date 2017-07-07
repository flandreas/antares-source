package ch.scorpion.jabbah.execution.scheduler

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

    var isPaused: Boolean

    fun step()

    /**
     * Proceed executing scheduling requests until there are no more left, or until the specified execution time has
     * been reached.
     * @param time the relative execution time (in nanoseconds) to proceed to
     */
    fun proceedTo(time: Long)
}

/** Posted by a [Scheduler] during execution phase.*/
class SchedulerEvent(val type: Type, val scheduler: Scheduler, val actor: Actor) {

    enum class Type {
        /** An event of this type is sent after an [Actor] has requested scheduling*/
        REQUESTED,

        /** An event of this type is sent after acting has been done.*/
        DONE
    }
}

/** Posted by a [Scheduler] after an execution cycle.*/
class SchedulerStateEvent(val numberOfRemainingSlots: Int, val relativeTime: Long)

/** Posted by a [Scheduler] when the execution depth changes.*/
class ExecutionDepthEvent(val scheduler: Scheduler, val deepSimulation: Boolean)