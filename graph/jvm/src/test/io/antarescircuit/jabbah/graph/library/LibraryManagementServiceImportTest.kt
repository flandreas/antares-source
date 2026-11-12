package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.graph.TempFileLibraryTestRule
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.test.*

class LibraryManagementServiceImportTest {

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		TempFileLibraryTestRule.configure()
	}

	@Test
	fun shouldReplaceExistingLibraryWhenUuidExists() = runBlocking {
		val library = TempFileLibraryTestRule.createLibrary("A")
		val zipPath = export(library)

		val result = LibraryModule.libraryManagementService.import(zipPath, replaceIfUuidExists = true)

		assertEquals(LibraryImportResultType.Success, result.type)
		assertTrue(LibraryModule.userLibraryDictionaryService.contains(library.uuid))
		assertEquals(library.uuid, LibraryModule.libraryManagementService.loadLibrary(library.identification).uuid)
	}

	@Test
	fun shouldKeepExistingLibraryWhenReplacementIsInvalid() = runBlocking {
		val importedLibrary = TempFileLibraryTestRule.createLibrary("B")
		val library = TempFileLibraryTestRule.createLibrary("A")
		LibraryModule.libraryService.addImport(library, importedLibrary.uuid)
		val zipPath = export(library)
		// Makes the import reference of the exported library stale
		LibraryModule.libraryManagementService.delete(importedLibrary.identification)

		val result = LibraryModule.libraryManagementService.import(zipPath, replaceIfUuidExists = true)

		assertEquals(LibraryImportResultType.StaleLibraryReference, result.type)
		assertTrue(LibraryModule.userLibraryDictionaryService.contains(library.uuid))
		assertEquals(library.uuid, LibraryModule.libraryManagementService.loadLibrary(library.identification).uuid)
	}

	private fun export(library: Library): String {
		val zipPath = Files.createTempFile(null, ".zip").toAbsolutePath().toString()
		LibraryModule.libraryManagementService.export(library.identification, zipPath)
		return zipPath
	}
}
