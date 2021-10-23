package ch.scorpion.antares

import ch.scorpion.jabbah.graph.library.FileLibraryPersistenceService
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryService
import java.io.File
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

abstract class AbstractJvmCircuitTest : AbstractCircuitTest() {

	protected fun setupLibrary() {
		val dir = Files.createTempDirectory(null)
		File.createTempFile("library", ".lib", dir.toFile())
		LibraryModule.userLibraryPersistenceService = FileLibraryPersistenceService(dir.parent.absolutePathString(), dir.name)
		LibraryModule.libraryService = LibraryService()
		LibraryModule.libraryHolder.l = LibraryImpl("testLib")
	}

}