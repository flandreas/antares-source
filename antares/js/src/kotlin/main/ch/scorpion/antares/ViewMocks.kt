package ch.scorpion.antares

import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.graph.ui.GraphNavigationView
import ch.scorpion.jabbah.graph.ui.NavigationStackEntry
import ch.scorpion.jabbah.graph.ui.NavigationStackView
import ch.scorpion.jabbah.graph.ui.graphviewer.GraphViewerController
import ch.scorpion.jabbah.graph.ui.graphviewer.GraphViewerView
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Sets dummy [View] implementations of controller classes not yet used by [AntaresSingleCircuitViewerJs].
 * Without this, "access to uninitialized view property" would result.
 */
internal class ViewMocks(
    private val controller: GraphViewerController
) : GraphViewerView, GraphNavigationView, NavigationStackView {

    init {
        controller.view = this
        controller.graphNavigationViewController.view = this
        controller.graphNavigationViewController.navigationStackViewController.view = this
        controller.graphNavigationViewController.navigationStackViewController.navigationStack.rootEntry =
            NavigationStackEntry(content = controller.drawingView.content)
    }

    /** ---- [GraphViewerView] */

    override fun dispose() {}

    /** ---- [GraphNavigationView] */

    override val graphView: GraphView get() = controller.drawingView.drawing
    override val reusable: Boolean get() = false
    override val showsNavigationRoot: Boolean get() = true
    override fun refresh() {}
    override val drawingView: DrawingView<GraphView> get() = controller.drawingView
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