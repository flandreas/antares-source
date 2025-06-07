package ch.scorpion.jabbah.graph.ui.graphviewer

import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.ui.GraphNavigationViewController
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

interface SubGraphViewerView : UIView

/**
 * Displays the [GraphView] of a single [SubGraphVerticeView] without the possibility
 * to change the [ApplicationMode].
 */
class SubGraphViewerController(
	graphView: GraphView,
	applicationContextHolder: GraphApplicationContextHolder
) : AbstractUIController<SubGraphViewerView>() {

	val drawingView = EditModule.drawingViewFactory.create(
		graphView as Drawing<Component>,
		applicationContextHolder,
		displayGlobalMessages = false,
		""
	) as DrawingView<GraphView>

	val graphNavigationViewController = GraphNavigationViewController(isRoot = false, drawingView)

	override fun dispose() {
		super.dispose()
		graphNavigationViewController.dispose()
		drawingView.dispose()
	}

	fun setGraphView(graphView: GraphView) {
		graphNavigationViewController.setRootGraphView(graphView, editable = false)
	}
}