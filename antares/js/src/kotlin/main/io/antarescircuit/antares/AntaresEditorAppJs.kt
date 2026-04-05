package io.antarescircuit.antares

import io.antarescircuit.jabbah.app.Environment
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.auth.DesktopUser
import io.antarescircuit.jabbah.edit.auth.DesktopUserHolder
import io.antarescircuit.jabbah.edit.auth.UserIdentity
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlin.js.Promise

@JsExport
data class AntaresEditorContent(
    val library: Any,
    val libraryTree: io.antarescircuit.antares.LibraryTreeNodeJS,
    val metaGraph: Any?
)

@JsExport
class AntaresEditorAppJs(
    environment: io.antarescircuit.jabbah.app.Environment,
    akrabURL: String
) : io.antarescircuit.antares.AbstractAntaresAppJs(environment, akrabURL) {

    companion object {
        private val LOG by _root_ide_package_.io.antarescircuit.jabbah.base.logger(_root_ide_package_.io.antarescircuit.antares.AbstractAntaresAppJs::class)
    }

    /**
     * Initializes an application context and load the specified [io.antarescircuit.jabbah.graph.library.Library]/[io.antarescircuit.jabbah.graph.project.Project]
     * and its main [io.antarescircuit.jabbah.graph.MetaGraph], if any.
     * The result of the returned [Promise] is either [io.antarescircuit.jabbah.graph.MetaGraph], if there is a default circuit in the [io.antarescircuit.jabbah.graph.library.Library],
     * or the [io.antarescircuit.jabbah.graph.library.Library] itself. Both can't be declared explicitly because that would require `@JsExport` for everything.
     */
    @Suppress("unused") // JS app
    fun start(libraryUuid: String, userIdentity: io.antarescircuit.jabbah.edit.auth.UserIdentity, themeName: String? = null): Promise<io.antarescircuit.antares.AntaresEditorContent> {
        LOG.info("Starting Antares editor app for user ${userIdentity.id}")

        configure()

        val scope = CoroutineScope(SupervisorJob())
        return scope.promise {
            Promise.all(loadTranslations().toTypedArray()).await()
            init(
                _root_ide_package_.io.antarescircuit.jabbah.edit.auth.DesktopUserHolder(
                    _root_ide_package_.io.antarescircuit.jabbah.edit.auth.DesktopUser(
                        userIdentity,
                        "",
                        false
                    )
                ),
                themeName
            )

            LOG.debug("Loading repository in AntaresEditorAppJs")
            val libraryPromise = loadRepository(libraryUuid)

            val library = libraryPromise.await()

            val libraryTree = _root_ide_package_.io.antarescircuit.antares.createLibraryTreeJS(library)

            if (library.getDefaultElement() != null) {
                LOG.debug("Repository loaded, start loading MetaGraph")
                val metaGraphPromise = loadMetaGraph(library,
                    _root_ide_package_.io.antarescircuit.jabbah.base.UUID(library.getDefaultElement()!!.uuid.toString())
                )
                val metaGraph = metaGraphPromise.await()
                _root_ide_package_.io.antarescircuit.antares.AntaresEditorContent(library, libraryTree, metaGraph)
            } else {
                _root_ide_package_.io.antarescircuit.antares.AntaresEditorContent(library, libraryTree, null)
            }
        }
    }
}
