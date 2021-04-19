package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.execution.issue.Issue
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.actor.Actor

/**
 * Represents an error that has occurred in an [Actor] during execution by a [Scheduler].
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
	 * @return `true` if this [ExecutionError] has been reevaluated and can therefore be deleted by
	 * the [Scheduler], or `false` if reevaluation was not yet possible, and this [ExecutionError]
	 * should still be kept by the [Scheduler].
	 */
	fun reevaluated(signalHandler: SignalHandler): Boolean
}