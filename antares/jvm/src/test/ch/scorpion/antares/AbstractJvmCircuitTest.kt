package ch.scorpion.antares

import ch.scorpion.jabbah.graph.library.FileLibraryPersistenceService
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryService
import java.io.File

abstract class AbstractJvmCircuitTest : AbstractCircuitTest() {

	protected fun setupLibrary() {
		val file = File.createTempFile("library", ".lib")
		LibraryModule.userLibraryPersistenceService = FileLibraryPersistenceService(file.parentFile.absolutePath)
		LibraryModule.libraryService = LibraryService()
		LibraryModule.libraryHolder.l = LibraryImpl("testLib")
	}

}