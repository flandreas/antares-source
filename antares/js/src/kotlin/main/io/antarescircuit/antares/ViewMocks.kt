package io.antarescircuit.antares

import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.graph.ui.GraphNavigationView
import io.antarescircuit.jabbah.graph.ui.NavigationStackEntry
import io.antarescircuit.jabbah.graph.ui.NavigationStackView
import io.antarescircuit.jabbah.graph.ui.NavigationStackViewController
import io.antarescircuit.jabbah.graph.ui.graphviewer.GraphViewerController
import io.antarescircuit.jabbah.graph.ui.graphviewer.GraphViewerView
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * Sets dummy [View] implementations of controller classes not yet used by [AntaresSingleCircuitViewerJs].
 * Without this, "access to uninitialized view property" would result.
 */
internal class ViewMocks(
    private val graphViewerController: GraphViewerController
) : GraphViewerView,
    GraphNavigationView, NavigationStackView {

    init {
        graphViewerController.view = this
        graphViewerController.graphNavigationViewController.view = this
        graphViewerController.graphNavigationViewController.navigationStackViewController.view = this
        graphViewerController.graphNavigationViewController.navigationStackViewController.navigationStack.rootEntry =
            NavigationStackEntry(content = graphViewerController.drawingView.content)
    }

    override val controller: NavigationStackViewController
        get() = graphViewerController.graphNavigationViewController.navigationStackViewController

    /** ---- [GraphViewerView] */

    override fun dispose() {}

    /** ---- [GraphNavigationView] */

    override val layoutWidth: Int get() = 0
    override val layoutHeight: Int get() = 0
    override val graphView: GraphView get() = graphViewerController.drawingView.drawing
    override val reusable: Boolean get() = false
    override val showsNavigationRoot: Boolean get() = true
    override fun refresh() {}
    override val drawingView: DrawingView<GraphElementView<*>, GraphView> get() = graphViewerController.drawingView
    override var contextColor: CompositeColor? = null
    override val isDetached: Boolean = false
    override fun disposeItem() {}
    override fun findContent(condition: (DrawingViewContent<GraphElementView<*>, GraphView>) -> Boolean): DrawingViewContent<*,*>? = null
    override fun createCloseRequest(): Any = "Close Request"
    override fun displays(content: Any?): Boolean = content === graphView

    /** ---- [NavigationStackView] */

    override var editable: Boolean = false
    override var active: Boolean = false
}