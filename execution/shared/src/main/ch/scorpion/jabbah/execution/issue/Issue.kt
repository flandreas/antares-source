package ch.scorpion.jabbah.execution.issue

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent

/**
 * An [Issue] represents a situation that requires the attention of the user.
 * For example, this could be an error that occurs during execution due to a buggy JavaScript snippet.
 * [Issue]s are raised by posting them on [EventBus], on which interested consumer listen for them.
 */
interface Issue {

	/** Represents the severity of this [Issue]. */
	val severity: IssueSeverity

	/** The name or a short text that serves an overview description of this [Issue]. */
	val name: String

	/** A longer description containing details about the cause of this [Issue]. */
	val description: String?

	/**
	 * Information about the object from which this [Issue] originates. Will be used by the user to navigate
	 * to that origin and fix the cause of the [Issue].
	 */
	val origin: String

	/**
	 * The optional context that the source of the [Issue] more precisely than [origin].
	 * For example, if the [origin] of a JavaScript error is a particular scenario step, the [context]
	 * can indicate whether the error is in the condition, in the entry script or in the exit script.
	 */
	val context: String?

}

/**
 * Standard implementation of the [Issue] interface.
 * We don't know yet whether we need additional logic in different [Issue] implementations, such as automatically
 * navigation to the origin of the [Issue]. If not, we can drop the interface and go directly with a data class.
 */
data class IssueImpl(
	override val severity: IssueSeverity,
	override val name: String,
	override val description: String?,
	override val origin: String,
	override val context: String?
) : Issue

enum class IssueSeverity {
	Warning,
	Error;

	override fun toString(): String {
		return when (this) {
			Warning -> Translations.getString("issue.severity.warning.name")
			Error -> Translations.getString("issue.severity.error.name")
		}
	}
}

/**
 * Collects [Issue]s posted on [EventBus] to provide them to the rest of the system for displaying and resolving.
 * Posts an [IssueCollectorEvent] whenever a new [Issue] had been collected, or when the collected [Issue]s
 * had been cleared.
 */
class IssueCollector(
	clearOnExecutionStart: Boolean = true,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	init {
		if (clearOnExecutionStart) {
			eventBus.register(SchedulerActivationStateEvent::class) {
				if (it.scheduler.isActive) {
					clear()
				}
			}
		}
		eventBus.register(IssueImpl::class) { handleNewIssue(it) }
	}

	/** Holds all collected [Issue]s. */
	private val _issues = mutableListOf<Issue>()

	/** Returns the number of collected [Issue]s. */
	val size: Int get() = _issues.size

	/** Returns the collected [Issue]s in the order they occurred. */
	val issues: List<Issue> get() = _issues

	/** Removes all collected [Issue]s. */
	fun clear() {
		_issues.clear()
		eventBus.post(IssueCollectorEvent(this, null))
	}

	/** Returns the [Issue] at the specified index.*/
	fun getIssue(index: Int): Issue = _issues.get(index)

	private fun handleNewIssue(issue: Issue) {
		_issues.add(issue)
		eventBus.post(IssueCollectorEvent(this, issue))
	}
}

/**
 * Posted by [IssueCollector] on its [EventBus] whenever a new [Issue] had been collected,
 * or when the collected [Issue]s had been cleared (in which case [issue] is `null`.
 */
data class IssueCollectorEvent(val issueCollector: IssueCollector, val issue: Issue?)