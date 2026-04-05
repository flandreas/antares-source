package io.antarescircuit.antares

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.draw.view.CanvasJs
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.execution.ExecutionControlOutlet
import io.antarescircuit.jabbah.execution.ExecutionDepthAction
import io.antarescircuit.jabbah.execution.SchedulerActions
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.library.AbstractAkrab2RestLibraryPersistenceServiceJs
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.graph.ui.graphviewer.GraphViewerController
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.Promise

@Suppress("unused") // Used in JS applications
@JsExport
class AntaresEditorViewerJs(
    private val content: io.antarescircuit.antares.AntaresEditorContent
) : io.antarescircuit.jabbah.execution.SchedulerActions {

    companion object {
        private val LOG by _root_ide_package_.io.antarescircuit.jabbah.base.logger(AntaresEditorViewerJs::class)
    }

    private var metaGraph: io.antarescircuit.jabbah.graph.MetaGraph? = null

    private val controller: io.antarescircuit.jabbah.graph.ui.graphviewer.GraphViewerController

    @Suppress("unused") // Used in JS applications
    var libraryTree: io.antarescircuit.antares.LibraryTreeNodeJS = content.libraryTree
        private set

    val metaGraphId: String? get() = metaGraph?.uuid?.id

    val metaGraphName: String get() = metaGraph?.name?.let { _root_ide_package_.io.antarescircuit.jabbah.base.richtext.RichText.stripToPlainText(it) } ?: ""

    val executionControlOutlet: io.antarescircuit.jabbah.execution.ExecutionControlOutlet get() = controller

    override val executionDepthAction: io.antarescircuit.jabbah.base.Action

    init {
        if (content.metaGraph != null && content.metaGraph !is io.antarescircuit.jabbah.graph.MetaGraph) {
            LOG.error("Expecting MetaGraph in content, got ${content.metaGraph::class.simpleName}")
        }
        metaGraph = content.metaGraph as? io.antarescircuit.jabbah.graph.MetaGraph

        controller = _root_ide_package_.io.antarescircuit.jabbah.graph.ui.graphviewer.GraphViewerController(
            metaGraph?.graph?.graphView,
            true
        )
        controller.graphNavigationViewController.enableOpenSubGraphRequests = false
        _root_ide_package_.io.antarescircuit.antares.ViewMocks(controller)

        executionDepthAction =
            _root_ide_package_.io.antarescircuit.jabbah.execution.ExecutionDepthAction(controller.applicationContextHolder.scheduler)

        // This application has only 1 View, so set this View as the current one right from the start
        _root_ide_package_.io.antarescircuit.jabbah.draw.view.DrawViewModule.viewManager.activeView = controller.drawingView

        // Required to activate ScenarioDetector
        if (metaGraph != null) {
            controller.graphNavigationViewController.setRootGraphView(metaGraph!!.graph.graphView, false)
        }
    }

    fun bindCanvas(canvas: HTMLCanvasElement) {
        try {
            val canvasJs =
                _root_ide_package_.io.antarescircuit.jabbah.draw.view.CanvasJs(canvas, controller.drawingView)
            // No watermark in editor view

            canvasJs.repaint()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    /**
     * Loads the [io.antarescircuit.jabbah.graph.MetaGraph] with the specified [io.antarescircuit.jabbah.base.UUID] asynchronously.
     * The promised object is the [io.antarescircuit.jabbah.graph.MetaGraph] (not exposed to JS).
     */
    fun loadMetaGraphAsync(uuid: String): Promise<Any> {
        if (controller.currentMode.isExecute()) {
            controller.setMode(_root_ide_package_.io.antarescircuit.jabbah.graph.app.ApplicationMode.EDIT)
        }

        val library = (content.library as io.antarescircuit.jabbah.graph.library.Library).getContainerLibraryElement(
            _root_ide_package_.io.antarescircuit.jabbah.base.UUID(uuid)
        )?.library
            ?: throw IllegalArgumentException("Circuit not found")

        val service = if (library.isSystem) {
            _root_ide_package_.io.antarescircuit.jabbah.graph.library.LibraryModule.systemLibraryPersistenceService
        } else {
            _root_ide_package_.io.antarescircuit.jabbah.graph.project.ProjectModule.projectLibraryPersistenceService
        }

        return (service as io.antarescircuit.jabbah.graph.library.AbstractAkrab2RestLibraryPersistenceServiceJs).loadMetaGraphAsync(library,
            _root_ide_package_.io.antarescircuit.jabbah.base.UUID(uuid)
        )
    }

    /**
     * Sets a [io.antarescircuit.jabbah.graph.MetaGraph] as the currently displayed one.
     * The argument [metaGraph] is of type [io.antarescircuit.jabbah.graph.MetaGraph] (not exposed to JS).
     */
    fun setMetaGraph(metaGraph: Any) {
        if (controller.currentMode.isExecute()) {
            controller.setMode(_root_ide_package_.io.antarescircuit.jabbah.graph.app.ApplicationMode.EDIT)
        }
        this.metaGraph = metaGraph as io.antarescircuit.jabbah.graph.MetaGraph
        controller.setMetaGraph(metaGraph)
        controller.graphNavigationViewController.setRootGraphView(metaGraph.graph.graphView, false)
    }
}
