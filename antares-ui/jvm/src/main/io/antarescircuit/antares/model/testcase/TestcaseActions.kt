package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.ui.AbstractApplicationDataEditModeAction

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

class DuplicateTestcaseAction(
	controller: TestcaseViewController,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractTestcaseAction(controller, "antares.testcase.action.duplicate", eventBus ) {

	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		controller.view.getDuplicateTestcaseName()?.let {
			controller.duplicateTestcase(it)
		}
	}

}