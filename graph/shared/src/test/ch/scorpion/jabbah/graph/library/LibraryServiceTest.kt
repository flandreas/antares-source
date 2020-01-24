package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import io.mockk.mockk
import kotlin.test.*


/** Unit tests for [LibraryService].*/
class LibraryServiceTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val libraryPersistenceService = mockk<LibraryPersistenceService>(relaxed = true)
	private val service: LibraryService = LibraryService(userLibraryPersister = libraryPersistenceService, libraryAccessor = { libraryBuilder.library })
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

	@Test
	fun shouldDuplicate() {
		libraryBuilder.addContainerLibraryElement("Element")
		val orig = library.get("Element") as ContainerLibraryElement

		val duplicate = service.duplicateContainerLibraryElement(library, orig, TranslatableText("NewName"))

		assertNotEquals(orig.uuid, duplicate.uuid)
		assertEquals("NewName", duplicate.name.value)
	}
}