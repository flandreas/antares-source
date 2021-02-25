package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioView
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioViewController
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseView
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseViewController

/**
 * A [UIView] for editing a root [GraphView].
 *
 * Consists of a [GraphNavigationView] at the left side and a side bar at the right side
 * for displaying a [ScenarioView] and a [UsecaseView].
 */
interface GraphEditView : UIView

class GraphEditViewController(
	val drawingView: DrawingView<GraphView>,
	initialSavable: Savable? = null,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<GraphEditView>() {

	val graphNavigationViewController = GraphNavigationViewController(isRoot = true, drawingView, initialSavable, eventBus = eventBus)
	val scenarioViewController = ScenarioViewController(eventBus)
	val usecaseViewController = UsecaseViewController()

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