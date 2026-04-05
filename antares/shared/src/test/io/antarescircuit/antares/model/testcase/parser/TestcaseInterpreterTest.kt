package io.antarescircuit.antares.model.testcase.parser

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.testcase.TestVector
import io.antarescircuit.antares.model.testcase.TestVectorCollector
import io.antarescircuit.jabbah.graph.model.GraphPort
import io.antarescircuit.jabbah.graph.model.GraphPortOwner
import io.antarescircuit.jabbah.graph.model.PortType
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalUnsignedTypes::class)
class TestcaseInterpreterTest {

	private val inputPort = mock<GraphPort<Any>>()
	private val outputPort = mock<GraphPort<Any>>()

	@Suppress("UNCHECKED_CAST")
	private val graphPortOwner = object : GraphPortOwner {
		override fun <T : Any> getGraphPort(name: String): GraphPort<T>? {
			return when (name) {
				"A", "B" -> inputPort as GraphPort<T>
				"O" -> outputPort as GraphPort<T>
				else -> null
			}
		}
	}

	init {
		AntaresTestRule.configure()
		every { inputPort.portType } returns PortType.INPUT
		every { outputPort.portType } returns PortType.OUTPUT
	}

	@Test
	fun shouldProduceTestVectors() {
		val collector = TestVectorCollector()
		val parser = TestcaseParser("""
			A B O
			0 0 0
			0 1 1
		""".trimIndent())
		val interpreter = TestcaseInterpreter(parser.parse() as TestScript, graphPortOwner, collector)

		interpreter.interpret()

		assertEquals(2, collector.size)
		assert(collector.get(0), 0UL, 0UL, 0UL)
		assert(collector.get(1), 0UL, 1UL, 1UL)
	}

	@Test
	fun shouldMultiplyDontCareInputs() {
		val collector = TestVectorCollector()
		val parser = TestcaseParser("""
			A B O
			X X 0
		""".trimIndent())
		val interpreter = TestcaseInterpreter(parser.parse() as TestScript, graphPortOwner, collector)

		interpreter.interpret()

		assertEquals(4, collector.size)
		assert(collector.get(0), 0UL, 0UL, 0UL)
		assert(collector.get(1), 0UL, 1UL, 0UL)
		assert(collector.get(2), 1UL, 0UL, 0UL)
		assert(collector.get(3), 1UL, 1UL, 0UL)
	}

	@Test
	fun shouldProductRunBlock() {
		val collector = TestVectorCollector()
		val parser = TestcaseParser("""
			A B O
			0 0 0
			run {
				1 0 0
				0 1 1
			}
		""".trimIndent())
		val interpreter = TestcaseInterpreter(parser.parse() as TestScript, graphPortOwner, collector)

		interpreter.interpret()

		assertEquals(3, collector.size)

		assert(collector.get(0), 0UL, 0UL, 0UL)
		assertEquals(TestVector.Type.Top, collector.get(0).type)

		assert(collector.get(1), 1UL, 0UL, 0UL)
		assertEquals(TestVector.Type.RunFirst, collector.get(1).type)

		assert(collector.get(2), 0UL, 1UL, 1UL)
		assertEquals(TestVector.Type.RunLast, collector.get(2).type)
	}

	private fun assert(testVector: TestVector, vararg values: ULong) {
		values.forEachIndexed { index, l ->
			val signal = testVector.getValue(index).value
			assertEquals(DigitalSignalFactory.of(signal.bitWidth, l), signal)
		}
	}
}