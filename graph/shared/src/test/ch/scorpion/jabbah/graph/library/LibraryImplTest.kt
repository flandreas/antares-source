package ch.scorpion.jabbah.graph.library

import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryImplTest {

	private val libraryBuilder: LibraryBuilder

	private val library: Library get() = libraryBuilder.library

	init {
		GraphLibraryTestRule.configure()
		LibraryModule.userLibraryPersistenceService = mock<LibraryPersistenceService>(MockMode.autofill)
		libraryBuilder = LibraryBuilder(name = "Library")
	}

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