package io.antarescircuit.jabbah.graph.view.graph

import io.antarescircuit.jabbah.graph.model.graph.GraphSymbolTable
import io.antarescircuit.jabbah.graph.model.param.StringGraphParamType
import io.antarescircuit.jabbah.graph.model.param.GraphParamDefinition
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.TestGraphPortView
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GraphViewSymbolTableTest {

	private val builder: GraphViewBuilder<Boolean>

	init {
		GraphViewTestRule.configure()
		builder = GraphViewBuilder<Boolean>("Test")
	}

	@Test
	fun shouldContainPortNames() {
		builder.addVerticeView(TestGraphPortView.input<Int>("I"))
		builder.addVerticeView(TestGraphPortView.output<Int>("O"))
		val symbolTable = GraphSymbolTable(builder.graphView.graph!!)

		assertTrue(symbolTable.hasSymbol("I"))
		assertTrue(symbolTable.hasSymbol("O"))
		assertFalse(symbolTable.hasSymbol("X"))
	}

	@Test
	fun shouldContainGraphParams() {
		builder.graph.parameterDefinitions = builder.graph.parameterDefinitions.withDefinition(
			GraphParamDefinition.create("P", StringGraphParamType, "Test"))
		val symbolTable = GraphSymbolTable(builder.graphView.graph!!)

		assertTrue(symbolTable.hasSymbol("P"))
		assertFalse(symbolTable.hasSymbol("X"))
	}
}