package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.library.FileLibraryPersistenceService
import io.antarescircuit.jabbah.graph.library.GraphLibraryFactory
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryManagementService
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.LibraryProperties
import io.antarescircuit.jabbah.graph.library.LibraryService
import io.antarescircuit.jabbah.graph.library.dictionary.FileLibraryDictionaryPersistenceService
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryService
import io.antarescircuit.jabbah.graph.project.ProjectManagementService
import io.antarescircuit.jabbah.graph.project.ProjectModule
import java.nio.file.Files
import kotlin.io.path.absolutePathString

/**
 * Provides a setup with all [io.antarescircuit.jabbah.graph.library.Library] related services based on temporary files.
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