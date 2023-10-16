package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.testcase.TestVector
import ch.scorpion.antares.model.testcase.TestVectorCollector
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.GraphPortOwner
import ch.scorpion.jabbah.graph.model.PortType
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalUnsignedTypes::class)
class TestcaseInterpreterTest {

	private val inputPort = mockk<GraphPort<Any>>()
	private val outputPort = mockk<GraphPort<Any>>()

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

	private fun assert(testVector: TestVector, vararg values: ULong) {
		values.forEachIndexed { index, l ->
			assertEquals(l, testVector.getValue(index).value)
		}
	}
}