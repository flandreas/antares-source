package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.scheduler.Scheduler

/**
 * Represents an error that has occurred in an [Actor] during execution by a [Scheduler].
 * [ExecutionError]s are registered with an [ExecutionErrorHandler] for handling.
 */
interface ExecutionError {

	/** The execution time (in ns) when this [ExecutionError] has been created.*/
	val creationTime: Long

	/**
	 * Returns the HTML formatted text to be displayed in the tooltip for the object from
	 * which this [ExecutionError] originates.
	 */
	val tooltipText: String

	/**
	 * Check if the reason for this [ExecutionError] still exists, and if that's the case,
	 * escalate it, e.g. by creating an [Issue] to inform the user.
	 *
	 * @param force `true` if this call is possibly the last one, because the execution queue is
	 * empty and there could be nothing more going on that could change the state of the executed system
	 *
	 * @return `true` if this [ExecutionError] has been reevaluated and can therefore be deleted by
	 * the [ExecutionErrorHandler], or `false` if reevaluation was not yet possible, and this [ExecutionError]
	 * should still be kept by the [ExecutionErrorHandler].
	 */
	fun reevaluated(force: Boolean, signalHandler: SignalHandler): Boolean
}

interface ExecutionErrorHandler {

	/** The number of pending [ExecutionError]s. */
	val executionErrorCount: Int

	/**
	 * Registers and defers handling of the specified [ExecutionError] until the end of the current
	 * execution cycle.
	 *
	 * Some [ExecutionError]s can occur during the quasi-parallel execution of [Actor] within the same
	 * execution cycle, which can lead to race conditions and dependencies on the order in which [Actor]s
	 * are execution during that same execution cycle. Therefore, defer such [ExecutionError] until
	 * the execution cycle has ended, and then re-evaluate whether their cause is still present, or whether is has
	 * solved by the execution of other [Actor]s.
	 */
	fun deferExecutionError(error: ExecutionError)
}

/**
 * Base implementation of [ExecutionError].
 * @param gracePeriod the duration in ns of how long [reevaluated] returns `false` even
 * if this [ExecutionError] has not been resolved.
 */
abstract class AbstractExecutionError(
	override val creationTime: Long,
	private val gracePeriod: Int = 0
) : ExecutionError {

	private fun getAge(executionTime: Long): Long = executionTime - creationTime

	protected fun isGracePeriodOver(executionTime: Long): Boolean = getAge(executionTime) > gracePeriod
}