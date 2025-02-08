package ch.scorpion.antares

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.richtext.RichText
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.execution.ExecutionControlOutlet
import ch.scorpion.jabbah.execution.ExecutionDepthAction
import ch.scorpion.jabbah.execution.SchedulerActions
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.library.AbstractAkrab2RestLibraryPersistenceServiceJs
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.graphviewer.GraphViewerController
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

    @Suppress("unused") // Used in JS applications
    var libraryTree: LibraryTreeNodeJS = content.libraryTree
        private set

    val metaGraphId: String? get() = metaGraph?.uuid?.id

    val metaGraphName: String get() = metaGraph?.name?.let { RichText.stripToPlainText(it) } ?: ""

    val executionControlOutlet: ExecutionControlOutlet get() = controller

    override val executionDepthAction: Action

    init {
        if (content.metaGraph != null && content.metaGraph !is MetaGraph) {
            LOG.error("Expecting MetaGraph in content, got ${content.metaGraph::class.simpleName}")
        }
        metaGraph = content.metaGraph as? MetaGraph

        controller = GraphViewerController(metaGraph?.graph?.graphView, true)
        controller.graphNavigationViewController.enableOpenSubGraphRequests = false
        ViewMocks(controller)

        executionDepthAction = ExecutionDepthAction(controller.applicationContextHolder.scheduler)

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

    /**
     * Loads the [MetaGraph] with the specified [UUID] asynchronously.
     * The promised object is the [MetaGraph] (not exposed to JS).
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
     * The argument [metaGraph] is of type [MetaGraph] (not exposed to JS).
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
