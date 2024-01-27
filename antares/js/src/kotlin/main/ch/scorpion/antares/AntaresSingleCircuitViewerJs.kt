package ch.scorpion.antares

import ch.scorpion.antares.module.AntaresModuleJs
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.LogSystem
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.auth.AnonymousWebUserHolder
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.UserIdentity
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.library.LibraryIdentification
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.AkrabApiException
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.GraphView
import org.w3c.dom.HTMLCanvasElement

/**
 * Establishes everything in Kotlin code necessary to display a single circuit of a project
 * in a JavaScript application.
 *
 * Can be used either by an Angular app (written in Angular/Typescript) or [AntaresIFrame],
 * which extract circuit and project UUID from URL query parameters.
 *
 * Uses as little Kotlin classes as possible to be exposed as TypeScript wrappers.
 * This is an application-level class that should be instantiated only once.
 *
 * @throws AkrabApiException in case of an error
 */
@JsExport
class AntaresSingleCircuitViewerJs(
    ownerUuid: String,
    projectUuid: String,
    metaGraphUuid: String,
    canvas: HTMLCanvasElement,
    width: Int? = null,
    height: Int? = null,
    themeName: String? = null
) {
    private val canvasJs: CanvasJs

    init {
        EditAuthModule.require()
        EditAuthModule.userHolder = AnonymousWebUserHolder

        AntaresModuleJs.require()
        LogSystem.level = LogLevel.Trace

        ProjectModule.projectManagementService.open(
            LibraryIdentification(UUID(projectUuid), UserIdentity(ownerUuid))
        )

        AntaresThemes.install(themeName)

        val metaGraph = LibraryModule.libraryHolder.getMetaGraph(UUID(metaGraphUuid))
        val clone = metaGraph.cloneGraphGraphStorable()

        val drawingView = createDrawingView(clone.graphView as Drawing<Component>)

        val dimension: Dimension2D? = if (width != null && height != null) {
            Dimension2D(width, height)
        } else {
            null
        }

        try {
            canvasJs = CanvasJs(canvas, drawingView, dimension)
        } catch (e: Throwable) {
            console.log("Error in create CanvasJs: ${e.message}, ${e.cause}")
            console.log(e.printStackTrace())
            throw e
        }
    }

    // TODO This would have to be done by the Angular app
    private fun createDrawingView(drawing: Drawing<Component>): DrawingView<GraphView> {
        val systemSpeed = SystemSpeed()
        val systemSpeedCategory = CurrentSystemSpeedCategory(systemSpeed)
        val applicationContextHolder = GraphApplicationContextHolder(
            SchedulerImpl(systemSpeedCategory),
            systemSpeed = systemSpeed,
            currentSystemSpeedCategory = systemSpeedCategory)
        val drawingView = EditModule.drawingViewFactory.create(
            drawing,
            applicationContextHolder,
            displayGlobalMessages = false
        ) as DrawingView<GraphView>
        return drawingView
    }
}