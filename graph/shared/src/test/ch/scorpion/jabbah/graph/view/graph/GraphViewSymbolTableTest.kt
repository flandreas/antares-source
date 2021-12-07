package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.graph.model.graph.GraphSymbolTable
import ch.scorpion.jabbah.graph.model.graph.StringGraphParamType
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinition
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphPortView
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GraphViewSymbolTableTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val builder = GraphViewBuilder<Boolean>("Test")

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