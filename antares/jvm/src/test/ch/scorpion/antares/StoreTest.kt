package ch.scorpion.antares

import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.library.FileLibraryPersistenceService
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.io.StorableCloner
import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.File
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.test.BeforeTest
import kotlin.test.assertNotSame

/**
 * Scenario tests for storing Antares circuits.
 */
class StoreTest {

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
		LibraryModule.libraryHolder.l = LibraryImpl("test")
	}

	@Test
	fun shouldBeStorable() {
		val testCircuit = TestCircuit()
		testCircuit.orGateView.location = Point2D(50, 100)

		val storable = GraphStorable(testCircuit.circuitView)
		val clone = StorableCloner.clone(storable)
		val orGateView = clone.graphView.getWithId(2) as LogicGateView

		assertEquals(3, orGateView.model.inputCount)

		var portViewCount = 0
		for (portView in orGateView.getPortViews()) {
			portViewCount++
		}
		assertEquals(4, portViewCount)
		assertEquals(Point2D(50, 100), orGateView.location)
		assertEquals(6.0 * Look.SCALE, orGateView.bounds.width)
		assertEquals(8.0 * Look.SCALE, orGateView.bounds.height)

		assertNotSame(testCircuit.wire.boundingBox as Rectangle2D, Rectangle2D())
	}
}