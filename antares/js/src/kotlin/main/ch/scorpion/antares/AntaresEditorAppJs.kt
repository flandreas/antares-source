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
data class AntaresEditorContent(
    val library: Any,
    val libraryTree: LibraryTreeNodeJS,
    val metaGraph: Any?
)

@JsExport
class AntaresEditorAppJs(environment: Environment) : AbstractAntaresAppJs(environment) {

    companion object {
        private val LOG by logger(AbstractAntaresAppJs::class)
    }

    /**
     * Initializes an application context and load the specified [Library]/[Project]
     * and its main [MetaGraph], if any.
     * The result of the returned [Promise] is either [MetaGraph], if there is a default circuit in the [Library],
     * or the [Library] itself. Both can't be declared explicitly because that would require `@JsExport` for everything.
     */
    @Suppress("unused") // JS app
    fun start(libraryUuid: String, userIdentity: UserIdentity, themeName: String? = null): Promise<AntaresEditorContent> {
        LOG.info("Starting Antares editor app for user ${userIdentity.id}")

        configure()

        val scope = CoroutineScope(SupervisorJob())
        return scope.promise {
            Promise.all(loadTranslations().toTypedArray()).await()
            init(
                DesktopUserHolder(DesktopUser(userIdentity, "", false)),
                themeName
            )

            LOG.debug("Loading repository in AntaresEditorAppJs")
            val libraryPromise = loadRepository(libraryUuid)

            val library = libraryPromise.await()

            val libraryTree = createLibraryTreeJS(library)

            if (library.getDefaultElement() != null) {
                LOG.debug("Repository loaded, start loading MetaGraph")
                val metaGraphPromise = loadMetaGraph(library, UUID(library.getDefaultElement()!!.uuid.toString()))
                val metaGraph = metaGraphPromise.await()
                AntaresEditorContent(library, libraryTree, metaGraph)
            } else {
                AntaresEditorContent(library, libraryTree, null)
            }
        }
    }
}
