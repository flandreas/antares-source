package io.antarescircuit.antares

import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.graph.ui.GraphNavigationView
import io.antarescircuit.jabbah.graph.ui.NavigationStackEntry
import io.antarescircuit.jabbah.graph.ui.NavigationStackView
import io.antarescircuit.jabbah.graph.ui.NavigationStackViewController
import io.antarescircuit.jabbah.graph.ui.graphviewer.GraphViewerController
import io.antarescircuit.jabbah.graph.ui.graphviewer.GraphViewerView
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * Sets dummy [io.antarescircuit.jabbah.draw.View] implementations of controller classes not yet used by [io.antarescircuit.antares.AntaresSingleCircuitViewerJs].
 * Without this, "access to uninitialized view property" would result.
 */
internal class ViewMocks(
    private val graphViewerController: io.antarescircuit.jabbah.graph.ui.graphviewer.GraphViewerController
) : io.antarescircuit.jabbah.graph.ui.graphviewer.GraphViewerView,
    io.antarescircuit.jabbah.graph.ui.GraphNavigationView, io.antarescircuit.jabbah.graph.ui.NavigationStackView {

    init {
        graphViewerController.view = this
        graphViewerController.graphNavigationViewController.view = this
        graphViewerController.graphNavigationViewController.navigationStackViewController.view = this
        graphViewerController.graphNavigationViewController.navigationStackViewController.navigationStack.rootEntry =
            _root_ide_package_.io.antarescircuit.jabbah.graph.ui.NavigationStackEntry(content = graphViewerController.drawingView.content)
    }

    override val controller: io.antarescircuit.jabbah.graph.ui.NavigationStackViewController
        get() = graphViewerController.graphNavigationViewController.navigationStackViewController

    /** ---- [io.antarescircuit.jabbah.graph.ui.graphviewer.GraphViewerView] */

    override fun dispose() {}

    /** ---- [io.antarescircuit.jabbah.graph.ui.GraphNavigationView] */

    override val layoutWidth: Int get() = 0
    override val layoutHeight: Int get() = 0
    override val graphView: io.antarescircuit.jabbah.graph.view.GraphView get() = graphViewerController.drawingView.drawing
    override val reusable: Boolean get() = false
    override val showsNavigationRoot: Boolean get() = true
    override fun refresh() {}
    override val drawingView: io.antarescircuit.jabbah.edit.DrawingView<io.antarescircuit.jabbah.graph.view.GraphView> get() = graphViewerController.drawingView
    override var contextColor: io.antarescircuit.jabbah.draw.graphics.CompositeColor? = null
    override val isDetached: Boolean = false
    override fun disposeItem() {}
    override fun findContent(condition: (io.antarescircuit.jabbah.edit.DrawingViewContent<io.antarescircuit.jabbah.graph.view.GraphView>) -> Boolean): io.antarescircuit.jabbah.edit.DrawingViewContent<*>? = null
    override fun createCloseRequest(): Any = "Close Request"
    override fun displays(content: Any?): Boolean = content === graphView

    /** ---- [io.antarescircuit.jabbah.graph.ui.NavigationStackView] */

    override var editable: Boolean = false
    override var active: Boolean = false
}