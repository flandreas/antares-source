package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.TempFileLibraryTestRule
import kotlin.test.*

class LibraryManagementServiceTest {

	companion object {
		init {
			TempFileLibraryTestRule.configure()
		}
	}

	private var libraryImportEventSent: Boolean = false

	private val libraryImportsHandler: EventHandler<LibraryImportsEvent> = { libraryImportEventSent = true }

	@BeforeTest
	fun setup() {
		BaseModule.eventBus.register(LibraryImportsEvent::class, libraryImportsHandler)
		libraryImportEventSent = false
	}

	@AfterTest
	fun tearDown() {
		BaseModule.eventBus.unregister(libraryImportsHandler)
	}

	@Test
	fun shouldAddImportedLibrary() {
		TempFileLibraryTestRule.createAndEstablishCurrentLibrary("A")
		val libraryB = TempFileLibraryTestRule.createLibrary("B")

		LibraryModule.libraryManagementService.addImport(libraryB.uuid)

		assertTrue(LibraryModule.libraryHolder.library.importedLibraryIds.contains(libraryB.uuid))
		assertTrue(LibraryModule.libraryService.loadLibrary(LibraryModule.libraryHolder.library.identification, isSystem = false).importedLibraryIds.contains(libraryB.uuid))
		assertTrue(libraryImportEventSent)
	}

	@Test
	fun shouldRemoveImportedLibrary() {
		TempFileLibraryTestRule.createAndEstablishCurrentLibrary("A")
		val libraryB = TempFileLibraryTestRule.createLibrary("B")
		LibraryModule.libraryManagementService.addImport(libraryB.uuid)

		LibraryModule.libraryManagementService.removeImport(libraryB.uuid)

		assertFalse(LibraryModule.libraryHolder.library.importedLibraryIds.contains(libraryB.uuid))
		assertFalse(LibraryModule.libraryService.loadLibrary(LibraryModule.libraryHolder.library.identification, isSystem = false).importedLibraryIds.contains(libraryB.uuid))
		assertTrue(libraryImportEventSent)
	}
}