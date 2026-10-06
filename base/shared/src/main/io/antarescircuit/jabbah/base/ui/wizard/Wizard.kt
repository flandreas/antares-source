package io.antarescircuit.jabbah.base.ui.wizard

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView

/** The platform-specific view controlled by [WizardController]. */
interface WizardView : UIView {

	/** Refreshes the current step, validation message, and completion state. */
	fun update()
}

/**
 * Platform-neutral controller for a branching wizard.
 *
 * The supplied context is owned by the wizard while it is running and is returned by
 * [finish] after the terminal step validates successfully.
 */
class WizardController<C : WizardContext>(
	initialStep: WizardStepModel<C>,
	val context: C,
	val supportFinish: Boolean = true
) : AbstractUIController<WizardView>() {

	private val history = ArrayDeque<WizardStepModel<C>>()
	private var isViewInitialized = false

	var currentStep: WizardStepModel<C> = initialStep
		private set

	var validationError: WizardValidationResult.Invalid? = null
		private set

	var completed: Boolean = false
		private set

	var cancelled: Boolean = false
		private set

	var result: C? = null
		private set

	val canGoBack: Boolean get() = history.isNotEmpty() && !isClosed
	val canGoNext: Boolean get() = !currentStep.isTerminal && !isClosed
	val canFinish: Boolean get() = supportFinish && currentStep.isTerminal && !isClosed

	private val isClosed: Boolean get() = completed || cancelled

	val backAction: Action = wizardAction("base.wizard.back") { back() }
	val nextAction: Action = wizardAction("base.wizard.next") { next() }
	val cancelAction: Action = if (supportFinish) {
		wizardAction("base.wizard.cancel") { cancel() }
	} else {
		wizardAction("base.wizard.close") { cancel() }
	}
	val finishAction: Action = wizardAction("base.wizard.finish") { finish() }

	override var view: WizardView
		get() = super.view
		set(value) {
			super.view = value
			isViewInitialized = true
			value.update()
		}

	init {
		updateActions()
	}

	fun next(): Boolean {
		if (!canGoNext) {
			return false
		}
		if (!validateCurrentStep()) {
			return false
		}

		val nextStep = currentStep.next(context)
			?: throw IllegalStateException("Non-terminal wizard step returned no successor")
		history.addLast(currentStep)
		currentStep = nextStep
		validationError = null
		notifyStateChanged()
		return true
	}

	fun back(): Boolean {
		if (!canGoBack) {
			return false
		}
		currentStep = history.removeLast()
		validationError = null
		notifyStateChanged()
		return true
	}

	fun finish(): C? {
		if (!canFinish || !validateCurrentStep()) {
			return null
		}
		result = context
		completed = true
		notifyStateChanged()
		return result
	}

	fun cancel() {
		if (!isClosed) {
			cancelled = true
			notifyStateChanged()
		}
	}

	private fun notifyStateChanged() {
		updateActions()
		if (isViewInitialized) {
			view.update()
		}
	}

	private fun updateActions() {
		backAction.enabled = canGoBack
		nextAction.enabled = canGoNext
		cancelAction.enabled = !isClosed
		finishAction.enabled = canFinish
	}

	private fun wizardAction(baseName: String, handler: () -> Unit): Action =
		object : AbstractAction(baseName, null, false) {
			override fun execute(event: ActionEvent) = handler()
		}

	private fun validateCurrentStep(): Boolean {
		val result = currentStep.validate(context)
		validationError = result as? WizardValidationResult.Invalid
		if (validationError != null) {
			notifyStateChanged()
		}
		return result === WizardValidationResult.Valid
	}
}
