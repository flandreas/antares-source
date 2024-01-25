package ch.scorpion.antares.module

import ch.scorpion.antares.AntaresApplication
import ch.scorpion.antares.view.AntaresLibraryFactory
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.TranslationServiceImpl
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModuleJs
import ch.scorpion.jabbah.edit.module.EditModuleJs
import ch.scorpion.jabbah.graph.library.LibraryManagementService
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryService
import ch.scorpion.jabbah.graph.library.UrlLibraryPersistenceServiceJs
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.library.dictionary.RestLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.project.ProjectManagementService
import ch.scorpion.jabbah.graph.project.ProjectModule

/**
 * Module definition of the [ch.scorpion.antares] module for the JS platform.
 */
object AntaresModuleJs : AbstractModule() {

    override fun initialize() {
        EditModuleJs.require()

        BaseModuleJs.translationService = TranslationServiceImpl(
            baseUrl = ".."
        )

        LibraryModule.DEF_LIBRARY_UUID = AntaresApplication.DEF_LIBRARY_UUID

        AntaresViewModule.require()

        LibraryModule.systemLibraryPersistenceService = UrlLibraryPersistenceServiceJs(
            baseUrl = "..",
            libraryDirectoryName = AntaresApplication.DEFAULT_LIB_DIRECTORY,
            libraryFileName = AntaresApplication.DEFAULT_LIB_FILENAME
        )
        LibraryModule.libraryFactory = AntaresLibraryFactory()
        LibraryModule.libraryService = LibraryService()

        LibraryModule.libraryManagementService = LibraryManagementService()

        LibraryModule.systemLibraryDictionaryService = LibraryDictionaryService(
            RestLibraryDictionaryPersistenceService(
            baseUrl = "..",
            libraryDirectoryName = AntaresApplication.DEFAULT_LIB_DIRECTORY,
            dictionaryFileName = "dictionary.xml",
            directoryExists = true
        )
        )

        LibraryModule.libraryManagementService = LibraryManagementService()

        ProjectModule.projectLibraryPersistenceService = UrlLibraryPersistenceServiceJs(
            baseUrl = "..",
            libraryDirectoryName = "web-projects",
            libraryFileName = AntaresApplication.DEFAULT_LIB_FILENAME
        )

        ProjectModule.projectManagementService = ProjectManagementService(
            newMetaGraphNameTranslationKey = "graph.name.unknown")


        // This was used for React icons. Not needed any more.
        //registerAntaresIconsInProvider()

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