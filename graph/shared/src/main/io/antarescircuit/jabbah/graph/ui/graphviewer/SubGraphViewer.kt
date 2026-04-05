package io.antarescircuit.jabbah.graph.ui.graphviewer

import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.ui.GraphNavigationViewController
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

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