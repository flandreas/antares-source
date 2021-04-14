package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import kotlin.reflect.KClass

/**
 * The part of a [Scheduler] that is passed to [Actor]s in order to access the necessary part of the
 * scheduling functionality.
 */
interface SignalHandler {

    /**
     * Determines whether the current execution environment performs deep execution of [Actor],
     * i.e. whether execution scripts of nested [Actor]s are ignored.
     */
    var isDeepExecution: Boolean

	/**
	 * Determines whether the current execution environment is in single-step mode.
	 */
	val isPaused: Boolean

    /** Returns the relative execution time, i.e. the relative time in nanoseconds since execution has been started.*/
    val executionTime: Long

    /** Creates a trace log entry for tracing signal propagation. This allows central trace enabling/disabling.*/
    fun logTrace(clazz: KClass<*>, id: Int, msg: () -> String)

	/** Creates a trace log entry for tracing signal propagation. This allows central trace enabling/disabling.*/
	fun logActorTrace(actor: Actor, msg: () -> String)

    /**
     * Asks this [SignalHandler] to recalculate the specified [Actor] after a given delay.
     *
     * @param actor the [Actor] to be recalculated after `delay` ns
     * @param delay the delay in nanoseconds
     * @param data the [ActorData] to be returned in [Actor.act]
     */
    fun requestActingAfter(actor: Actor, delay: Long, data: ActorData)

    /**
     * Informs this [SignalHandler] that the acting of the specified [Actor] is done.
     *
     * @param actor the [Actor] that has done acting
     * @param data the [ActorData] to forward to the [Actor]. This is only required when calling this [SignalHandler]
     * in testing scenarios. In all other scenarios, the [ActorData] is known by the system itself.
     */
    fun actingDone(actor: Actor, data: ActorData?)

	/**
	 * Registers and defers handling of the specified [ExecutionError] until the end of the current
	 * execution cycle.
	 *
	 * Some [ExecutionError] can occur during the quasi-parallel execution of [Actor] within the same
	 * execution cycle, which can lead to race conditions and dependencies on the order in which [Actor]s
	 * are execution during that same execution cycle. Therefore, defer such [ExecutionError] until
	 * the execution cycle has ended, and then check whether their cause is still present, or whether is has
	 * solved by the execution of other [Actor]s.
	 */
	fun deferExecutionError(error: ExecutionError)

}