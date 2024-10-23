package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import kotlin.reflect.KClass

/**
 * The part of a [Scheduler] that is passed to [Actor]s in order to access the necessary part of the
 * scheduling functionality.
 */
interface SignalHandler : ExecutionErrorHandler {

	/**
	 * The [EventBus] on which this [SignalHandler] posts event related to the state of the [SignalHandler],
	 * or the [Scheduler] respectively.
	 */
	val eventBus: EventBus

	val systemSpeedCategory: CurrentSystemSpeedCategory

	/**
     * Determines whether the current execution environment performs deep execution of [Actor],
     * i.e. whether execution scripts of nested [Actor]s are ignored.
     */
    var isDeepExecution: Boolean

	/**
	 * Determines whether the current execution environment is in single-step mode.
	 */
	val isSingleStepMode: Boolean

    /** Returns the relative execution time, i.e. the relative time in nanoseconds since execution has been started.*/
    val executionTime: Long

	val isLogTrace: Boolean

	/**
	 * An optional object set by the main object being executed when execution is started.
	 * [Actors][Actor] to which this [SignalHandler] gets passed in all relevant methods
	 * can then access this context object.
	 * The concrete type of the context is determined by higher-level modules.
	 */
	var executionContext: Any?

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
	 * An [Actor] can act by itself even if the propagation delay with which it requested acting
	 * with the [Scheduler] is not yet over. A typical application of this are composed [Actor]s
	 * that defined their "end of acting" not in terms of a propagation delay, but by producing
	 * a changed output (at which time that ever might be).
	 */
	fun actPrematurely(actor: Actor, data: ActorData?)

    /**
     * Informs this [SignalHandler] that the acting of the specified [Actor] is done.
     *
     * @param actor the [Actor] that has done acting
     * @param data the [ActorData] to forward to the [Actor]. This is only required when calling this [SignalHandler]
     * in testing scenarios. In all other scenarios, the [ActorData] is known by the system itself.
     */
    fun actingDone(actor: Actor, data: ActorData?)
}