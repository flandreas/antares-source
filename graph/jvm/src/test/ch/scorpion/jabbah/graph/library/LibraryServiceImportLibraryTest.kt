package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.TempFileLibraryTestRule
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import kotlin.test.*


class LibraryServiceImportLibraryTest {

	private var libraryImportEventSent: Boolean = false

	private val libraryImportsHandler: EventHandler<LibraryImportsEvent> = { libraryImportEventSent = true }

	@BeforeTest
	fun setup() {
		TempFileLibraryTestRule.configure()
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

		LibraryModule.libraryHolder.library.libraryService.addImport(LibraryModule.libraryHolder.library, libraryB.uuid)

		assertTrue(LibraryModule.libraryHolder.library.importedLibraryIds.contains(libraryB.uuid))
		assertTrue(LibraryModule.libraryService.loadLibrary(LibraryModule.libraryHolder.library.identification, isSystem = false).importedLibraryIds.contains(libraryB.uuid))
		assertTrue(libraryImportEventSent)
	}

	@Test
	fun shouldRemoveImportedLibrary() {
		TempFileLibraryTestRule.createAndEstablishCurrentLibrary("C")
		val libraryD = TempFileLibraryTestRule.createLibrary("D")
		LibraryModule.libraryHolder.library.libraryService.addImport(LibraryModule.libraryHolder.library, libraryD.uuid)

		LibraryModule.libraryManagementService.removeImport(libraryD.uuid, emptySet())

		assertFalse(LibraryModule.libraryHolder.library.importedLibraryIds.contains(libraryD.uuid))
		assertFalse(LibraryModule.libraryService.loadLibrary(LibraryModule.libraryHolder.library.identification, isSystem = false).importedLibraryIds.contains(libraryD.uuid))
		assertTrue(libraryImportEventSent)
	}

	@Test
	fun shouldDetectLibraryReference() {
		val libraryE = TempFileLibraryTestRule.createLibrary("E")
		val builder = TestLibraryBuilder()
		builder.addInnerCustomComponent(libraryE)

		TempFileLibraryTestRule.createAndEstablishCurrentLibrary("F")
		LibraryModule.libraryHolder.library.libraryService.addImport(LibraryModule.libraryHolder.library, libraryE.uuid)

		builder.addOuterCustomComponent(LibraryModule.libraryHolder.library, innerLibrary = libraryE)

		// F contains a MetaGraph with a SubGraphVerticeRef E
		assertTrue(LibraryModule.libraryHolder.library.libraryService.evaluateLibraryReferences(LibraryModule.libraryHolder.library, libraryE).hasNonSystemReferences)
	}
}