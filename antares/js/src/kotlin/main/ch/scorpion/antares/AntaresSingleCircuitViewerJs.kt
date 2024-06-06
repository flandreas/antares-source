package ch.scorpion.antares

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.ViewDecorator
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.execution.PauseOrResumeAction
import ch.scorpion.jabbah.execution.speed.SystemSpeedOutlet
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.project.AkrabApiException
import ch.scorpion.jabbah.graph.ui.graphviewer.GraphViewerController
import kotlinx.browser.window
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
) : SystemSpeedOutlet {

    companion object {
        private val LOG by logger(AntaresSingleCircuitViewerJs::class)
    }

    private val controller: GraphViewerController

    init {
        if (data !is MetaGraph) {
            LOG.error("Expecting MetaGraph in app data, but was ${data::class.simpleName}")
        }
        LOG.debug("Initializing AntaresSingleCircuitViewerJs with MetaGraph ${(data as MetaGraph).uuid}")
        controller = GraphViewerController(data.graph.graphView, true)
        controller.graphNavigationViewController.enableOpenSubGraphRequests = false
        ViewMocks(controller)
        controller.drawingView.editable = false
    }

    fun bindCanvas(
        canvas: HTMLCanvasElement,
        width: Int? = null,
        height: Int? = null,
    ) {
        try {
            val dimension: Dimension2D? = if (width != null && height != null) {
                Dimension2D(width, height)
            } else {
                null
            }

            val effWidth = dimension?.width?.toInt() ?: canvas.offsetWidth
            val effHeight = dimension?.height?.toInt() ?: canvas.offsetHeight

            canvas.width = effWidth * window.devicePixelRatio.toInt()
            canvas.height = effHeight * window.devicePixelRatio.toInt()

            val canvasJs = CanvasJs(canvas, controller.drawingView, dimension)
            addWatermark()

            canvasJs.repaint()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    val circuitName: String get() = controller.graphNavigationViewController.drawingView.drawing.name.value

    fun getToggleApplicationModeAction(): Action = controller.toggleApplicationModeAction
    fun getSingleStepModeAction(): Action = controller.singleStepModeAction
    fun getPauseOrResumeAction(): PauseOrResumeAction = controller.pauseOrResumeAction

    /** ---- [SystemSpeedOutlet] interface */

    override val systemSpeedCategoryName: String
        get() = controller.applicationContextHolder.currentSystemSpeedCategory.systemSpeedCategory.toString()

    override var currentSystemSpeed: Int
        get() = controller.applicationContextHolder.currentSystemSpeedCategory.systemSpeed.speed
        set(value) {
            controller.applicationContextHolder.currentSystemSpeedCategory.systemSpeed.speed = value
        }

    private fun addWatermark() {
        controller.drawingView.decorator.bottomRight = Label(
            "Powered by antarescircuit.io",
            ViewDecorator.FONT_ITALIC,
            ViewDecorator.TEXT_COLOR_SUBTLE,
            horizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlignment = VerticalAlignment.TOP
        )
    }
}