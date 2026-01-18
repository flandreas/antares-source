package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphModelTestRule
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.vertice.GraphPortName.createPastedName
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GraphPortNameTest {

	@BeforeTest
	fun setup() {
		GraphModelTestRule.configure()
	}

	@Test
	fun shouldCreateStructure() {
		assertStructure("Input", 4, GraphPortName.createStructure("Input4"))
		assertStructure("Input ", 4, GraphPortName.createStructure("Input 4"))
	}

	@Test
	fun shouldNotCreateStructure() {
		assertNull(GraphPortName.createStructure("Bla"))
		assertNull(GraphPortName.createStructure("3A"))
		assertNull(GraphPortName.createStructure("A5B"))
	}

	private fun assertStructure(text: String, number: Int, structure: GraphPortNameStructure?) {
		assertEquals(text, structure?.text)
		assertEquals(number, structure?.number)
	}

	@Test
	fun shouldCreateUnqualifiedPastedName() {
		assertEquals("Test (3)", createPastedName("Test", graph("Test", "Test (2)")))
	}

	@Test
	fun shouldCreateQualifiedPastedName() {
		assertEquals("I6", createPastedName("I4", graph("I4", "I5")))
		assertEquals("Out 2", createPastedName("Out 1", graph("Out 1")))
	}

	private fun graph(vararg names: String): Graph {
		val graph = mock<Graph>()
		val graphPorts = names
			.map { name -> mock<GraphPort<*>>().also { every { it.name } returns name } }
			.toImmutableList()
		every { graph.graphPorts } returns graphPorts
		return graph
	}
}