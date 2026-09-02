package io.antarescircuit.antares

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.draw.view.CanvasJs
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.execution.ExecutionControlOutlet
import io.antarescircuit.jabbah.execution.ExecutionDepthAction
import io.antarescircuit.jabbah.execution.SchedulerActions
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
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
    private val content: AntaresEditorContent
) : SchedulerActions {

    companion object {
        private val LOG by logger(AntaresEditorViewerJs::class)
    }

    private var metaGraph: MetaGraph? = null

    private val controller: GraphViewerController

    private val previewDrawingView: DrawingView<Component, ContainerDrawing>

    private var previewMetaGraph: MetaGraph? = null

    @Suppress("unused") // Used in JS applications
    var libraryTree: LibraryTreeNodeJS = content.libraryTree
        private set

    val metaGraphId: String? get() = metaGraph?.uuid?.id

    val metaGraphName: String get() = metaGraph?.name?.let { RichText.stripToPlainText(it) } ?: ""

    val previewDescription: String get() = previewMetaGraph?.graph?.model?.description?.value.orEmpty()

    val executionControlOutlet: ExecutionControlOutlet get() = controller

    override val executionDepthAction: Action

    init {
        if (content.metaGraph != null && content.metaGraph !is MetaGraph) {
            LOG.error("Expecting MetaGraph in content, got ${content.metaGraph::class.simpleName}")
        }
        metaGraph = content.metaGraph as? MetaGraph

        controller = GraphViewerController(
            metaGraph?.graph?.graphView,
            true
        )
        previewDrawingView = EditModule.drawingViewFactory.create(
            ContainerDrawing(),
            controller.applicationContextHolder,
            false,
            "preview"
        )
        previewDrawingView.editable = false
        previewDrawingView.showGrid = false
        controller.graphNavigationViewController.enableOpenSubGraphRequests = false
        ViewMocks(controller)

        executionDepthAction =
            ExecutionDepthAction(controller.applicationContextHolder.scheduler)

        // This application has only 1 View, so set this View as the current one right from the start
        DrawViewModule.viewManager.activeView = controller.drawingView

        // Required to activate ScenarioDetector
        if (metaGraph != null) {
            controller.graphNavigationViewController.setRootGraphView(metaGraph!!.graph.graphView, false)
        }
    }

    fun bindCanvas(canvas: HTMLCanvasElement) {
        try {
            val canvasJs = CanvasJs(canvas, controller.drawingView)
            // No watermark in editor view

            canvasJs.repaint()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    fun bindPreviewCanvas(canvas: HTMLCanvasElement) {
        try {
            val canvasJs = CanvasJs(canvas, previewDrawingView)
            canvasJs.repaint()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    fun setPreviewMetaGraph(metaGraph: Any?) {
        if (metaGraph != null && metaGraph !is MetaGraph) {
            throw IllegalArgumentException("Expecting MetaGraph preview, got ${metaGraph::class.simpleName}")
        }
        previewMetaGraph = metaGraph
        previewDrawingView.setDrawing(previewMetaGraph?.containerDrawing ?: ContainerDrawing())
    }

    /**
     * Loads the [io.antarescircuit.jabbah.graph.MetaGraph] with the specified [io.antarescircuit.jabbah.base.UUID] asynchronously.
     * The promised object is the [io.antarescircuit.jabbah.graph.MetaGraph] (not exposed to JS).
     */
    fun loadMetaGraphAsync(uuid: String): Promise<Any> {
        if (controller.currentMode.isExecute()) {
            controller.setMode(ApplicationMode.EDIT)
        }

        val library = (content.library as Library).getContainerLibraryElement(UUID(uuid))?.library
            ?: throw IllegalArgumentException("Circuit not found")

        val service = if (library.isSystem) {
            LibraryModule.systemLibraryPersistenceService
        } else {
            ProjectModule.projectLibraryPersistenceService
        }

        return (service as AbstractAkrab2RestLibraryPersistenceServiceJs).loadMetaGraphAsync(library, UUID(uuid))
    }

    /**
     * Sets a [MetaGraph] as the currently displayed one.
     * The argument [metaGraph] is of type [.MetaGraph] (not exposed to JS).
     */
    fun setMetaGraph(metaGraph: Any) {
        if (controller.currentMode.isExecute()) {
            controller.setMode(ApplicationMode.EDIT)
        }
        this.metaGraph = metaGraph as MetaGraph
        controller.setMetaGraph(metaGraph)
        controller.graphNavigationViewController.setRootGraphView(metaGraph.graph.graphView, false)
    }
}
