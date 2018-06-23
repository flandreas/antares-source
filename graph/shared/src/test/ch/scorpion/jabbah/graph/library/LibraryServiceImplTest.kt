package ch.scorpion.jabbah.graph.library

import com.nhaarman.mockito_kotlin.mock
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
	private val service: LibraryService = LibraryServiceImpl(persistenceService = libraryPersistenceService, libraryAccessor = { library })
	private val library = LibraryImpl(name = "Test", libraryService = service)

	@Test
	fun shouldAddFolderToRoot() {
		service.addFolder(library, "Folder", library)

		assertThat(library.get("Folder"), `is`(notNullValue()))
		assertThat(library.get("Folder"), `is`(instanceOf(LibraryFolder::class.java)))
	}

	@Test
	fun shouldAddFolderToFolder() {
		service.addFolder(library, "Folder", library)
		service.addFolder(library, "InnerFolder", library.get("Folder") as LibraryDirectory)

		assertThat(library.getRecursively("InnerFolder"), `is`(notNullValue()))
		assertThat((library.get("Folder") as LibraryFolder).contains(library.getRecursively("InnerFolder") as LibraryItem), `is`(true))
	}

}