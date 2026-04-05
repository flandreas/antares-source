package io.antarescircuit.jabbah.graph.dsl

import io.antarescircuit.jabbah.base.dsl.DslLexer
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GenericGraphType
import io.antarescircuit.jabbah.graph.model.TestVertice
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphDslInterpreterTest {

	private val signalHandler = mock<SignalHandler>(MockMode.autofill)

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
	}

	@Test
	fun shouldInterpretInitStatement() {
		val result = GraphDslInterpreter("""
			init {
				var a = 42
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
		val graph = GraphModelModule.graphFactory.create(TranslatableText("Test"), GenericGraphType)
		graph.add(vertice)

		val result = GraphDslInterpreter(
			node = GraphDslParser(DslLexer("#1:I"), null).parse()
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
		val graph = GraphModelModule.graphFactory.create(TranslatableText("Test"), GenericGraphType)
		graph.add(vertice)

		val result = GraphDslInterpreter(
			node = GraphDslParser(DslLexer("#1:O"), null).parse()
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
		val graph = GraphModelModule.graphFactory.create(TranslatableText("Test"), GenericGraphType)
		graph.add(vertice)

		val result = GraphDslInterpreter(
			node = GraphDslParser(DslLexer("#1:'I + Bla'"), null).parse()
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
		val graph = GraphModelModule.graphFactory.create(TranslatableText("Test"), GenericGraphType)
		graph.add(vertice)

		val result = GraphDslInterpreter(
			node = GraphDslParser(DslLexer("#1:1"), null).parse()
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
		val graph = GraphModelModule.graphFactory.create(TranslatableText("Test"), GenericGraphType)
		graph.add(vertice)

		val result = GraphDslInterpreter(
			node = GraphDslParser(DslLexer("#1:2"), null).parse()
		).interpret(graph)

		assertEquals(17L, result)
	}
}