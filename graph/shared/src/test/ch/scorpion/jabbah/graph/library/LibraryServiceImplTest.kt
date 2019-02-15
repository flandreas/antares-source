package ch.scorpion.jabbah.graph.library

import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


/** Unit tests for [LibraryServiceImpl].*/
class LibraryServiceImplTest {

	companion object {
		init {
			GraphLibraryTestRule.configure()
		}
	}

	private val libraryPersistenceService = mockk<LibraryPersistenceService>(relaxed = true)
	private val service: LibraryService = LibraryServiceImpl(persistenceService = libraryPersistenceService, libraryAccessor = { libraryBuilder.library })
	private val libraryBuilder = LibraryBuilder(name = "Library", libraryService = service)
	private val library: Library get() = libraryBuilder.library

	@Test
	fun shouldAddFolderToRoot() {
		libraryBuilder.addDirectory("Folder")

		assertNotNull(library.get("Folder"))
		assertTrue(library.get("Folder") is LibraryFolder)
	}

	@Test
	fun shouldAddFolderToFolder() {
		libraryBuilder
			.addDirectory("Folder")
			.addDirectory("InnerFolder")

		assertNotNull(library.getRecursively("InnerFolder"))
		assertTrue((library.get("Folder") as LibraryFolder).contains(library.getRecursively("InnerFolder") as LibraryItem))
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
		assertEquals(0, folder.indexOf(library.getRecursively("Elem2")!!))
		assertEquals(1, folder.indexOf(library.getRecursively("Elem3")!!))
		assertEquals(2, folder.indexOf(library.getRecursively("Elem1")!!))
	}
}