package io.antarescircuit.antares.module

import io.antarescircuit.antares.AntaresApplication
import io.antarescircuit.antares.view.AntaresLibraryFactory
import io.antarescircuit.antares.view.module.AntaresViewModule
import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.DataLocation
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.edit.module.EditModuleJs
import io.antarescircuit.jabbah.graph.draw.ImageLoaderJs
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.library.dictionary.Akrab2RestLibraryDictionaryPersistenceService
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryService
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.nonvolatile.EmptyNonVolatileService
import io.antarescircuit.jabbah.graph.project.ProjectManagementService
import io.antarescircuit.jabbah.graph.project.ProjectModule

/**
 * Module definition of the [io.antarescircuit.antares] module for the JS platform.
 */
object AntaresModuleJs : io.antarescircuit.jabbah.base.AbstractModule() {

    override fun initialize() {
        _root_ide_package_.io.antarescircuit.jabbah.edit.module.EditModuleJs.require()

        _root_ide_package_.io.antarescircuit.jabbah.graph.library.LibraryModule.DEF_LIBRARY_UUID = _root_ide_package_.io.antarescircuit.antares.AntaresApplication.DEF_LIBRARY_UUID

        _root_ide_package_.io.antarescircuit.antares.view.module.AntaresViewModule.require()

        val akrabUrl = _root_ide_package_.io.antarescircuit.jabbah.base.module.BaseModule.properties.getString(
            _root_ide_package_.io.antarescircuit.jabbah.base.DataLocation.PROP_SERVER_URL)

        // TODO Initialize RasterImageFactory as well.
        // Required by Components that build Images programmatically, such as VideoRamView.

        _root_ide_package_.io.antarescircuit.jabbah.draw.module.DrawModule.imageLoader =
            _root_ide_package_.io.antarescircuit.jabbah.graph.draw.ImageLoaderJs()

        _root_ide_package_.io.antarescircuit.jabbah.graph.model.module.GraphModelModule.nonVolatileService =
            _root_ide_package_.io.antarescircuit.jabbah.graph.model.nonvolatile.EmptyNonVolatileService()

        _root_ide_package_.io.antarescircuit.jabbah.graph.library.LibraryModule.systemLibraryPersistenceService =
            _root_ide_package_.io.antarescircuit.jabbah.graph.library.Akrab2RestSystemLibraryPersistenceServiceJs(
                akrabUrl
            )
        _root_ide_package_.io.antarescircuit.jabbah.graph.library.LibraryModule.libraryFactory =
            _root_ide_package_.io.antarescircuit.antares.view.AntaresLibraryFactory()

        _root_ide_package_.io.antarescircuit.jabbah.graph.library.LibraryModule.libraryManagementService =
            _root_ide_package_.io.antarescircuit.jabbah.graph.library.LibraryManagementService()

        _root_ide_package_.io.antarescircuit.jabbah.graph.library.LibraryModule.systemLibraryDictionaryService =
            _root_ide_package_.io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryService(
                _root_ide_package_.io.antarescircuit.jabbah.graph.library.dictionary.Akrab2RestLibraryDictionaryPersistenceService(
                    akrabUrl
                )
            )

        _root_ide_package_.io.antarescircuit.jabbah.graph.library.LibraryModule.libraryManagementService =
            _root_ide_package_.io.antarescircuit.jabbah.graph.library.LibraryManagementService()

        _root_ide_package_.io.antarescircuit.jabbah.graph.project.ProjectModule.projectLibraryPersistenceService =
            _root_ide_package_.io.antarescircuit.jabbah.graph.library.Akrab2RestProjectPersistenceServiceJs(akrabUrl)

        _root_ide_package_.io.antarescircuit.jabbah.graph.project.ProjectModule.projectManagementService =
            _root_ide_package_.io.antarescircuit.jabbah.graph.project.ProjectManagementService(
                newMetaGraphNameTranslationKey = "graph.name.unknown"
            )


        loadTranslations()
    }

    override fun resetDependencies() {
        _root_ide_package_.io.antarescircuit.jabbah.edit.module.EditModuleJs.reset()
        _root_ide_package_.io.antarescircuit.antares.view.module.AntaresViewModule.reset()
    }

    /** TODO Work around for missing implementation of [io.antarescircuit.jabbah.base.Translations] mechanism for texts used for demo application.*/
    private fun loadTranslations() {
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addKey("execution.action.execute.name", "Simulate")
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addKey("execution.action.start.desc", "Start simulation")
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addKey("execution.action.stop.desc", "Stop simulation")
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addKey("execution.action.speed.name", "Simulation Speed")
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addKey("execution.systemSpeedCategory.use", "Use")
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addKey("execution.systemSpeedCategory.observe", "Observe")
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addKey("execution.systemSpeedCategory.explore", "Explore")
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addKey("graph.desktop.name", "Desktop")
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addKey("library.savable.prefix", "Library Element")
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addKey("library.library.name", "Library")
        _root_ide_package_.io.antarescircuit.jabbah.base.Translations.addKey("project.project.name", "Project")
    }
}