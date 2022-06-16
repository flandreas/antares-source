package ch.scorpion.jabbah.graph.library

import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryImplTest {

	companion object {
		init {
			GraphLibraryTestRule.configure()
		}
	}

	private val libraryPersistenceService = mockk<LibraryPersistenceService>(relaxed = true)
	private val service: LibraryService = LibraryService(userLibraryPersister = libraryPersistenceService)
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

		assertEquals(2, library.directory.indexOf(library.directory.getRecursively("Folder3")!!))
	}

	@Test
	fun shouldFindIndexOfNested() {
		libraryBuilder
			.addDirectory("Folder1")
			.back()
			.addDirectory("Folder2")
			.back()
			.addDirectory("Folder3")

		assertEquals(2, library.directory.indexOf(library.directory.getRecursively("Folder3")!!))
	}

	@Test
	fun shouldCountMetaGraphs() {
		libraryBuilder
			.addContainerLibraryElement("MetaGraph1")
			.addDirectory("Folder1")
			.addContainerLibraryElement("MetaGraph2")
			.addContainerLibraryElement("MetaGraph3")

		assertEquals(3, library.metaGraphCount)
	}
}