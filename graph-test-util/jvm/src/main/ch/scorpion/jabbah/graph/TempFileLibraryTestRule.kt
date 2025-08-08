package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.library.FileLibraryPersistenceService
import ch.scorpion.jabbah.graph.library.GraphLibraryFactory
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryManagementService
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryProperties
import ch.scorpion.jabbah.graph.library.LibraryService
import ch.scorpion.jabbah.graph.library.dictionary.FileLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.project.ProjectManagementService
import ch.scorpion.jabbah.graph.project.ProjectModule
import java.nio.file.Files
import kotlin.io.path.absolutePathString

/**
 * Provides a setup with all [ch.scorpion.jabbah.graph.library.Library] related services based on temporary files.
 */
object TempFileLibraryTestRule {

	fun configure() {
		//GraphViewTestRule.configure()

		val tempDir = Files.createTempDirectory(null)

		LibraryModule.libraryFactory = GraphLibraryFactory()

		LibraryModule.systemLibraryPersistenceService =
            FileLibraryPersistenceService({ tempDir.absolutePathString() }, "systemLibs")
		LibraryModule.systemLibraryDictionaryService = LibraryDictionaryService(
            FileLibraryDictionaryPersistenceService(
                { tempDir.absolutePathString() },
                "systemLibs"
            )
        )

		LibraryModule.userLibraryPersistenceService =
            FileLibraryPersistenceService({ tempDir.absolutePathString() }, "libraries")
		LibraryModule.userLibraryDictionaryService = LibraryDictionaryService(
            FileLibraryDictionaryPersistenceService(
                { tempDir.absolutePathString() },
                "libraries"
            )
        )

		LibraryModule.libraryManagementService = LibraryManagementService()

		ProjectModule.projectLibraryPersistenceService =
            FileLibraryPersistenceService({ tempDir.absolutePathString() }, "projects")
		ProjectModule.projectLibraryService =
            LibraryService(userLibraryPersisterProvider = { ProjectModule.projectLibraryPersistenceService })
		ProjectModule.projectDictionaryService = LibraryDictionaryService(
            FileLibraryDictionaryPersistenceService(
                { tempDir.absolutePathString() },
                "projects"
            )
        )
		ProjectModule.projectManagementService = ProjectManagementService()
	}

	fun createLibrary(name: String): Library {
		val service = LibraryModule.libraryManagementService
		return service.create(LibraryProperties(name = TranslatableText(name)), null)
	}

	fun createAndEstablishCurrentLibrary(name: String) {
		val library = createLibrary(name)
		LibraryModule.libraryHolder.l = library
	}
}