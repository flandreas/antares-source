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
 * in a JavaScript application, and loads [io.antarescircuit.jabbah.graph.MetaGraph] and [io.antarescircuit.jabbah.graph.library.Library] data to be displayed later.
 */
abstract class AbstractAntaresAppJs(
    private val environment: io.antarescircuit.jabbah.app.Environment,
    private val akrabURL: String
) {

    companion object {
        private val LOG by _root_ide_package_.io.antarescircuit.jabbah.base.logger(AbstractAntaresAppJs::class)

        // TODO: Find a way to make this dynamic, i.e. read from version.txt also on JS platform
        private val VERSION = _root_ide_package_.io.antarescircuit.jabbah.app.ApplicationVersion("2.0.0")
    }

    private val isUserAuthenticated: Boolean get() =
        _root_ide_package_.io.antarescircuit.jabbah.edit.auth.EditAuthModule.userHolder.user.identity.id != _root_ide_package_.io.antarescircuit.jabbah.edit.auth.UserIdentity.ANYBODY.id

    /**
     * Configures the application to be able to start calling Akrab REST endpoints, especially
     * the translations, which are fetched first.
     */
    protected fun configure() {
        _root_ide_package_.io.antarescircuit.jabbah.base.module.BaseModuleJs.require()
        _root_ide_package_.io.antarescircuit.jabbah.base.module.BaseModule.properties.set(_root_ide_package_.io.antarescircuit.jabbah.base.DataLocation.PROP_SERVER_URL, akrabURL)
        _root_ide_package_.io.antarescircuit.jabbah.base.module.BaseModuleJs.translationService =
            _root_ide_package_.io.antarescircuit.jabbah.base.TranslationServiceJsImpl(
                _root_ide_package_.io.antarescircuit.jabbah.base.module.BaseModule.properties.getString(
                    _root_ide_package_.io.antarescircuit.jabbah.base.DataLocation.PROP_SERVER_URL
                )
            )
    }

    /**
     * Initializes the application with a [io.antarescircuit.jabbah.edit.auth.User] and a theme, and some other initializations.
     * After that, the required user data can be fetched.
     */
    protected fun init(userHolder: io.antarescircuit.jabbah.edit.auth.UserHolder<io.antarescircuit.jabbah.edit.auth.User>, themeName: String?) {
        _root_ide_package_.io.antarescircuit.jabbah.edit.auth.EditAuthModule.require()
        _root_ide_package_.io.antarescircuit.jabbah.edit.auth.EditAuthModule.userHolder = userHolder

        _root_ide_package_.io.antarescircuit.antares.module.AntaresModuleJs.require()
        _root_ide_package_.io.antarescircuit.jabbah.base.module.BaseModule.settings.set(_root_ide_package_.io.antarescircuit.jabbah.execution.scheduler.SchedulerImpl.SETTING_ENABLE_SOFT_BREAKPOINTS, true)

        _root_ide_package_.io.antarescircuit.jabbah.app.CurrentApplicationVersion.version = VERSION
        _root_ide_package_.io.antarescircuit.jabbah.base.LogSystem.level = _root_ide_package_.io.antarescircuit.jabbah.base.LogLevel.Info

        _root_ide_package_.io.antarescircuit.antares.view.theme.AntaresThemes.install(themeName)
    }

    protected fun loadTranslations(): List<Promise<Unit>> {
        val promises = mutableListOf<Promise<Unit>>()
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addBundleAsync("jabbah-base")?.let { promises.add(it) }
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addBundleAsync("jabbah-draw")?.let { promises.add(it) }
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addBundleAsync("jabbah-edit")?.let { promises.add(it) }
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addBundleAsync("jabbah-execution")?.let { promises.add(it) }
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addBundleAsync("jabbah-app")?.let { promises.add(it) }
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addBundleAsync("jabbah-graph")?.let { promises.add(it) }
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addBundleAsync("antares")?.let { promises.add(it) }
        return promises
    }

    protected fun loadRepository(libraryUuid: String): Promise<io.antarescircuit.jabbah.graph.library.Library> {
        LOG.debug("Loading repository $libraryUuid")
        val url = if (isUserAuthenticated) {
            "${
                _root_ide_package_.io.antarescircuit.jabbah.base.module.BaseModule.properties.getString(
                    _root_ide_package_.io.antarescircuit.jabbah.base.DataLocation.PROP_SERVER_URL)}/repositoryProtected/$libraryUuid"
        } else {
            "${
                _root_ide_package_.io.antarescircuit.jabbah.base.module.BaseModule.properties.getString(
                    _root_ide_package_.io.antarescircuit.jabbah.base.DataLocation.PROP_SERVER_URL)}/repository/$libraryUuid"
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
                val library = _root_ide_package_.io.antarescircuit.jabbah.io.StoreXmlReader(
                    _root_ide_package_.io.antarescircuit.jabbah.io.DomXmlReader(
                        it
                    )
                ).readStorable() as io.antarescircuit.jabbah.graph.library.Library
                library.bindLibraryItems()
                _root_ide_package_.io.antarescircuit.jabbah.graph.library.LibraryModule.libraryHolder.l = library
                library
            }
    }

    protected fun loadMetaGraph(library: io.antarescircuit.jabbah.graph.library.Library, uuid: io.antarescircuit.jabbah.base.UUID): Promise<io.antarescircuit.jabbah.graph.MetaGraph> {
        LOG.debug("Loading MetaGraph ${uuid.id}")
        val service = if (library.isSystem) {
            _root_ide_package_.io.antarescircuit.jabbah.graph.library.LibraryModule.systemLibraryPersistenceService
        } else {
            _root_ide_package_.io.antarescircuit.jabbah.graph.project.ProjectModule.projectLibraryPersistenceService
        }
        return (service as io.antarescircuit.jabbah.graph.library.AbstractAkrab2RestLibraryPersistenceServiceJs).loadMetaGraphAsync(library, uuid)
    }
}
