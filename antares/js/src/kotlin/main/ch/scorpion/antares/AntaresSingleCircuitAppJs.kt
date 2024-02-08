package ch.scorpion.antares

import ch.scorpion.antares.module.AntaresModuleJs
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.LogSystem
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModuleJs
import ch.scorpion.jabbah.edit.auth.AnonymousWebUserHolder
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryIdentification
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.ProjectModule

/**
 * Contains either the [MetaGraph] data (must be [Any] to avoid JS exporting [MetaGraph])
 * or an error message produces while loading the data.
 */
@JsExport
data class AntaresSingleCircuitAppJs(
    val data: Any?,
    val errorMsg: String?
) {
    companion object {

        fun success(data: Any): AntaresSingleCircuitAppJs =
            AntaresSingleCircuitAppJs(data, null)

        fun error(errorMsg: String): AntaresSingleCircuitAppJs =
            AntaresSingleCircuitAppJs(null, errorMsg)
    }
}

/**
 * Establishes everything in Kotlin code necessary to display a single circuit of a project
 * in a JavaScript application, and loads [MetaGraph] and [Library] data to be displayed later.
 */
@JsExport
object AntaresSingleCircuitAppLoaderJs {

    private val LOG by logger(AntaresSingleCircuitViewerJs::class)

    /**
     * TODO: This has to be asynchronous and return a Promise.
     */
    @Suppress("unused")
    fun start(libraryUuid: String, metaGraphUuid: String, themeName: String? = null): AntaresSingleCircuitAppJs {
        LOG.info("Starting Antares single circuit app")
        try {
            init(themeName)
            loadLibrary(libraryUuid)
            return AntaresSingleCircuitAppJs.success(loadMetaGraph(metaGraphUuid))
        } catch (e: Throwable) {
            LOG.error("Error: ", e)
            e.printStackTrace()
            return AntaresSingleCircuitAppJs(null, e.message)
        }
    }

    private fun init(themeName: String?) {
        BaseModuleJs.require()

        EditAuthModule.require()
        EditAuthModule.userHolder = AnonymousWebUserHolder

        AntaresModuleJs.require()
        LogSystem.level = LogLevel.Debug

        AntaresThemes.install(themeName)
    }

    private fun loadLibrary(libraryUuid: String) {
        LOG.debug("Loading library..")
        if (LibraryModule.systemLibraryDictionaryService.contains(UUID(libraryUuid))) {
            LOG.debug("-> opening system library")
            LibraryModule.libraryManagementService.open(
                LibraryIdentification(UUID(libraryUuid), null)
            )
        } else {
            LOG.debug("-> opening user project")
            ProjectModule.projectManagementService.open(
                LibraryIdentification(UUID(libraryUuid), null)
            )
        }
    }

    private fun loadMetaGraph(metaGraphUuid: String): MetaGraph {
        LOG.debug("Loading circuit..")
        return LibraryModule.libraryHolder.getMetaGraph(UUID(metaGraphUuid))
    }
}