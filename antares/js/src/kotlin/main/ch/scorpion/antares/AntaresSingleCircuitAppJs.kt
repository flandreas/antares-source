package ch.scorpion.antares

import ch.scorpion.jabbah.app.Environment
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.auth.DesktopUser
import ch.scorpion.jabbah.edit.auth.DesktopUserHolder
import ch.scorpion.jabbah.edit.auth.UserIdentity
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlin.js.Promise

@JsExport
class AntaresSingleCircuitAppJs(
    environment: Environment,
    akrabURL: String
) : AbstractAntaresAppJs(environment, akrabURL) {

    companion object {
        private val LOG by logger(AntaresSingleCircuitAppJs::class)
    }

    /**
     * Initializes an application context and load the specified [Library]/[Project] and [MetaGraph].
     * The result of the returned [Promise] can be cast to [MetaGraph], which can't be declared
     * explicitly because that would require `@JsExport` for everything.
     */
    @Suppress("unused")
    fun start(libraryUuid: String, metaGraphUuid: String, themeName: String? = null): Promise<Any> {
        LOG.info("Starting Antares single circuit app")

        configure()

        val scope = CoroutineScope(SupervisorJob())
        return scope.promise {
            Promise.all(loadTranslations().toTypedArray()).await()
            init(DesktopUserHolder(DesktopUser(UserIdentity.ANYBODY, "", false)), themeName)
            load(libraryUuid, metaGraphUuid)
        }
    }

    private suspend fun load(libraryUuid: String, metaGraphUuid: String): Promise<MetaGraph> {
        LOG.debug("Loading repository in AntaresSingleCircuitAppLoaderJs")
        val library = loadRepository(libraryUuid).await()

        LOG.debug("Repository loaded, start loading MetaGraph")
        return loadMetaGraph(library, UUID(metaGraphUuid))
    }
}
