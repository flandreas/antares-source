package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.dsl.Lexer
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
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

	private val signalHandler = mockk<SignalHandler>(relaxed = true)

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
	fun shouldInterpretPortNamePropertyAsInputValue() {
		val vertice = TestVertice()
		vertice.getInput<Long>().run {
			name = "I"
			setIncomingSignal(42L, signalHandler)
		}
		val graph = GraphModelModule.graphFactory("Test")
		graph.add(vertice)

		val result = GraphDslInterpreter(
			node = GraphDslParser(Lexer("#1.I"), null).parse()
		).interpret(graph)

		assertEquals(42L, result)
	}

	@Test
	fun shouldInterpretPortNamePropertyAsOutputValue() {
		val vertice = TestVertice()
		vertice.getOutput<Long>().run {
			name = "O"
			setOutgoingSignal(17L, signalHandler)
		}
		val graph = GraphModelModule.graphFactory("Test")
		graph.add(vertice)

		val result = GraphDslInterpreter(
			node = GraphDslParser(Lexer("#1.O"), null).parse()
		).interpret(graph)

		assertEquals(17L, result)
	}

	@Test
	fun shouldInterpretQuotedPortNamePropertyAsInputValue() {
		val vertice = TestVertice()
		vertice.getInput<Long>().run {
			name = "I + Bla"
			setIncomingSignal(42L, signalHandler)
		}
		val graph = GraphModelModule.graphFactory("Test")
		graph.add(vertice)

		val result = GraphDslInterpreter(
			node = GraphDslParser(Lexer("#1.'I + Bla'"), null).parse()
		).interpret(graph)

		assertEquals(42L, result)
	}

	@Test
	fun shouldInterpretPortIdPropertyAsInputValue() {
		val vertice = TestVertice()
		vertice.getInput<Long>().run {
			name = "I"
			setIncomingSignal(42L, signalHandler)
		}
		val graph = GraphModelModule.graphFactory("Test")
		graph.add(vertice)

		val result = GraphDslInterpreter(
			node = GraphDslParser(Lexer("#1.1"), null).parse()
		).interpret(graph)

		assertEquals(42L, result)
	}

	@Test
	fun shouldInterpretPortIdPropertyAsOutputValue() {
		val vertice = TestVertice()
		vertice.getOutput<Long>().run {
			name = "O"
			setOutgoingSignal(17L, signalHandler)
		}
		val graph = GraphModelModule.graphFactory("Test")
		graph.add(vertice)

		val result = GraphDslInterpreter(
			node = GraphDslParser(Lexer("#1.2"), null).parse()
		).interpret(graph)

		assertEquals(17L, result)
	}
}