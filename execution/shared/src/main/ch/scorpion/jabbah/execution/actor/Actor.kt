package ch.scorpion.jabbah.execution.actor

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.scheduler.Scheduler

/**
 * Represents an object that can request a [Scheduler] to be scheduled for acting.
 */
interface Actor {

    val id: Int

    /** The execution state of this [Actor].*/
    val state: ActorState

    val idle: Boolean get() = state == ActorState.Idle

    val waiting: Boolean get() = state == ActorState.Waiting

    val acting: Boolean get() = state == ActorState.Acting

    /**
     * Returns the propagation delay (in nanoseconds), i.e. the time this [Actor] requires to recalculate
     * its new state after its prerequisites have changed.
     */
    var propagationDelay: Long

    /** Determines whether this [Actor] acts as breakpoint when [Scheduler] performs a stepwise execution.*/
    val isBreakpoint: Boolean

    /** Adds the specified [ActorListener] to this [Actor].*/
    fun addActorListener(l: ActorListener)

    /** Removes the specified [ActorListener] from this [Actor].*/
    fun removeActorListener(l: ActorListener)

    /**
     * Called by the execution environment to give this [Actor] the opportunity to initialize its state
     * after overall execution has been started.
     */
    fun executionStarted(signalHandler: SignalHandler)

    /**
     * Called by execution environment} to let this [Actor] act
     * .
     * @param signalHandler the [SignalHandler] to be used to pass along to other [Actor]s.
     * @param data the [ActorData] this [Actor] has provided when calling [SignalHandler.requestActingAfter].
     * @return `true` if execution has been completed, `false` if this [Actor] has more to do
     *         and will call [SignalHandler.actingDone] after completion.
     */
    fun act(signalHandler: SignalHandler, data: ActorData): Boolean

    /** Called by a registered [ActorListener] after it has done its execution visualization.*/
    fun actingVisualized(signalHandler: SignalHandler, l: ActorListener)

    /**
     * Called by the [Scheduler] after an execution cycle has been completed. An [Actor] implementation can
     * implement this method in order to update another [Actor] that depends on this [Actor], thereby starting
     * another execution cycle.
     */
    fun actingDone(signalHandler: SignalHandler, data: ActorData)

    /**
     * Called by the execution environment to give this [Actor] the opportunity to cleanup its state
     * after overall execution has been stopped.
     */
    fun executionStopped(signalHandler: SignalHandler)
}

enum class ActorState {
    NonExecuting,
    Idle,
    Waiting,
    Acting
}