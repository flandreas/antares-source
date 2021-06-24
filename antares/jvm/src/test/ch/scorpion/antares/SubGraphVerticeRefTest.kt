package ch.scorpion.antares

import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.io.IOModule
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [SubGraphVerticeRef] using Antares libraries and components.
 */
class SubGraphVerticeRefTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@BeforeTest
	fun setup() {
		val file = File.createTempFile("library", ".lib")
		LibraryModule.userLibraryPersistenceService = FileLibraryPersistenceService(file.parentFile.absolutePath)
		LibraryModule.libraryService = LibraryService()
		LibraryModule.libraryHolder.l = LibraryImpl("test", libraryService = LibraryModule.libraryService)
	}

	@Test
	fun shouldBind() {
		// Add a custom NOT circuit to the Library
		TestLibraryBuilder().addCustomNot(LibraryModule.libraryHolder.library)

		// Create a new instance of the custom NOT circuit in the Library
		val customNOT = (LibraryModule.libraryHolder.library.get(TestLibraryBuilder.CUSTOM_NOT) as LibraryElement).getNewInstance<SubGraphVerticeRef>()

		customNOT.model.bind(LibraryModule.libraryHolder.library, IOModule.storableCreator)

		val libraryGraph = LibraryModule.libraryHolder.library.getMetaGraph(customNOT.model.graphUUID!!).graph.model!!
		val customGraph = customNOT.model.getGraph(LibraryModule.libraryHolder.library, IOModule.storableCreator)

		for (i in 1..5) {
			assertEquals(libraryGraph.withId(i)!!.id, customGraph.withId(i)!!.id)
		}
	}

	@Test
	fun shouldBindNested() {
		// Add a custom NOT circuit to the Library
		TestLibraryBuilder().addCustomNot(LibraryModule.libraryHolder.library)

		// Add a custom NAND circuit that uses the custom NOT circuit in the library
		TestLibraryBuilder().addCustomNand(LibraryModule.libraryHolder.library)

		// Create a new instance of the custom NAND circuit
		val customNAND = (LibraryModule.libraryHolder.library.get(TestLibraryBuilder.CUSTOM_NAND) as LibraryElement).getNewInstance<SubGraphVerticeRef>()

		customNAND.model.bind(LibraryModule.libraryHolder.library, IOModule.storableCreator)

		val libraryGraph = LibraryModule.libraryHolder.library.getMetaGraph(customNAND.model.graphUUID!!).graph.model!!
		val customGraph = customNAND.model.getGraph(LibraryModule.libraryHolder.library, IOModule.storableCreator)

		for (i in 1..6) {
			assertEquals(libraryGraph.withId(i)!!.id, customGraph.withId(i)!!.id)
		}
	}
}