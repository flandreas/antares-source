package ch.scorpion.antares

import ch.scorpion.antares.module.AntaresModuleJs
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.app.ApplicationVersion
import ch.scorpion.jabbah.app.CurrentApplicationVersion
import ch.scorpion.jabbah.app.Environment
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJs
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.edit.auth.UserHolder
import ch.scorpion.jabbah.edit.auth.UserIdentity
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.AbstractAkrab2RestLibraryPersistenceServiceJs
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.io.DomXmlReader
import ch.scorpion.jabbah.io.StoreXmlReader
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
        private val VERSION = ApplicationVersion("1.30.0")
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
        BaseModuleJs.translationService = TranslationServiceJsImpl(BaseModule.properties.getString(DataLocation.PROP_SERVER_URL))
    }

    /**
     * Initializes the application with a [User] and a theme, and some other initializations.
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
            "${BaseModule.properties.getString(DataLocation.PROP_SERVER_URL)}/repositoryProtected/$libraryUuid"
        } else {
            "${BaseModule.properties.getString(DataLocation.PROP_SERVER_URL)}/repository/$libraryUuid"
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
                val library = StoreXmlReader(DomXmlReader(it)).readStorable() as Library
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
