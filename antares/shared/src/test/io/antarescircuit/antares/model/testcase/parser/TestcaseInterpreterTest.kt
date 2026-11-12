package io.antarescircuit.antares.model.testcase.parser

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.testcase.TestVector
import io.antarescircuit.antares.model.testcase.TestVectorCollector
import io.antarescircuit.antares.model.testcase.Value
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

	@Test
	fun shouldExpandNestedLoops() {
		val collector = interpret("""
			A B O
			loop a 0..1 {
				loop b 0..1 {
					(a) (b) (a ^ b)
				}
			}
		""".trimIndent())

		assertEquals(4, collector.size)
		assert(collector.get(0), 0UL, 0UL, 0UL)
		assert(collector.get(1), 0UL, 1UL, 1UL)
		assert(collector.get(2), 1UL, 0UL, 1UL)
		assert(collector.get(3), 1UL, 1UL, 0UL)
	}

	@Test
	fun shouldCountLoopsDownwards() {
		val collector = interpret("""
			A B O
			loop i 2..0 {
				(i) 0 0
			}
		""".trimIndent())

		assertEquals(listOf(2UL, 1UL, 0UL), collector.testVectors.map { it.getValue(0).value.toLong() })
	}

	@Test
	fun shouldExpandLoopsInsideRunBlockIntoOneRun() {
		val collector = interpret("""
			A B O
			run {
				0 0 0
				loop i 1..2 {
					(i) 0 0
				}
			}
		""".trimIndent())

		assertEquals(
			listOf(TestVector.Type.RunFirst, TestVector.Type.RunLine, TestVector.Type.RunLast),
			collector.testVectors.map { it.type }
		)
		assert(collector.get(2), 2UL, 0UL, 0UL)
	}

	@Test
	fun shouldEvaluateOperatorsWithUnsignedWrapAround() {
		val collector = interpret("""
			A B O
			(0 - 1 >> 60) (1 << 64) (~0 & 0xF0 | 0b11)
			(-1 * 2 + 3) (7 ^ 2) ^(2 + 3)
		""".trimIndent())

		assert(collector.get(0), 15UL, 0UL, 0xF3UL)
		assert(collector.get(1), 1UL, 5UL, 5UL)
		assertEquals(Value.Type.CLOCKED, collector.get(1).getValue(2).type)
	}

	private fun interpret(script: String): TestVectorCollector {
		val collector = TestVectorCollector()
		TestcaseInterpreter(TestcaseParser(script).parse() as TestScript, graphPortOwner, collector).interpret()
		return collector
	}

	private fun assert(testVector: TestVector, vararg values: ULong) {
		values.forEachIndexed { index, l ->
			val signal = testVector.getValue(index).value
			assertEquals(DigitalSignalFactory.of(signal.bitWidth, l), signal)
		}
	}
}