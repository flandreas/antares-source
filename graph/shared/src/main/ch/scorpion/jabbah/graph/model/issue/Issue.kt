package ch.scorpion.jabbah.graph.model.issue

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.graph.model.Graph

/**
 * An [Issue] represents a situation that requires the attention of the user.
 * For example, this could be an error that occurs during the execution of a [Graph] due to a buggy JavaScript snippet.
 * [Issues] are raised by posting them on [EventBus], on which interested consumer listen for them.
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
     * can indicate whether the error is the condition, the entry or the exit script.
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
    Error
}