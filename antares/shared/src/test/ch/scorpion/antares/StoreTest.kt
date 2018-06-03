package ch.scorpion.antares

import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.gate.OrGateView
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.library.FileLibraryPersistenceService
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.io.IOModule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import java.io.File

/**
 * Scenario tests for storing Antares circuits.
 */
class StoreTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    @Before
    fun setup() {
        val file = File.createTempFile("library", ".lib")
        TestTranslationsBuilder().withAnyKey()
        LibraryModule.libraryPersistenceService = FileLibraryPersistenceService(file.parent)
        LibraryModule.libraryHolder.l = LibraryImpl("test", libraryService = LibraryModule.libraryService.invoke())
    }

    @Test
    fun shouldBeStorable() {
        val testCircuit = TestCircuit()
        testCircuit.orGateView.location = Point2D(50, 100)

        val storable = GraphStorable(testCircuit.circuitView)
        val clone = IOModule.storableClonerProvider.invoke().cloneUsingCreator(storable, IOModule.storableCreator) as GraphStorable
        val orGateView = clone.graphView!!.getWidthId(2) as OrGateView

        assertThat(orGateView.model!!.inputCount, `is`(3))

        var portViewCount = 0
        for (portView in orGateView.getPortViews()) {
            portViewCount++
        }
        assertThat(portViewCount, `is`(4))
        assertThat(orGateView.location, `is`(Point2D(50, 100)))
        assertThat(orGateView.bounds.width, `is`(6.0 * Look.SCALE))
        assertThat(orGateView.bounds.height, `is`(8.0 * Look.SCALE))

        assertThat(testCircuit.wire.boundingBox as Rectangle2D, `is`(not(Rectangle2D())))
    }
}