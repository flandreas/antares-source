package ch.scorpion.jabbah.execution.actor

import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.scheduler.Scheduler

/**
 * Represents an object that can request a [Scheduler] to be scheduled for acting.
 */
interface Actor {

	/** An identification of this [Actor] primarely used for debugging and tracing.*/
	val id: Int

	/** The execution state of this [Actor].*/
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
	 * Called by execution environment} to let this [Actor] act.
	 *
	 * @param signalHandler the [SignalHandler] to be used to pass along to other [Actor]s.
	 * @param data the [ActorData] this [Actor] has provided when calling [SignalHandler.requestActingAfter].
	 * @return `true` if execution has been completed, `false` if this [Actor] has more to do
	 *         and will call [SignalHandler.actingDone] after completion.
	 */
	fun act(signalHandler: SignalHandler, data: ActorData): Boolean

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
	NonExecuting,
	Idle,
	Waiting,
	Acting
}

open class ActorImpl(
	override val id: Int = 0,
	override var propagationDelay: Long = 0
) : Actor {

	/** Manages [Actor] behaviour on behalf of this [Actor].*/
	private val actorSupport: ActorSupport by lazy { ActorSupport(this) }

	private var _state: ActorState = ActorState.NonExecuting

	/** ---- [Actor] interface */

	override var executionError: ExecutionError? = null

	override val state: ActorState get() = _state

	override val isBreakpoint: Boolean get() = actorSupport.hasListeners

	override fun addActorListener(l: ActorListener) {
		actorSupport.addListener(l)
	}

	override fun removeActorListener(l: ActorListener) {
		actorSupport.removeListener(l)
	}

	override fun executionStarted(signalHandler: SignalHandler) {
		_state = ActorState.Idle
		executionError = null

	}

	override fun act(signalHandler: SignalHandler, data: ActorData): Boolean {
		_state = ActorState.Acting
		return actorSupport.notifyActed(signalHandler, data)
	}

	override fun actingVisualized(signalHandler: SignalHandler, l: ActorListener, data: ActorData?) {
		actorSupport.actingVisualized(signalHandler, l, data)
	}

	override fun actingDone(signalHandler: SignalHandler, data: ActorData?) {
		_state = ActorState.Idle
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		executionError = null
		_state = ActorState.NonExecuting
	}

	/** ---- [AbstractActor] */

	protected fun requestActingAfter(signalHandler: SignalHandler, delay: Long, data: ActorData) {
		_state = ActorState.Waiting
		actorSupport.requestActingAfter(signalHandler, delay, data)
	}

	protected fun requestActingTimeFreeze(signalHandler: SignalHandler, data: ActorData) {
		_state = ActorState.Waiting
		actorSupport.requestActingTimeFreeze(signalHandler, data)
	}

	fun notifyActed(signalHandler: SignalHandler, data: ActorData): Boolean {
		return actorSupport.notifyActed(signalHandler, data)
	}
}