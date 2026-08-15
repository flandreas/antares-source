package io.antarescircuit.antares

import io.antarescircuit.antares.module.AntaresModuleJs
import io.antarescircuit.antares.view.theme.AntaresThemes
import io.antarescircuit.jabbah.app.ApplicationVersion
import io.antarescircuit.jabbah.app.CurrentApplicationVersion
import io.antarescircuit.jabbah.app.Environment
import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.module.BaseModuleJs
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.auth.User
import io.antarescircuit.jabbah.edit.auth.UserHolder
import io.antarescircuit.jabbah.edit.auth.UserIdentity
import io.antarescircuit.jabbah.execution.scheduler.SchedulerImpl
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.AbstractAkrab2RestLibraryPersistenceServiceJs
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.io.DomXmlReader
import io.antarescircuit.jabbah.io.StoreXmlReader
import kotlinx.browser.window
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import kotlin.js.Promise

/**
 * Establishes everything in Kotlin code necessary to display a single circuit of a project
 * in a JavaScript application, and loads [MetaGraph] and [Library] data to be displayed later.
 */
abstract class AbstractAntaresAppJs(
    private val environment: Environment,
    private val akrabURL: String
) {

    companion object {
        private val LOG by logger(AbstractAntaresAppJs::class)

        // TODO: Find a way to make this dynamic, i.e. read from version.txt also on JS platform
        private val VERSION = ApplicationVersion("2.2.0")
    }

    private val isUserAuthenticated: Boolean get() =
        EditAuthModule.userHolder.user.identity.id != UserIdentity.ANYBODY.id

    /**
     * Configures the application to be able to start calling Akrab REST endpoints, especially
     * the translations, which are fetched first.
     */
    protected fun configure() {
        BaseModuleJs.require()
        BaseModule.properties.set(DataLocation.PROP_SERVER_URL, akrabURL)
        BaseModuleJs.translationService =
            TranslationServiceJsImpl(
                BaseModule.properties.getString(
                    DataLocation.PROP_SERVER_URL
                )
            )
    }

    /**
     * Initializes the application with a [io.antarescircuit.jabbah.edit.auth.User] and a theme, and some other initializations.
     * After that, the required user data can be fetched.
     */
    protected fun init(userHolder: UserHolder<User>, themeName: String?) {
        EditAuthModule.require()
        EditAuthModule.userHolder = userHolder

        AntaresModuleJs.require()
        BaseModule.settings.set(SchedulerImpl.SETTING_ENABLE_SOFT_BREAKPOINTS, true)

        CurrentApplicationVersion.version = VERSION
        LogSystem.level = LogLevel.Info

        AntaresThemes.install(themeName)
    }

    protected fun loadTranslations(): List<Promise<Unit>> {
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

    protected fun loadRepository(libraryUuid: String): Promise<Library> {
        LOG.debug("Loading repository $libraryUuid")
        val url = if (isUserAuthenticated) {
            "${
                BaseModule.properties.getString(DataLocation.PROP_SERVER_URL)}/repositoryProtected/$libraryUuid"
        } else {
            "${
                BaseModule.properties.getString(DataLocation.PROP_SERVER_URL)}/repository/$libraryUuid"
        }
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
                val library = StoreXmlReader(DomXmlReader(it)
                ).readStorable() as Library
                library.bindLibraryItems()
                LibraryModule.libraryHolder.l = library
                library
            }
    }

    protected fun loadMetaGraph(library: Library, uuid: UUID): Promise<MetaGraph> {
        LOG.debug("Loading MetaGraph ${uuid.id}")
        val service = if (library.isSystem) {
            LibraryModule.systemLibraryPersistenceService
        } else {
            ProjectModule.projectLibraryPersistenceService
        }
        return (service as AbstractAkrab2RestLibraryPersistenceServiceJs).loadMetaGraphAsync(library, uuid)
    }
}
