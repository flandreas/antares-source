package ch.scorpion.antares

import ch.scorpion.antares.module.AntaresModuleJs
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.app.Environment
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJs
import ch.scorpion.jabbah.edit.auth.AnonymousWebUserHolder
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.AbstractAkrab2RestLibraryPersistenceServiceJs
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.io.DomXmlReader
import ch.scorpion.jabbah.io.StoreXmlReader
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import kotlin.js.Promise

/**
 * Establishes everything in Kotlin code necessary to display a single circuit of a project
 * in a JavaScript application, and loads [MetaGraph] and [Library] data to be displayed later.
 */
@JsExport
class AntaresSingleCircuitAppJs(private val environment: Environment) {

    companion object {
        private val LOG by logger(AntaresSingleCircuitViewerJs::class)
    }

    /**
     * Initializes an application context and load the specified [Library]/[Project] and [MetaGraph].
     * The result of the returned [Promise] can be cast to [MetaGraph], which can't be declared
     * explicitly because that would require @JsExport for everything.
     */
    @Suppress("unused")
    fun start(libraryUuid: String, metaGraphUuid: String, themeName: String? = null): Promise<Any> {
        LOG.info("Starting Antares single circuit app")

        BaseModuleJs.require()
        when (environment) {
            Environment.Development -> {
                BaseModule.properties.set(DataLocation.PROP_SERVER_URL, AntaresApplication.AKRAB_DEV_URL)
            }
            Environment.Production -> {
                BaseModule.properties.set(DataLocation.PROP_SERVER_URL, AntaresApplication.AKRAB_PROD_URL)
            }
        }

        val scope = CoroutineScope(SupervisorJob())
        return scope.promise {
            Promise.all(loadTranslations().toTypedArray()).await()
            init(themeName)
            load(libraryUuid, metaGraphUuid)
        }
    }

    private fun init(themeName: String?) {
        EditAuthModule.require()
        EditAuthModule.userHolder = AnonymousWebUserHolder

        AntaresModuleJs.require()
        BaseModule.settings.set(SchedulerImpl.SETTING_ENABLE_SOFT_BREAKPOINTS, true)

        LogSystem.level = LogLevel.Debug

        AntaresThemes.install(themeName)
    }

    private suspend fun load(libraryUuid: String, metaGraphUuid: String): Promise<MetaGraph> {
        LOG.debug("Start loading repository in AntaresSingleCircuitAppLoaderJs")
        val library = loadRepository(libraryUuid)
            .await()
        LOG.debug("Repository loaded, start loading MetaGraph")
        return loadMetaGraph(library, UUID(metaGraphUuid))
    }

    private fun loadTranslations(): List<Promise<Unit>> {
        val promises = mutableListOf<Promise<Unit>>()
        Translations.addBundleAsync("jabbah-base")?.let { promises.add(it) }
        Translations.addBundleAsync("jabbah-draw")?.let { promises.add(it) }
        Translations.addBundleAsync("jabbah-edit")?.let { promises.add(it) }
        Translations.addBundleAsync("jabbah-execution")?.let { promises.add(it) }
        Translations.addBundleAsync("jabbah-app")?.let { promises.add(it) }
        Translations.addBundleAsync("jabbah-graph")?.let { promises.add(it) }
        Translations.addBundleAsync("antares")?.let { promises.add(it) }
        return promises
    }

    private fun loadRepository(libraryUuid: String): Promise<Library> {
        LOG.debug("Loading repository $libraryUuid")
        val url = "${BaseModule.properties.getString(DataLocation.PROP_SERVER_URL)}/repository/$libraryUuid"
        val headers = Headers()
        headers.append("Content-Type", "text/xml")

        var status = 200
        return window.fetch(url, RequestInit("GET", headers))
            .then {
                status = it.status.toInt()
                it.text()
            }
            .then {
                if (status != 200) {
                    throw Error(it)
                }
                val library = StoreXmlReader(DomXmlReader(it)).readStorable() as Library
                library.bindLibraryItems()
                LibraryModule.libraryHolder.l = library
                library
            }
    }

    private fun loadMetaGraph(library: Library, uuid: UUID): Promise<MetaGraph> {
        LOG.debug("Loading MetaGraph ${uuid.id}")
        val service = if (library.isSystem) {
            LibraryModule.systemLibraryPersistenceService
        } else {
            ProjectModule.projectLibraryPersistenceService
        }
        return (service as AbstractAkrab2RestLibraryPersistenceServiceJs).loadMetaGraphAsync(library, uuid)
    }
}