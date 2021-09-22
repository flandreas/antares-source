package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.dsl.Lexer
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.vertice.GraphInputImpl
import ch.scorpion.jabbah.graph.model.vertice.GraphOutputImpl
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphDslInterpreterTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val signalHandler = mockk<SignalHandler>()

	@Test
	fun shouldInterpretInitStatement() {
		val result = GraphDslInterpreter("""
			init {
				a = 42
			}
		""".trimIndent()).executionStarted()
		assertEquals(42L, result)
	}

	@Test
	fun shouldInterpretPropertyAsInputValue() {
		val input = GraphInputImpl<Long>(name = "I")
		val graph = GraphModelModule.graphFactory("Test")
		graph.add(input)
		input.setIncomingSignal(42L, signalHandler)

		val result = GraphDslInterpreter(
			node = GraphDslParser(Lexer("#1.I"), null).parse()
		).interpret(graph)

		assertEquals(42L, result)
	}

	@Test
	fun shouldInterpretPropertyAsOutputValue() {
		val output = GraphOutputImpl<Long>(name = "O")
		val graph = GraphModelModule.graphFactory("Test")
		graph.add(output)
		output.setOutgoingSignal(17L, signalHandler)

		val result = GraphDslInterpreter(
			node = GraphDslParser(Lexer("#1.O"), null).parse()
		).interpret(graph)

		assertEquals(17L, result)
	}

	@Test
	fun shouldInterpretPropertyWithQuotedPortName() {
		val input = GraphInputImpl<Long>(name = "I + Bla")
		val graph = GraphModelModule.graphFactory("Test")
		graph.add(input)
		input.setIncomingSignal(42L, signalHandler)

		val result = GraphDslInterpreter(
			node = GraphDslParser(Lexer("#1.'I + Bla'"), null).parse()
		).interpret(graph)

		assertEquals(42L, result)
	}
}