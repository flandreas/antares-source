package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.dsl.Interpreter
import ch.scorpion.jabbah.base.dsl.DslLexer
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.dsl.DslParser
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryService
import ch.scorpion.jabbah.graph.library.MemoryLibraryPersistenceService
import ch.scorpion.jabbah.graph.model.GenericGraphType
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.graph.LongGraphParamType
import ch.scorpion.jabbah.graph.model.graph.StringGraphParamType
import ch.scorpion.jabbah.graph.model.param.*
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubGraphVerticeRefActivationRecordTest {

	companion object {
		init {
			GraphViewTestRule.configure()
			GraphParamTypeRegistry.register(StringGraphParamType.name) { LongGraphParamType }
		}
	}

	private val signalHandler = mockk<SignalHandler>(relaxed = true)

	@BeforeTest
	fun setup() {
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryService = LibraryService()
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
		val paramValue = GraphParamValue.create("L", LongGraphParamType, 99L)

		val vv = mockk<SubGraphVerticeRef>(relaxed = true)
		every { vv.paramValues } returns GraphParamValues().withValue(paramValue)

		val activationRecord = SubGraphVerticeRefActivationRecord(vv, signalHandler)
		val memory = Memory(activationRecord)
		val parser = DslParser(DslLexer("L"), null)
		val interpreter = Interpreter(parser.parse(), memory)

		val result = interpreter.interpret()

		assertEquals(99L, result)
	}

	private fun createSubGraphVerticeRefMock(paramValue: GraphParamValue<Long>? = null): SubGraphVerticeRef {
		val input = mockk<InputPort<Long>>()
		every { input.getIncomingSignal() } returns 42L

		val output = mockk<OutputPort<Long>>()
		val outputSlot = slot<Long>()
		every { output.setOutgoingSignalBuffered(capture(outputSlot), any()) } returns Unit

		val vv = mockk<SubGraphVerticeRef>(relaxed = true)
		every { vv.graphType } returns GenericGraphType
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