package ch.scorpion.antares

import ch.scorpion.antares.module.AntaresAkrabPublicModule
import ch.scorpion.antares.view.theme.AntaresThemes
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
    canvasId: String,
    width: Int? = null,
    height: Int? = null,
    themeName: String? = null
) {
    private val controller: GraphViewerController
    private val canvasJs: CanvasJs

    init {
        console.log("Constructor AntaresSingleCircuitViewerJs")

        EditAuthModule.require()
        EditAuthModule.userHolder = AnonymousWebUserHolder

        AntaresAkrabPublicModule.require()
        LogSystem.level = LogLevel.Debug

        console.log("Before loading project")
        ProjectModule.projectManagementService.load(
            LibraryIdentification(UUID(projectUuid), UserIdentity(ownerUuid))
        )
        console.log("After loading project")

        AntaresThemes.install(themeName)

        controller = GraphViewerController()
        controller.setMetaGraph(LibraryModule.libraryHolder.getMetaGraph(UUID(metaGraphUuid)))

        val dimension: Dimension2D? = if (width != null && height != null) {
            Dimension2D(width, height)
        } else {
            null
        }
        canvasJs = CanvasJs(canvasId, controller.drawingView, dimension)
    }
}