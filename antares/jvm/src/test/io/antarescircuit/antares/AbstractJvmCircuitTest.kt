package io.antarescircuit.antares

import io.antarescircuit.jabbah.graph.library.FileLibraryPersistenceService
import io.antarescircuit.jabbah.graph.library.LibraryImpl
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.nonvolatile.NonVolatileServiceJvm
import java.io.File
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.test.BeforeTest

/**
 * Base class for circuit simulation tests using a library built during test setup
 * in a temporary directory.
 */
abstract class AbstractJvmCircuitTest : AbstractCircuitTest() {

	companion object {
		fun setupLibrary() {
			val dir = Files.createTempDirectory(null)
			File.createTempFile("library", ".lib", dir.toFile())
			LibraryModule.userLibraryPersistenceService = FileLibraryPersistenceService({ dir.parent.absolutePathString() }, dir.name)
			LibraryModule.libraryHolder.l = LibraryImpl("testLib")
		}
	}

	@BeforeTest
	fun setupNonVolatile() {
		val nonVolatileDir = Files.createTempDirectory(null)
		GraphModelModule.nonVolatileService = NonVolatileServiceJvm({ nonVolatileDir.parent.absolutePathString() }, nonVolatileDir.name)
	}

	protected fun setupLibrary() {
		Companion.setupLibrary()
	}
}