package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import kotlin.test.*

class LibraryServiceTest {

	private val libraryBuilder: LibraryBuilder
	private val library: Library get() = libraryBuilder.library
	private val service: LibraryService get() = LibraryModule.libraryService

	init {
		GraphViewTestRule.configure()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		libraryBuilder = LibraryBuilder(name = "Library")
	}

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

		service.move(library, library.getRecursively("Elem1")!!, library.getRecursively("Folder") as LibraryFolder, 2)

		val folder = library.getRecursively("Folder") as LibraryDirectory
		assertEquals(0, folder.indexOf(library.getRecursively("Elem2")!!))
		assertEquals(1, folder.indexOf(library.getRecursively("Elem3")!!))
		assertEquals(2, folder.indexOf(library.getRecursively("Elem1")!!))
	}

	@Test
	fun shouldMoveFolderToOtherFolder() {
		libraryBuilder
			.addDirectory("DestinationFolder")
			.addContainerLibraryElement("Item1")
			.back()
			.addDirectory("MovedFolder")
			.addContainerLibraryElement("Item2")

		service.move(library, library.getRecursively("MovedFolder")!!, library.getRecursively("DestinationFolder") as LibraryDirectory, 1)

		val destinationFolder = library.getRecursively("DestinationFolder") as LibraryDirectory
		assertEquals(2, destinationFolder.size)
		assertEquals(0, destinationFolder.indexOf(destinationFolder.get("Item1")!!))
		assertEquals(1, destinationFolder.indexOf(destinationFolder.get("MovedFolder")!!))

		val origFolder = library
		assertEquals(1, origFolder.size)
	}

	@Test
	fun shouldDuplicate() {
		libraryBuilder.addContainerLibraryElement("Element")
		val orig = library.get("Element") as ContainerLibraryElement

		val duplicate = service.duplicateContainerLibraryElement(library, orig, TranslatableText("NewName"))

		assertNotEquals(orig.uuid, duplicate.uuid)
		assertEquals("Element", orig.storable!!.name)
		assertEquals("NewName", duplicate.name.value)
	}

	@Test
	fun shouldRenameContainerLibraryElement() {
		libraryBuilder.addContainerLibraryElement("OldName")
		val orig = library.get("OldName") as ContainerLibraryElement

		service.renameContainerLibraryElement(orig, TranslatableText("NewName"))

		val changed = library.get("NewName") as ContainerLibraryElement?

		assertNotNull(changed)
	}
}