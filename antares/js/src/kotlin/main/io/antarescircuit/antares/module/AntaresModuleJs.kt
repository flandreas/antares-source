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
object AntaresModuleJs : AbstractModule() {

    override fun initialize() {
        EditModuleJs.require()

        LibraryModule.DEF_LIBRARY_UUID = AntaresApplication.DEF_LIBRARY_UUID

        AntaresViewModule.require()

        val akrabUrl = BaseModule.properties.getString(
            DataLocation.PROP_SERVER_URL)

        // TODO Initialize RasterImageFactory as well.
        // Required by Components that build Images programmatically, such as VideoRamView.

        DrawModule.imageLoader = ImageLoaderJs()

        GraphModelModule.nonVolatileService = EmptyNonVolatileService()

        LibraryModule.systemLibraryPersistenceService = Akrab2RestSystemLibraryPersistenceServiceJs(akrabUrl)
        LibraryModule.libraryFactory = AntaresLibraryFactory()

        LibraryModule.libraryManagementService = LibraryManagementService()

        LibraryModule.systemLibraryDictionaryService = LibraryDictionaryService(Akrab2RestLibraryDictionaryPersistenceService(akrabUrl))

        LibraryModule.libraryManagementService = LibraryManagementService()

        ProjectModule.projectLibraryPersistenceService = Akrab2RestProjectPersistenceServiceJs(akrabUrl)

        ProjectModule.projectManagementService = ProjectManagementService(newMetaGraphNameTranslationKey = "graph.name.unknown")

        loadTranslations()
    }

    override fun resetDependencies() {
        EditModuleJs.reset()
        AntaresViewModule.reset()
    }

    /** TODO Work around for missing implementation of [io.antarescircuit.jabbah.base.Translations] mechanism for texts used for demo application.*/
    private fun loadTranslations() {
        Translations.addKey("execution.action.execute.name", "Simulate")
        Translations.addKey("execution.action.start.desc", "Start simulation")
        Translations.addKey("execution.action.stop.desc", "Stop simulation")
        Translations.addKey("execution.action.speed.name", "Simulation Speed")
        Translations.addKey("execution.systemSpeedCategory.use", "Use")
        Translations.addKey("execution.systemSpeedCategory.observe", "Observe")
        Translations.addKey("execution.systemSpeedCategory.explore", "Explore")
        Translations.addKey("graph.desktop.name", "Desktop")
        Translations.addKey("library.savable.prefix", "Library Element")
        Translations.addKey("library.library.name", "Library")
        Translations.addKey("project.project.name", "Project")
    }
}