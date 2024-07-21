package ch.scorpion.antares.module

import ch.scorpion.antares.AntaresApplication
import ch.scorpion.antares.view.AntaresLibraryFactory
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.DataLocation
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.module.EditModuleJs
import ch.scorpion.jabbah.graph.draw.ImageLoaderJs
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.dictionary.Akrab2RestLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.nonvolatile.NonVolatileServiceJs
import ch.scorpion.jabbah.graph.project.ProjectManagementService
import ch.scorpion.jabbah.graph.project.ProjectModule

/**
 * Module definition of the [ch.scorpion.antares] module for the JS platform.
 */
object AntaresModuleJs : AbstractModule() {

    override fun initialize() {
        EditModuleJs.require()

        LibraryModule.DEF_LIBRARY_UUID = AntaresApplication.DEF_LIBRARY_UUID

        AntaresViewModule.require()

        val akrabUrl = BaseModule.properties.getString(DataLocation.PROP_SERVER_URL)

        // TODO Initialize RasterImageFactory as well.
        // Required by Components that build Images programmatically, such as VideoRamView.

        DrawModule.imageLoader = ImageLoaderJs()

        GraphModelModule.nonVolatileService = NonVolatileServiceJs()

        LibraryModule.systemLibraryPersistenceService = Akrab2RestSystemLibraryPersistenceServiceJs(akrabUrl)
        LibraryModule.libraryFactory = AntaresLibraryFactory()
        LibraryModule.libraryService = LibraryService()

        LibraryModule.libraryManagementService = LibraryManagementService()

        LibraryModule.systemLibraryDictionaryService = LibraryDictionaryService(
            Akrab2RestLibraryDictionaryPersistenceService(akrabUrl)
        )

        LibraryModule.libraryManagementService = LibraryManagementService()

        ProjectModule.projectLibraryPersistenceService = Akrab2RestProjectPersistenceServiceJs(akrabUrl)

        ProjectModule.projectManagementService = ProjectManagementService(
            newMetaGraphNameTranslationKey = "graph.name.unknown")


        loadTranslations()
    }

    /** TODO Work around for missing implementation of [Translations] mechanism for texts used for demo application.*/
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