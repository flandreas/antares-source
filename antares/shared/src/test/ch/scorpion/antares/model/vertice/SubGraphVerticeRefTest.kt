package ch.scorpion.antares.model.vertice

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestLibraryBuilder
import ch.scorpion.antares.TestTranslationsBuilder
import ch.scorpion.jabbah.graph.library.FileLibraryPersistenceService
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.io.IOModule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import java.io.File

/**
 * Unit tests for [SubGraphVerticeRef] using Antares libraries and components.
 */
class SubGraphVerticeRefTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    @Before
    fun setup() {
        val file = File.createTempFile("library", ".lib")
        TestTranslationsBuilder().withAnyKey()
        LibraryModule.libraryPersistenceService = FileLibraryPersistenceService(file.parentFile.absolutePath)
        LibraryModule.libraryHolder.l = LibraryImpl("test", libraryService = LibraryModule.libraryService.invoke())
    }

    @Test
    fun shouldBind() {
        // Add a custom NOT circuit to the Library
        TestLibraryBuilder().addCustomNot(LibraryModule.libraryHolder.library)

        // Create a new instance of the custom NOT circuit in the Library
        val customNOT = (LibraryModule.libraryHolder.library.get(TestLibraryBuilder.CUSTOM_NOT) as LibraryElement).getNewInstance<SubGraphVerticeRef>()

        customNOT.model!!.bind(LibraryModule.libraryHolder.library, IOModule.storableCreator)

        val libraryGraph = LibraryModule.libraryHolder.library.getMetaGraph(customNOT.model!!.graphUUID!!).graph.model!!
        val customGraph = customNOT.model!!.getGraph(LibraryModule.libraryHolder.library, IOModule.storableCreator)

        for (i in 1..5) {
            assertThat(libraryGraph.withId(i)!!.storableId, `is`(customGraph.withId(i)!!.storableId))
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

        customNAND.model!!.bind(LibraryModule.libraryHolder.library, IOModule.storableCreator)

        val libraryGraph = LibraryModule.libraryHolder.library.getMetaGraph(customNAND.model!!.graphUUID!!).graph.model!!
        val customGraph = customNAND.model!!.getGraph(LibraryModule.libraryHolder.library, IOModule.storableCreator)

        for (i in 1..6) {
            assertThat(libraryGraph.withId(i)!!.storableId, `is`(customGraph.withId(i)!!.storableId))
        }
    }
}