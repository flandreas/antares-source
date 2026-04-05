package io.antarescircuit.jabbah.execution.scheduler

import io.antarescircuit.jabbah.base.Issue
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.Status
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.base.time.SystemSpeedPauseEvent
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.speed.SystemSpeedCategory
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.execution.ExecutionError

/**
 * A [Scheduler] receives requests of [Actor]s that want to be acting at a specific time in the future.
 * A [Scheduler] stores these requests in a time-based queue and schedules the [Actor]s for acting when their
 * time has come.
 *
 * Listens for [SystemSpeedPauseEvent]s from the [SystemSpeed] to pause and resume this [Scheduler].
 *
 * [Scheduler] features a breakpoint model. When execution runs into a breakpoint, execution is suspended,
 * until the user manually resumed it. Two kinds of breakpoints are supported:
 * - Soft breakpoints: Implicit breakpoints (if enabled) are set whenever an [Actor] produces a new output signal
 * - Hard breakpoints: Explicit breakpoints produced by special [Actor]s that send a [BreakEvent]
 */
interface Scheduler : SignalHandler {

	companion object {
		/** The custom name [String] of the limit [SystemSpeedCategory] in [Properties] for sending [SchedulerEvent]s.*/
		const val PROP_SCHEDULER_EVENT_SYSTEM_SPEED_LIMIT = "execution.scheduler.eventSystemSpeedLimit"
	}

    val numberOfRemainingSlots: Int

    var isActive: Boolean

    override var isSingleStepMode: Boolean

	val isInBreakpoint: Boolean

	val isQueueEmpty: Boolean

    /** Determines whether this [Scheduler] stops execution when an [Issue] occurs while executing. */
    var isStopOnIssue: Boolean

	/**
	 * Determines whether this [Scheduler] updates [Status] with the relative simulation time event when not
	 * in stepping mode.
	 * </p>
	 * Normally, this feature is only enabled in stepping mode due to performance considerations.
	 * When setting this flag, the [Status] gets also updated when not in stepping mode, but only
	 * if [SystemSpeed] is not greater than [SystemSpeedCategory.Observe].
	 */
	var isSimulationTimeStatusEnabled: Boolean

	/** Determines whether soft breakpoints are enabled.*/
	var isSoftBreakpointsEnabled: Boolean

	/**
	 * Determines whether there is a deferred [ExecutionError] pending for possible resolution,
	 * or leading to a simulation error if it can't be resolved.
	 */
	val hasDeferredExecutionError: Boolean

	/**
	 * The time (in nanoseconds) after simulation start during which soft breakpoints are not yet active.
	 * Can be set by the main [Actor] during simulation initialization. Is reset by this [Scheduler] after
	 * simulation has stopped.
	 * */
	var softBreakpointsArmTime: Long

	fun dispose()

	fun addListener(listener: SchedulerListener)

	fun removeListener(listener: SchedulerListener)

	fun notifyListeners(source: Any)

	/** Repeatedly called by a [SchedulerTask] to drive the execution. */
	fun execute(): ExecutionStepResult

    /**
     * Proceed executing scheduling requests until there are no more left, or until the specified execution time has
     * been reached. Mainly used for testing.
     * @param time the relative execution time (in nanoseconds) to proceed to
     */
    fun proceedTo(time: Long, maxIteration: Int = 1_000)

    /** Prints the pending scheduling request to the INFO log. Should only be used on explicit demand when debugging. */
    fun printSchedule()

	/**
	 * Resets [executionTime] to 0.
	 * @throws IllegalStateException if the simulation queue is not empty
	 */
	fun resetExecutionTime()
}

/**
 * Represents the result of a single execution step performed by a [Scheduler].
 * @property recalculated `true`if at least one [Actor] has been recalculated
 * @property breakpoint TODO Documentation
 */
data class ExecutionStepResult(val recalculated: Boolean, val breakpoint: Boolean) {
	companion object {
		val NOT_RECALCULATED_NO_BREAKPOINT = ExecutionStepResult(recalculated = false, breakpoint = false)
		val NOT_RECALCULATED_BREAKPOINT = ExecutionStepResult(recalculated = false, breakpoint = true)
		val RECALCULATED_NO_BREAKPOINT = ExecutionStepResult(recalculated = true, breakpoint = false)
	}
}

/** Posted by a [Scheduler] during execution phase.*/
class SchedulerEvent(
	val scheduler: Scheduler,
	val source: Any
)

fun interface SchedulerListener {
	fun handle(event: SchedulerEvent)
}

/**
 * An event being posted on a [Scheduler]'s [EventBus] whenever its
 * [Scheduler.isSingleStepMode] property changes.
 */
data class SchedulerSingleStepModeEvent(val scheduler: Scheduler)

/** Posted by an executed system to temporarily suspend execution. */
class BreakEvent

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