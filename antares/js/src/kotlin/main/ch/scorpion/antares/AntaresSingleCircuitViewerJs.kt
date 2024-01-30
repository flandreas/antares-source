package ch.scorpion.antares

import ch.scorpion.antares.module.AntaresModuleJs
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.LogSystem
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.edit.auth.AnonymousWebUserHolder
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.UserIdentity
import ch.scorpion.jabbah.graph.library.LibraryIdentification
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.AkrabApiException
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.graphviewer.GraphViewerController
import ch.scorpion.jabbah.graph.ui.graphviewer.GraphViewerView
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
@Suppress("unused")
@JsExport
class AntaresSingleCircuitViewerJs(
    ownerUuid: String,
    projectUuid: String,
    metaGraphUuid: String,
    themeName: String? = null
) {

    private val controller: GraphViewerController

    init {
        try {
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

            controller = GraphViewerController(clone.graphView, displayGlobalMessages = true)
            controller.view = object : GraphViewerView {
                override fun notifyAllResourcesLoaded() {}
                override fun dispose() {}
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    fun bindCanvas(
        canvas: HTMLCanvasElement,
        width: Int? = null,
        height: Int? = null
    ) {
        try {
            val dimension: Dimension2D? = if (width != null && height != null) {
                Dimension2D(width, height)
            } else {
                null
            }

            CanvasJs(canvas, controller.drawingView, dimension)
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    fun getToggleApplicationModeAction(): Action = controller.toggleApplicationModeAction
}