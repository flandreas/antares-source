package io.antarescircuit.antares

import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.draw.ViewDecorator
import io.antarescircuit.jabbah.draw.view.CanvasJs
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.execution.ExecutionControlOutlet
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.project.AkrabApiException
import io.antarescircuit.jabbah.graph.ui.graphviewer.GraphViewerController
import org.w3c.dom.HTMLCanvasElement

/**
 * Establishes everything in Kotlin code necessary to display a single circuit of a project
 * in a JavaScript application.
 *
 * Uses as little Kotlin classes as possible to be exposed as TypeScript wrappers.
 * This is an application-level class that should be instantiated only once.
 *
 * @throws AkrabApiException in case of an error
 */
@Suppress("unused")
@JsExport
class AntaresSingleCircuitViewerJs(
    data: Any
) {

    companion object {
        private val LOG by logger(AntaresSingleCircuitViewerJs::class)
    }

    private val controller: GraphViewerController

    val executionControlOutlet: ExecutionControlOutlet get() = controller

    init {
        if (data !is MetaGraph) {
            LOG.error("Expecting MetaGraph in app data, but was ${data::class.simpleName}")
        }
        LOG.debug("Initializing AntaresSingleCircuitViewerJs with MetaGraph ${(data as MetaGraph).uuid}")
        controller = GraphViewerController(
            data.graph.graphView,
            true
        )
        controller.graphNavigationViewController.enableOpenSubGraphRequests = false
        ViewMocks(controller)

        // Required to activate ScenarioDetector
        controller.graphNavigationViewController.setRootGraphView(data.graph.graphView, false)
    }

    fun bindCanvas(canvas: HTMLCanvasElement) {
        try {
            val canvasJs =
                CanvasJs(canvas, controller.drawingView)
            addWatermark()

            canvasJs.repaint()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    val circuitName: String get() = controller.graphNavigationViewController.drawingView.drawing.name.value

    /** ---- [ExecutionControlOutlet] interface */

    private fun addWatermark() {
        controller.drawingView.decorator.bottomRight =
            Label(
                "Powered by antarescircuit.io",
                ViewDecorator.FONT_ITALIC,
                ViewDecorator.TEXT_COLOR_SUBTLE,
                horizontalAlignment = HorizontalAlignment.LEFT,
                verticalAlignment = VerticalAlignment.TOP
            )
    }
}