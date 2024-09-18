package ch.scorpion.antares

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.graphviewer.GraphViewerController
import org.w3c.dom.HTMLCanvasElement

@JsExport
class AntaresEditorViewerJs(
    content: AntaresEditorContent
) {
    companion object {
        private val LOG by logger(AntaresEditorViewerJs::class)
    }


    @Suppress("unused") // Used in JS applications
    var libraryTree: LibraryTreeNodeJS = content.libraryTree
        private set

    private var metaGraph: MetaGraph? = null

    private val controller: GraphViewerController

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
}
