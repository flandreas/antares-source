package io.antarescircuit.jabbah.graph.model.vertice

import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.dsl.DslLexer
import io.antarescircuit.jabbah.base.dsl.DslParser
import io.antarescircuit.jabbah.base.dsl.Interpreter
import io.antarescircuit.jabbah.base.dsl.Memory
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.library.LibraryImpl
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.MemoryLibraryPersistenceService
import io.antarescircuit.jabbah.graph.model.GenericGraphType
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.InputPort
import io.antarescircuit.jabbah.graph.model.OutputPort
import io.antarescircuit.jabbah.graph.model.param.GraphParamValue
import io.antarescircuit.jabbah.graph.model.param.GraphParamValues
import io.antarescircuit.jabbah.graph.model.param.LongValueGraphParamType
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubGraphVerticeRefActivationRecordTest {

	private val signalHandler = mock<SignalHandler>(MockMode.autofill)

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
	}

	@Test
	fun shouldAccessGraphPortsAsVariables() {
		val vv = createSubGraphVerticeRefMock()

		val activationRecord = SubGraphVerticeRefActivationRecord(vv, signalHandler)
		val memory = Memory(activationRecord)

		// Use a Parser without semantic analysis, because SemanticAnalyser must also deal with the challenge
		// of predefining GraphPorts names as variables in the symbol table, but to test that is the
		// responsibility of another test
		val parser = DslParser(DslLexer("O = 2 * I"), null)
		val interpreter = Interpreter(parser.parse(), memory)

		val result = interpreter.interpret()

		assertEquals(84L, result)
	}

	@Test
	fun shouldAccessGraphParamsAsVariables() {
		val paramValue = GraphParamValue.create("L", LongValueGraphParamType, LongValueImpl(99L), null)

		val vv = mock<SubGraphVerticeRefIF>(MockMode.autofill)
		every { vv.paramValues } returns GraphParamValues().withValue(paramValue)

		val activationRecord = SubGraphVerticeRefActivationRecord(vv, signalHandler)
		val memory = Memory(activationRecord)
		val parser = DslParser(DslLexer("L"), null)
		val interpreter = Interpreter(parser.parse(), memory)

		val result = interpreter.interpret()

		assertEquals(99L, result)
	}

	private fun createSubGraphVerticeRefMock(paramValue: GraphParamValue<Long>? = null): SubGraphVerticeRefIF {
		val input = mock<InputPort<Long>>()
		every { input.getIncomingSignal() } returns 42L

		val output = mock<OutputPort<Long>>()
		val outputSlot = Capture.slot<Long>()
		every { output.setOutgoingSignalBuffered(capture(outputSlot), any()) } returns Unit

		val graph = mock<Graph>()
		every { graph.type } returns GenericGraphType

		val vv = mock<SubGraphVerticeRefIF>(MockMode.autofill)
		every { vv.graphType } returns GenericGraphType
		every { vv.getGraph() } returns graph
		every { vv.hasPort(any<String>()) } returns true
		every { vv.hasPort(any<Int>()) } returns true
		every { vv.hasInput(any()) } returns true
		every { vv.hasOutput(any()) } returns true
		every { vv.getInput<Long>(any<String>()) } returns input
		every { vv.getOutput<Long>(any<String>())} returns output
		paramValue?.let {
			every { vv.paramValues } returns GraphParamValues().withValue(it)
		}

		return vv
	}
}