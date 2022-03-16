package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioView
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioViewController
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseView
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseViewController
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * A [UIView] for editing a root [GraphView].
 *
 * Consists of a [GraphNavigationView] at the left side and a side bar at the right side
 * for displaying a [ScenarioView] and a [UsecaseView].
 */
interface GraphEditView : UIView, GraphDesktopViewItem {
	val graphNavigationView: GraphNavigationView
}

class GraphEditViewController(
	val editor: Editor,
	val applicationModeHolder: ApplicationModeHolder,
	val applicationContextHolder: GraphApplicationContextHolder,
	initialSavable: Savable? = null,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<GraphEditView>() {

	val graphNavigationViewController = GraphNavigationViewController(
		isRoot = true,
		editor.view as DrawingView<GraphView>,
		initialSavable,
		eventBus = eventBus)
	val scenarioViewController = ScenarioViewController(editor, applicationContextHolder, applicationModeHolder, eventBus)
	val usecaseViewController = UsecaseViewController(editor, applicationContextHolder, applicationModeHolder, eventBus)

	override fun dispose() {
		super.dispose()
		graphNavigationViewController.dispose()
		scenarioViewController.dispose()
		usecaseViewController.dispose()
	}

	fun setGraphView(graphView: GraphView, editable: Boolean, applyZoomStrategy: Boolean = true) {
		graphNavigationViewController.setRootGraphView(graphView, editable, applyZoomStrategy)
		scenarioViewController.graphView = graphView
		usecaseViewController.graphView = graphView
	}
}