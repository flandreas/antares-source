package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.ui.graphpanel.EditedGraphViewEvent

/**
 * Posted by [TestcaseView] on its [EventBus] when the user defines the current [Testcase]
 * by changing the selection in the [TestcaseView].
 */
data class TestcaseSelectionEvent(
	val graph: DigitalGraph,
	val testcase: Testcase?
)

interface TestcaseView : UIView {

	/** The [DigitalGraph] whose [Testcases] are displayed. */
	var graph: DigitalGraph?

	/**
	 * Asks the user for the name of a new [Testcase].
	 * @return `null` if the user cancelled the action
	 */
	fun getNewTestcaseName(): String?

	fun getDuplicateTestcaseName(): String?

	/** Asks the user to confirm deleting the current [Testcase]. */
	fun confirmDeleteTestcase(): Boolean
}

class TestcaseViewController(
	editor: Editor,
	val applicationDataHolder: ApplicationDataHolder,
	val applicationContextHolder: GraphApplicationContextHolder,
	val applicationModeHolder: ApplicationModeHolder,
	val service: TestcaseAppService = AntaresModelModule.testcaseAppService,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<TestcaseView>() {

	private val testcaseSelectionHandler: EventHandler<TestcaseSelectionEvent> = { handle(it) }

	var testcase: Testcase? = null
		private set

	private val editedGraphViewHandler: EventHandler<EditedGraphViewEvent> = {
		if (it.newGraphView == null || it.newGraphView?.graph is DigitalGraph) {
			graph = it.newGraphView?.graph as DigitalGraph
		}
	}

	val propertyPanelController = TestcasePropertyPanelController(editor, eventBus)

	var graph: DigitalGraph? = null
		set(value) {
			if (field != value) {
				field = value
				view.graph = value
				updateActions()
			}
		}

	val addAction = AddTestcaseAction(this)

	val deleteAction = DeleteTestcaseAction(this)

	val duplicateAction = DuplicateTestcaseAction(this)

	val metaAddAction: Action = MetaAddAction()

	val runSelectedTestcaseAction = RunTestcaseAction(this)

	val runAllTestcasesAction = RunCircuitTestcasesAction(this)

	init {
		eventBus.register(TestcaseSelectionEvent::class, testcaseSelectionHandler)
		eventBus.register(EditedGraphViewEvent::class, editedGraphViewHandler)
		updateActions()
	}

	override fun dispose() {
		super.dispose()
		propertyPanelController.dispose()
		eventBus.unregister(testcaseSelectionHandler)
		eventBus.unregister(editedGraphViewHandler)
	}

	fun addTestcase(name: String) {
		service.addTestcase(applicationDataHolder, Testcase(name))
	}

	fun deleteTestcase() {
		service.deleteTestcase(applicationDataHolder, testcase!!.id)
	}

	fun duplicateTestcase(name: String) {
		service.duplicateTestcase(applicationDataHolder, testcase!!.id, name)
	}

	private fun handle(event: TestcaseSelectionEvent) {
		testcase = event.testcase
		updateActions()
	}

	private fun updateActions() {
		addAction.updateEnabled()
		deleteAction.updateEnabled()
		duplicateAction.updateEnabled()
		runSelectedTestcaseAction.updateEnabled()
		runAllTestcasesAction.updateEnabled()
		metaAddAction.enabled = addAction.enabled
	}

	private inner class MetaAddAction : AbstractAction("antares.testcase.action.add", "/img/plus-18.png") {
		init {
			description = addAction.name
		}
		override fun execute(event: ActionEvent) {
			addAction.execute(ActionEvent(null, this, 0, "", 0))
		}
	}
}