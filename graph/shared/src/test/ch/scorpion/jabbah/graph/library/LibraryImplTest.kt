package ch.scorpion.jabbah.graph.library

import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/** Unit tests for [LibraryImpl].*/
class LibraryImplTest {

	companion object {
		@ClassRule
		@JvmField
		val rule = GraphLibraryTestRule()
	}

	private val libraryPersistenceService = mock<LibraryPersistenceService>()
	private val service: LibraryService = LibraryServiceImpl(persistenceService = libraryPersistenceService, libraryAccessor = { libraryBuilder.library })
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

		assertThat(library.indexOf(library.getRecursively("Folder3")!!), `is`(2))
	}

	@Test
	fun shouldFindIndexOfNested() {
		libraryBuilder
			.addDirectory("Folder1")
			.back()
			.addDirectory("Folder2")
			.back()
			.addDirectory("Folder3")

		assertThat(library.indexOf(library.getRecursively("Folder3")!!), `is`(2))
	}
}