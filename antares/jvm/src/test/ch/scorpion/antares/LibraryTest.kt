package ch.scorpion.antares

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.LibraryModule.libraryHolder
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.StorableCloner
import java.io.File
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.test.*

/**
 * Scenario (integration) tests for [Library] and corresponding classes.
 * Uses Antares components in order to achieve realistic scenarios without mocking burden.
 * TODO Write Antares-neutral test class in the jabbah module
 */
class LibraryTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@BeforeTest
	fun setup() {
		val dir = Files.createTempDirectory(null)
		File.createTempFile("library", ".lib", dir.toFile())
		LibraryModule.userLibraryPersistenceService = FileLibraryPersistenceService({ dir.parent.absolutePathString() }, dir.name)
		LibraryModule.libraryService = LibraryService(userLibraryPersisterProvider = { LibraryModule.userLibraryPersistenceService })
		libraryHolder.l = LibraryImpl("test")
	}

	@Test
	fun shouldStoreAndLoadLibraryWithSubGraph() {
		val customNot = TestLibraryBuilder().addCustomNot(libraryHolder.library)
		val restoredLibrary = storeAndLoad(libraryHolder.library as LibraryImpl, LibraryModule.libraryService)
		assertNotSame(libraryHolder.library, restoredLibrary)
		libraryHolder.l = restoredLibrary
		restoredLibrary.getMetaGraph(customNot.uuid)
	}

	@Test
	fun shouldInstantiateSubCircuit() {
		TestLibraryBuilder().addCustomNot(libraryHolder.library)

		val item = libraryHolder.library.get(TestLibraryBuilder.CUSTOM_NOT) as LibraryElement
		val vvr = item.getNewInstance<SubGraphVertice>() as SubGraphVerticeView

		assertEquals(TestLibraryBuilder.CUSTOM_NOT, vvr.type)
		assertEquals("I1", vvr.model.getInput<DigitalSignal>().name)
		assertEquals("O1", vvr.model.getOutput<DigitalSignal>().name)

		for (portView in vvr.getPortViews()) {
			assertSame(portView.port as Port<DigitalSignal>, vvr.model.getPort(portView.port.name!!))
		}
	}

	@Test
	fun shouldStoreAndLoadLibraryWithNestedSubCircuit() {
		val customNot = TestLibraryBuilder().addCustomNot(libraryHolder.library)
		val customNand = TestLibraryBuilder().addCustomNand(libraryHolder.library)

		val restoredLibrary = storeAndLoad(libraryHolder.library as LibraryImpl, LibraryModule.libraryService)
		libraryHolder.l = restoredLibrary

		restoredLibrary.getMetaGraph(customNot.uuid)
		restoredLibrary.getMetaGraph(customNand.uuid)

		val nandItem = restoredLibrary.get(TestLibraryBuilder.CUSTOM_NAND) as LibraryElement
		val vvr = nandItem.getNewInstance<SubGraphVertice>() as SubGraphVerticeView

		assertEquals(TestLibraryBuilder.CUSTOM_NAND, vvr.type)
		assertEquals("I1", vvr.model.getInput<DigitalSignal>(1).name)
		assertEquals("I2", vvr.model.getInput<DigitalSignal>(2).name)
		assertEquals("O1", vvr.model.getOutput<DigitalSignal>().name)

		for (portView in vvr.getPortViews()) {
			assertSame(portView.port as Port<DigitalSignal>, vvr.model.getPort(portView.port.name!!))
		}
	}

	@Test
	fun shouldStoreAndLoadCircuitWithNestedSubCircuit() {
		TestLibraryBuilder().addCustomNot(libraryHolder.library)
		TestLibraryBuilder().addCustomNand(libraryHolder.library)

		val nandItem = libraryHolder.library.get(TestLibraryBuilder.CUSTOM_NAND) as LibraryElement
		val vvr = nandItem.getNewInstance<SubGraphVertice>() as SubGraphVerticeView

		val circuitView = GraphViewImpl()
		circuitView.add(vvr)

		val graphStorable = GraphStorable(circuitView)
		StorableCloner.clone(graphStorable)
	}

	@Test
	fun shouldExportLibrary() {
		TestLibraryBuilder().addCustomNot(libraryHolder.library)
		val file = File.createTempFile("library", ".zip")
		LibraryModule.userLibraryPersistenceService.exportLibrary(libraryHolder.library.identification, file.absolutePath)
	}

	private fun storeAndLoad(library: LibraryImpl, service: LibraryService): Library {
		service.storeLibrary(library)
		return LibraryModule.libraryService.loadLibrary(library.identification, isSystem = false)
	}
}