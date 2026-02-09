package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.AbstractApplicationDataEditModeAction

abstract class AbstractTestcaseAction(
	protected val controller: TestcaseViewController,
	baseName: String,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractApplicationDataEditModeAction(baseName, controller.applicationDataHolder, controller.applicationModeHolder, eventBus) {

	override fun calculateEnabled(): Boolean = super.calculateEnabled() && isDigital

	private val isDigital: Boolean get() {
		val state = controller.applicationDataHolder.getUndoableState()
		return state is MetaGraph && state.graph.graphView.graph is DigitalGraph
	}
}

class AddTestcaseAction(
	controller: TestcaseViewController,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractTestcaseAction(controller, "antares.testcase.action.add", eventBus) {

	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		controller.view.getNewTestcaseName()?.let {
			controller.addTestcase(it)
		}
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && controller.testcase == null
}

class DeleteTestcaseAction(
	controller: TestcaseViewController,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractTestcaseAction(controller, "antares.testcase.action.delete", eventBus) {

	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		if (controller.view.confirmDeleteTestcase()) {
			controller.deleteTestcase()
		}
	}
}