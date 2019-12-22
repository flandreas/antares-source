package ch.scorpion.jabbah.graph.library

import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [LibraryImpl].*/
class LibraryImplTest {

	companion object {
		init {
			GraphLibraryTestRule.configure()
		}
	}

	private val libraryPersistenceService = mockk<LibraryPersistenceService>(relaxed = true)
	private val service: LibraryService = LibraryService(userLibraryPersister = libraryPersistenceService, libraryAccessor = { libraryBuilder.library })
	private val libraryBuilder = LibraryBuilder(name = "Library", libraryService = service)
	private val library: Library get() = libraryBuilder.library

	@Test
	fun shouldFindIndexOf() {
		libraryBuilder
			.addDirectory("Folder1")
			.back()
			.addDirectory("Folder2")
			.back()
			.addDirectory("Folder3")

		assertEquals(2, library.indexOf(library.getRecursively("Folder3")!!))
	}

	@Test
	fun shouldFindIndexOfNested() {
		libraryBuilder
			.addDirectory("Folder1")
			.back()
			.addDirectory("Folder2")
			.back()
			.addDirectory("Folder3")

		assertEquals(2, library.indexOf(library.getRecursively("Folder3")!!))
	}
}