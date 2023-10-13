package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.testcase.Testcase
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
}

class TestcaseViewController(
	editor: Editor,
	val applicationContextHolder: GraphApplicationContextHolder,
	val applicationModeHolder: ApplicationModeHolder,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<TestcaseView>() {

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
			}
		}

	init {
		eventBus.register(EditedGraphViewEvent::class, editedGraphViewHandler)
	}

	override fun dispose() {
		super.dispose()
		propertyPanelController.dispose()
		eventBus.unregister(editedGraphViewHandler)
	}
}