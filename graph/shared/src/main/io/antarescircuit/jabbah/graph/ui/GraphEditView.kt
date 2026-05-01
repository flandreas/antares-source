package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem
import io.antarescircuit.jabbah.graph.ui.scenario.ScenarioView
import io.antarescircuit.jabbah.graph.ui.scenario.ScenarioViewController
import io.antarescircuit.jabbah.graph.ui.usecase.UsecaseView
import io.antarescircuit.jabbah.graph.ui.usecase.UsecaseViewController
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * A [UIView] for editing a root [GraphView].
 *
 * Consists of a [GraphNavigationView] on the left side and a sidebar on the right side
 * for displaying a [ScenarioView] and a [UsecaseView].
 */
interface GraphEditView : UIView, GraphDesktopViewItem {
	val graphNavigationView: GraphNavigationView
}

class GraphEditViewController(
	val drawingView: DrawingView<GraphElementView<*>, GraphView>,
	val editor: Editor,
	val applicationDataHolder: ApplicationDataHolder,
	val applicationModeHolder: ApplicationModeHolder,
	val applicationContextHolder: GraphApplicationContextHolder,
	initialSavable: Savable? = null,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<GraphEditView>() {

	val graphNavigationViewController = GraphNavigationViewController(
		isRoot = true,
		drawingView,
		initialSavable,
		eventBus = eventBus)
	val scenarioViewController = ScenarioViewController(editor, applicationDataHolder, applicationContextHolder, applicationModeHolder, eventBus)
	val usecaseViewController = UsecaseViewController(editor, applicationDataHolder, applicationContextHolder, applicationModeHolder, eventBus)

	override fun onViewInitialized() {
		graphNavigationViewController.closeTarget = view
	}

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