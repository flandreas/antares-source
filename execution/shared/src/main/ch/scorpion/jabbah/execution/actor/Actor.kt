package ch.scorpion.jabbah.execution.actor

import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.scheduler.Scheduler

/**
 * Represents an object that can request a [Scheduler] to be scheduled for acting.
 */
interface Actor {

	/** An identification of this [Actor] primarily used for debugging and tracing.*/
	val id: Int

	/**
	 * The execution state of this [Actor].
	 * Maintained by this [Actor] itself. Primarily used by [ActorView].
	 */
	val state: ActorState

	val idle: Boolean get() = state == ActorState.Idle

	val waiting: Boolean get() = state == ActorState.Waiting

	val acting: Boolean get() = state == ActorState.Acting

	/** Holds the current [ExecutionError] of this [Actor], if any. */
	var executionError: ExecutionError?

	/**
	 * Returns the propagation delay (in nanoseconds), i.e. the time this [Actor] requires to recalculate
	 * its new state after its prerequisites have changed.
	 */
	var propagationDelay: Long

	/**
	 * Decides whether this [Actor] acts as breakpoint when [Scheduler] performs a stepwise execution.
	 * The typical implementation will return `true` if this [Actor] has registered [ActorListener], because
	 * this will break an execution only for [Actor] that are observed by a UI.
	 */
	val isBreakpoint: Boolean

	/** Adds the specified [ActorListener] to this [Actor].*/
	fun addActorListener(l: ActorListener)

	/** Removes the specified [ActorListener] from this [Actor].*/
	fun removeActorListener(l: ActorListener)

	/**
	 * Phase 1 of the start-up procedure.
	 * Called by the execution environment to give this [Actor] the opportunity to initialize its initial state,
	 * which is primarily setting default values of this [Actor].
	 * Called on all [Actor]s before any [executionStart] gets called.
	 */
	fun executionInitialize(signalHandler: SignalHandler)

	/**
	 * Phase 2 of the start-up procedure.
	 * Called by the execution environment after [executionInitialize] on all [Actor]s has been called.
	 * [Actor]s that depend on the state of other [Actor]s should request calculation by the [Scheduler].
	 */
	fun executionStart(signalHandler: SignalHandler)


	/**
	 * Called by execution environment} to let this [Actor] act.
	 *
	 * @param signalHandler the [SignalHandler] to be used to pass along to other [Actor]s.
	 * @param data the [ActorData] this [Actor] has provided when calling [SignalHandler.requestActingAfter].
	 */
	fun act(signalHandler: SignalHandler, data: ActorData)

	/**
	 * Called by a registered [ActorListener] after it has done its execution visualization.
	 *
	 * @param data must only be provided if [Actor] has a zero propagation, because in all other cases,
	 * [actingVisualized] will result in a call to [SignalHandler.actingDone], which has access to the
	 * [ActorData] of the queued scheduling request. Thus, an [ActorData] must only be provided in test scenarios,
	 * where [ActorListener]s are simulated and [actingVisualized] is called artificially. If there are
	 * no [ActorListener]s in real scenarios, an [Actor] will directly call [SignalHandler.actingDone]
	 * with the available [ActorData].
	 */
	fun actingVisualized(signalHandler: SignalHandler, l: ActorListener, data: ActorData? = null)

	/**
	 * Called by the [Scheduler] after an execution cycle has been completed. An [Actor] implementation can
	 * implement this method in order to update another [Actor] that depends on this [Actor], thereby starting
	 * another execution cycle.
	 */
	fun actingDone(signalHandler: SignalHandler, data: ActorData?)

	/**
	 * Called by the execution environment to give this [Actor] the opportunity to cleanup its state
	 * after overall execution has been stopped.
	 */
	fun executionStopped(signalHandler: SignalHandler)
}

enum class ActorState {

	/** An [Actor] is in this state when the simulation is not running.*/
	NonExecuting,

	/** The simulation is running, but the [Actor] has not yet requested execution.*/
	Idle,

	/** The [Actor] has requested execution and is waiting to be scheduled for acting.*/
	Waiting,

	/**
	 * The [Actor] has been scheduled for acting and is waiting until all its [ActorListener] have completed
	 * visualizing the acting, and the [Scheduler] has confirmed that acting is done.
	 */
	Acting
}