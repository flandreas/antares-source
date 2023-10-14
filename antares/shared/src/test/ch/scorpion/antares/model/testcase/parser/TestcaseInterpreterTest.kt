package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.testcase.TestVector
import ch.scorpion.antares.model.testcase.TestVectorCollector
import kotlin.test.Test
import kotlin.test.assertEquals

class TestcaseInterpreterTest {

	@Test
	fun shouldProduceTestVectors() {
		val collector = TestVectorCollector()
		val parser = TestcaseParser("""
			A B O
			0 0 0
			0 1 1
		""".trimIndent())
		val interpreter = TestcaseInterpreter(parser.parse() as TestScript, collector)

		interpreter.interpret()

		assertEquals(2, collector.size)
		assert(collector.get(0), 0UL, 0UL, 0UL)
		assert(collector.get(1), 0UL, 1UL, 1UL)
	}

	private fun assert(testVector: TestVector, vararg values: ULong) {
		values.forEachIndexed { index, l ->
			assertEquals(l, testVector.getValue(index).value)
		}
	}
}