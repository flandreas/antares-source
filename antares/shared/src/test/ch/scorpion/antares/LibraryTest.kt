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
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import java.io.File

/**
 * Scenario (integration) tests for [Library] and corresponding classes.
 * Uses Antares components in order to achieve realistic scenarios without mocking burden.
 * TODO Write Antares-neutral test class in the jabbah module
 */
class LibraryTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    @Before
    fun setup() {
        val file = File.createTempFile("library", ".lib")
        TestTranslationsBuilder().withAnyKey()
        LibraryModule.libraryService = FileLibraryService(file.parent)
        LibraryModule.libraryHolder.l = LibraryImpl(file.toPath().fileName.toString(), file.toPath().parent.toString())
    }

    @Test
    fun shouldStoreAndLoadLibraryWithSubGraph() {
        val customNot = TestLibraryBuilder().addCustomNot(LibraryModule.libraryHolder.library)
        val restoredLibrary = storeAndLoad(LibraryModule.libraryHolder.library as LibraryImpl)
        assertThat(libraryHolder.library, `is`(not(sameInstance(restoredLibrary))))
        LibraryModule.libraryHolder.l = restoredLibrary
        restoredLibrary.getMetaGraph(customNot.uuid)
    }

    @Test
    fun shouldInstantiateSubCircuit() {
        TestLibraryBuilder().addCustomNot(LibraryModule.libraryHolder.library)

        val item = libraryHolder.library.get(TestLibraryBuilder.CUSTOM_NOT) as LibraryElement
        val vvr = item.getNewInstance<SubGraphVertice>() as SubGraphVerticeView

        assertThat(vvr.model!!.name, `is`(TestLibraryBuilder.CUSTOM_NOT))
        assertThat(vvr.model!!.getInput<DigitalSignal>().name, `is`("I1"))
        assertThat(vvr.model!!.getOutput<DigitalSignal>().name, `is`("O1"))

        for (portView in vvr.getPortViews()) {
            assertThat(portView.port as Port<DigitalSignal>, `is`(sameInstance(vvr.model!!.getPort<DigitalSignal>(portView.port.name!!))))
        }
    }

    @Test
    fun shouldStoreAndLoadLibraryWithNestedSubCircuit() {
        val customNot = TestLibraryBuilder().addCustomNot(libraryHolder.library)
        val customNand = TestLibraryBuilder().addCustomNand(libraryHolder.library)

        val restoredLibrary = storeAndLoad(libraryHolder.library as LibraryImpl)
        LibraryModule.libraryHolder.l = restoredLibrary

        restoredLibrary.getMetaGraph(customNot.uuid)
        restoredLibrary.getMetaGraph(customNand.uuid)

        val nandItem = restoredLibrary.get(TestLibraryBuilder.CUSTOM_NAND) as LibraryElement
        val vvr = nandItem.getNewInstance<SubGraphVertice>() as SubGraphVerticeView

        assertThat(vvr.model!!.name, `is`(TestLibraryBuilder.CUSTOM_NAND))
        assertThat(vvr.model!!.getInput<DigitalSignal>(1).name, `is`("I1"))
        assertThat(vvr.model!!.getInput<DigitalSignal>(2).name, `is`("I2"))
        assertThat(vvr.model!!.getOutput<DigitalSignal>().name, `is`("O1"))

        for (portView in vvr.getPortViews()) {
            assertThat(portView.port as Port<DigitalSignal>, `is`(sameInstance(vvr.model!!.getPort<DigitalSignal>(portView.port.name!!))))
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

    private fun storeAndLoad(library: LibraryImpl): Library {
        library.store()
        val loadedLibrary = LibraryImpl(library.fileName, library.locationPath)
        loadedLibrary.load()
        return loadedLibrary
    }
}