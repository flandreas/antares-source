package ch.scorpion.jabbah.graph.library

import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/** Unit tests for [LibraryServiceImpl].*/
class LibraryServiceImplTest {

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
	fun shouldAddFolderToRoot() {
		libraryBuilder.addDirectory("Folder")

		assertThat(library.get("Folder"), `is`(notNullValue()))
		assertThat(library.get("Folder"), `is`(instanceOf(LibraryFolder::class.java)))
	}

	@Test
	fun shouldAddFolderToFolder() {
		libraryBuilder
			.addDirectory("Folder")
			.addDirectory("InnerFolder")

		assertThat(library.getRecursively("InnerFolder"), `is`(notNullValue()))
		assertThat((library.get("Folder") as LibraryFolder).contains(library.getRecursively("InnerFolder") as LibraryItem), `is`(true))
	}

	@Test
	fun shouldMoveItemWithinFolder() {
		libraryBuilder
			.addDirectory("Folder")
			.addDirectory("Elem1")
			.back()
			.addDirectory("Elem2")
			.back()
			.addDirectory("Elem3")

		service.move(library, library.getRecursively("Elem1")!!, 2)

		val folder = library.getRecursively("Folder") as LibraryDirectory
		assertThat(folder.indexOf(library.getRecursively("Elem2")!!), `is`(0))
		assertThat(folder.indexOf(library.getRecursively("Elem3")!!), `is`(1))
		assertThat(folder.indexOf(library.getRecursively("Elem1")!!), `is`(2))
	}
}