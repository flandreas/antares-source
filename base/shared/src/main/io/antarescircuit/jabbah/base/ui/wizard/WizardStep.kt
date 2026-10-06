package io.antarescircuit.jabbah.base.ui.wizard

import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView

/**
 * A node in a wizard's navigation graph.
 *
 * Implementations should read and update [WizardContext] in [validate] and [next].
 * A terminal step must return `true` from [isTerminal].
 */
interface WizardStepModel<C : WizardContext> {

    /** Translation key for the step title. */
    val titleKey: String

    /** Whether this is a step from which the wizard can be finished. */
    val isTerminal: Boolean

    fun validate(context: C): WizardValidationResult

    /** Returns the next step after successful validation, or `null` for a terminal step. */
    fun next(context: C): WizardStepModel<C>?
}

abstract class AbstractWizardStepModel<C : WizardContext>(
    override val titleKey: String,
    override val isTerminal: Boolean = false
) : WizardStepModel<C> {

    override fun validate(context: C): WizardValidationResult = WizardValidationResult.Valid
}

/** A platform-specific view associated with a wizard step. */
interface WizardStepView : UIView

/** Optional controller base for applications that want MVC controllers per wizard step. */
abstract class AbstractWizardStepController<C : WizardContext, V : WizardStepView>(
    val model: WizardStepModel<C>,
) : AbstractUIController<V>()

/** The result of validating a wizard step. */
sealed class WizardValidationResult {

    /** Indicates that the current step can be left. */
    data object Valid : WizardValidationResult()

    /** A translated message explaining why the current step cannot be left. */
    data class Invalid(
        val messageKey: String,
        val arguments: List<Any> = emptyList(),
    ) : WizardValidationResult()
}
