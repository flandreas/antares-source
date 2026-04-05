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
class AntaresSingleCircuitAppJs(
    environment: io.antarescircuit.jabbah.app.Environment,
    akrabURL: String
) : io.antarescircuit.antares.AbstractAntaresAppJs(environment, akrabURL) {

    companion object {
        private val LOG by _root_ide_package_.io.antarescircuit.jabbah.base.logger(AntaresSingleCircuitAppJs::class)
    }

    /**
     * Initializes an application context and load the specified [io.antarescircuit.jabbah.graph.library.Library]/[io.antarescircuit.jabbah.graph.project.Project] and [io.antarescircuit.jabbah.graph.MetaGraph].
     * The result of the returned [Promise] can be cast to [io.antarescircuit.jabbah.graph.MetaGraph], which can't be declared
     * explicitly because that would require `@JsExport` for everything.
     */
    @Suppress("unused")
    fun start(libraryUuid: String, metaGraphUuid: String, themeName: String? = null): Promise<Any> {
        LOG.info("Starting Antares single circuit app")

        configure()

        val scope = CoroutineScope(SupervisorJob())
        return scope.promise {
            Promise.all(loadTranslations().toTypedArray()).await()
            init(
                _root_ide_package_.io.antarescircuit.jabbah.edit.auth.DesktopUserHolder(
                    _root_ide_package_.io.antarescircuit.jabbah.edit.auth.DesktopUser(
                        _root_ide_package_.io.antarescircuit.jabbah.edit.auth.UserIdentity.ANYBODY,
                        "",
                        false
                    )
                ), themeName)
            load(libraryUuid, metaGraphUuid)
        }
    }

    private suspend fun load(libraryUuid: String, metaGraphUuid: String): Promise<io.antarescircuit.jabbah.graph.MetaGraph> {
        LOG.debug("Loading repository in AntaresSingleCircuitAppLoaderJs")
        val library = loadRepository(libraryUuid).await()

        LOG.debug("Repository loaded, start loading MetaGraph")
        return loadMetaGraph(library, _root_ide_package_.io.antarescircuit.jabbah.base.UUID(metaGraphUuid))
    }
}
