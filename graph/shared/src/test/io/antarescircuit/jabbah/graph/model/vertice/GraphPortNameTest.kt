package io.antarescircuit.jabbah.graph.model.vertice

import io.antarescircuit.jabbah.base.collection.toImmutableList
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphModelTestRule
import io.antarescircuit.jabbah.graph.model.GraphPort
import io.antarescircuit.jabbah.graph.model.vertice.GraphPortName.createPastedName
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphPortNameTest {

	@BeforeTest
	fun setup() {
		GraphModelTestRule.configure()
	}

	@Test
	fun shouldCreateStructure() {
		assertStructure("Input", 4, GraphPortName.createStructure("Input4"))
		assertStructure("Input ", 3, GraphPortName.createStructure("Input 3"))
		assertStructure("Bla", -1, GraphPortName.createStructure("Bla"))
	}

	private fun assertStructure(text: String, number: Int, structure: GraphPortNameStructure?) {
		assertEquals(text, structure?.text)
		assertEquals(number, structure?.number)
	}

	@Test
	fun shouldCreateQualifiedPastedName() {
		assertEquals("I2", createPastedName("I", graph("I")))
		assertEquals("I2", createPastedName("I1", graph("I1")))
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