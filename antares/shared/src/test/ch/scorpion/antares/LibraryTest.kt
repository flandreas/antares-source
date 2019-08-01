package ch.scorpion.antares

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.LibraryModule.libraryHolder
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.IOModule
import java.io.File
import java.nio.file.Files
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
		val dir = Files.createTempDirectory("library")
		val file = File.createTempFile("library", ".lib", dir.toFile())
		LibraryModule.libraryPersistenceService = FileLibraryPersistenceService(file.parent)
		LibraryModule.libraryHolder.l = LibraryImpl("test", libraryService = LibraryModule.libraryService.invoke())
	}

	@Test
	fun shouldStoreAndLoadLibraryWithSubGraph() {
		val customNot = TestLibraryBuilder().addCustomNot(LibraryModule.libraryHolder.library)
		val restoredLibrary = storeAndLoad(LibraryModule.libraryHolder.library as LibraryImpl, LibraryModule.libraryService.invoke())
		assertNotSame(libraryHolder.library, restoredLibrary)
		LibraryModule.libraryHolder.l = restoredLibrary
		restoredLibrary.getMetaGraph(customNot.uuid)
	}

	@Test
	fun shouldInstantiateSubCircuit() {
		TestLibraryBuilder().addCustomNot(LibraryModule.libraryHolder.library)

		val item = libraryHolder.library.get(TestLibraryBuilder.CUSTOM_NOT) as LibraryElement
		val vvr = item.getNewInstance<SubGraphVertice>() as SubGraphVerticeView

		assertEquals(TestLibraryBuilder.CUSTOM_NOT, vvr.model!!.name)
		assertEquals("I1", vvr.model!!.getInput<DigitalSignal>().name)
		assertEquals("O1", vvr.model!!.getOutput<DigitalSignal>().name)

		for (portView in vvr.getPortViews()) {
			assertSame(portView.port as Port<DigitalSignal>, vvr.model!!.getPort(portView.port.name!!))
		}
	}

	@Test
	fun shouldStoreAndLoadLibraryWithNestedSubCircuit() {
		val customNot = TestLibraryBuilder().addCustomNot(libraryHolder.library)
		val customNand = TestLibraryBuilder().addCustomNand(libraryHolder.library)

		val restoredLibrary = storeAndLoad(libraryHolder.library as LibraryImpl, LibraryModule.libraryService.invoke())
		LibraryModule.libraryHolder.l = restoredLibrary

		restoredLibrary.getMetaGraph(customNot.uuid)
		restoredLibrary.getMetaGraph(customNand.uuid)

		val nandItem = restoredLibrary.get(TestLibraryBuilder.CUSTOM_NAND) as LibraryElement
		val vvr = nandItem.getNewInstance<SubGraphVertice>() as SubGraphVerticeView

		assertEquals(TestLibraryBuilder.CUSTOM_NAND, vvr.model!!.name)
		assertEquals("I1", vvr.model!!.getInput<DigitalSignal>(1).name)
		assertEquals("I2", vvr.model!!.getInput<DigitalSignal>(2).name)
		assertEquals("O1", vvr.model!!.getOutput<DigitalSignal>().name)

		for (portView in vvr.getPortViews()) {
			assertSame(portView.port as Port<DigitalSignal>, vvr.model!!.getPort(portView.port.name!!))
		}
	}

	@Test
	fun shouldStoreAndLoadCircuitWithNestedSubCircuit() {
		TestLibraryBuilder().addCustomNot(libraryHolder.library)
		TestLibraryBuilder().addCustomNand(libraryHolder.library)

		val nandItem = libraryHolder.library.get(TestLibraryBuilder.CUSTOM_NAND) as LibraryElement
		val vvr = nandItem.getNewInstance<SubGraphVertice>() as SubGraphVerticeView

		val circuitView = GraphViewImpl<GraphElementView<*>>()
		circuitView.add(vvr)

		var graphStorable = GraphStorable(circuitView)
		graphStorable = IOModule.storableClonerProvider.invoke().cloneUsingCreator(graphStorable, IOModule.storableCreator) as GraphStorable
	}

	@Test
	fun shouldExportLibrary() {
		TestLibraryBuilder().addCustomNot(LibraryModule.libraryHolder.library)
		val file = File.createTempFile("library", ".zip")
		LibraryModule.libraryPersistenceService.exportLibrary(LibraryModule.libraryHolder.library.name, file.absolutePath)
	}

	private fun storeAndLoad(library: LibraryImpl, service: LibraryService): Library {
		service.storeLibrary(library)
		return LibraryModule.libraryService.invoke().loadLibrary("test")
	}
}