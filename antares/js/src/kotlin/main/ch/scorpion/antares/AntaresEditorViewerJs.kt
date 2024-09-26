package ch.scorpion.antares

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.execution.PauseOrResumeAction
import ch.scorpion.jabbah.execution.ExecutionControlOutlet
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.ui.graphviewer.GraphViewerController
import org.w3c.dom.HTMLCanvasElement

@Suppress("unused") // JS app
@JsExport
class AntaresEditorViewerJs(
    private val content: AntaresEditorContent
) : ExecutionControlOutlet {

    companion object {
        private val LOG by logger(AntaresEditorViewerJs::class)
    }

    private var metaGraph: MetaGraph? = null

    private val controller: GraphViewerController

    @Suppress("unused") // Used in JS applications
    var libraryTree: LibraryTreeNodeJS = content.libraryTree
        private set

    @Suppress("unused") // Used in JS applications
    val metaGraphId: String? get() = metaGraph?.uuid?.id

    init {
        if (content.metaGraph != null && content.metaGraph !is MetaGraph) {
            LOG.error("Expecting MetaGraph in content, got ${content.metaGraph::class.simpleName}")
        }
        metaGraph = content.metaGraph as? MetaGraph

        controller = GraphViewerController(metaGraph?.graph?.graphView, true)
        controller.graphNavigationViewController.enableOpenSubGraphRequests = false
        ViewMocks(controller)

        // Required to activate ScenarioDetector
        if (metaGraph != null) {
            controller.graphNavigationViewController.setRootGraphView(metaGraph!!.graph.graphView, false)
        }
    }

    @Suppress("unused") // JS app
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

    @Suppress("unused") // JS app
    fun openMetaGraph(id: String) {
        val library = content.library as Library
        metaGraph = library.getMetaGraph(UUID(id))
        controller.setMetaGraph(metaGraph!!)
        controller.graphNavigationViewController.setRootGraphView(metaGraph!!.graph.graphView, false)
    }

    /** ---- [ExecutionControlOutlet] interface */

    override val toggleApplicationModeAction: Action get() = controller.toggleApplicationModeAction
    override val singleStepModeAction: Action get() = controller.singleStepModeAction
    override val pauseOrResumeAction: PauseOrResumeAction get() = controller.pauseOrResumeAction

    override val systemSpeedCategoryName: String
        get() = controller.applicationContextHolder.currentSystemSpeedCategory.systemSpeedCategory.toString()

    override var currentSystemSpeed: Int
        get() = controller.applicationContextHolder.currentSystemSpeedCategory.systemSpeed.speed
        set(value) {
            controller.applicationContextHolder.currentSystemSpeedCategory.systemSpeed.speed = value
        }
}
