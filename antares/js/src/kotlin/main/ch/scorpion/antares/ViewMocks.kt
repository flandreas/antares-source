package ch.scorpion.antares

import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.graph.ui.GraphNavigationView
import ch.scorpion.jabbah.graph.ui.NavigationStackEntry
import ch.scorpion.jabbah.graph.ui.NavigationStackView
import ch.scorpion.jabbah.graph.ui.NavigationStackViewController
import ch.scorpion.jabbah.graph.ui.graphviewer.GraphViewerController
import ch.scorpion.jabbah.graph.ui.graphviewer.GraphViewerView
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Sets dummy [View] implementations of controller classes not yet used by [AntaresSingleCircuitViewerJs].
 * Without this, "access to uninitialized view property" would result.
 */
internal class ViewMocks(
    private val graphViewerController: GraphViewerController
) : GraphViewerView, GraphNavigationView, NavigationStackView {

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
    override val drawingView: DrawingView<GraphView> get() = graphViewerController.drawingView
    override var contextColor: CompositeColor? = null
    override val isDetached: Boolean = false
    override fun disposeItem() {}
    override fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>? = null
    override fun createCloseRequest(): Any = "Close Request"
    override fun displays(content: Any?): Boolean = content === graphView

    /** ---- [NavigationStackView] */

    override var editable: Boolean = false
    override var active: Boolean = false
}