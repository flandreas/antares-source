package io.antarescircuit.jabbah.base.ui.wizard

import io.antarescircuit.jabbah.base.event.ActionEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WizardControllerTest {

	private data class Context(var branch: Boolean = false, var visited: List<String> = emptyList()) : WizardContext

	private class Step(
		override val titleKey: String,
		private val terminal: Boolean = false,
		private val invalid: Boolean = false,
		private val successor: (Context) -> WizardStepModel<Context>? = { null },
	) : WizardStepModel<Context> {
		override val isTerminal: Boolean get() = terminal
		override fun validate(context: Context): WizardValidationResult =
			if (invalid) WizardValidationResult.Invalid("wizard.invalid") else WizardValidationResult.Valid
		override fun next(context: Context): WizardStepModel<Context> = successor(context)!!
	}

	private class View : WizardView {
		var updateCount = 0

		override fun update() {
			updateCount++
		}

		override fun dispose() { }
	}

	@Test
	fun shouldCollectContextAcrossBranchingSteps() {
		lateinit var branch: Step
		val end = Step("end", terminal = true)
		branch = Step("branch", successor = { end })
		val start = Step("start", successor = { if (it.branch) branch else end })
		val context = Context(branch = true)
		val controller = WizardController(start, context)

		assertTrue(controller.next())
		assertEquals("branch", controller.currentStep.titleKey)
		assertTrue(controller.next())
		assertEquals("end", controller.currentStep.titleKey)
		assertTrue(controller.finish() === context)
	}

	@Test
	fun shouldBlockNavigationWhenValidationFails() {
		val end = Step("end", terminal = true)
		val controller = WizardController(Step("start", invalid = true, successor = { end }), Context())

		assertFalse(controller.next())
		assertEquals("wizard.invalid", controller.validationError?.messageKey)
		assertEquals("start", controller.currentStep.titleKey)
	}

	@Test
	fun shouldNavigateBackThroughVisitedSteps() {
		val end = Step("end", terminal = true)
		val middle = Step("middle", successor = { end })
		val controller = WizardController(Step("start", successor = { middle }), Context())

		controller.next()
		controller.next()
		assertTrue(controller.back())
		assertEquals("middle", controller.currentStep.titleKey)
		assertTrue(controller.back())
		assertEquals("start", controller.currentStep.titleKey)
		assertFalse(controller.back())
	}

	@Test
	fun shouldReturnNoResultWhenCancelled() {
		val controller = WizardController(Step("start", terminal = true), Context())

		controller.cancel()
		assertTrue(controller.cancelled)
		assertNull(controller.finish())
	}

	@Test
	fun shouldUpdateMvcViewWhenControllerStateChanges() {
		val end = Step("end", terminal = true)
		val controller = WizardController(Step("start", successor = { end }), Context())
		val view = View()

		controller.view = view
		assertEquals(1, view.updateCount)
		controller.next()
		assertEquals(2, view.updateCount)
	}

	@Test
	fun shouldExposeNavigationAsActions() {
		val end = Step("end", terminal = true)
		val controller = WizardController(Step("start", successor = { end }), Context())
		val event = ActionEvent(null, controller, 0, "", 0)

		assertFalse(controller.backAction.enabled)
		assertTrue(controller.nextAction.enabled)
		assertFalse(controller.finishAction.enabled)
		assertTrue(controller.cancelAction.enabled)

		controller.nextAction.execute(event)
		assertTrue(controller.backAction.enabled)
		assertFalse(controller.nextAction.enabled)
		assertTrue(controller.finishAction.enabled)

		controller.finishAction.execute(event)
		assertTrue(controller.completed)
		assertFalse(controller.cancelAction.enabled)
	}
}
