package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class TestcaseCircuitRunnerLoopTest : AbstractTestcaseCircuitRunnerTest(false) {

	@Test
	fun shouldPassExhaustiveAndGateTestWithLoops() {
		buildAndGateCircuit()

		val result = run("""
			A B O
			loop a 0..1 {
				loop b 0..1 {
					(a) (b) (a & b)
				}
			}
		""".trimIndent())

		assertEquals(4, result.collector.size)
		result.collector.testVectors.forEach { assertEquals(Value.State.PASSED, it.getValue(2).state) }
	}

	@Test
	fun shouldFailExhaustiveAndGateTestWithWrongExpectation() {
		buildAndGateCircuit()

		val result = run("""
			A B O
			loop a 0..1 {
				loop b 0..1 {
					(a) (b) (a | b)
				}
			}
		""".trimIndent())

		assertEquals(listOf(Value.State.PASSED, Value.State.FAILED, Value.State.FAILED, Value.State.PASSED), result.collector.testVectors.map { it.getValue(2).state })
	}

	@Test
	fun shouldMaskExpressionValuesToPortBitWidth() {
		buildMultiBitNOPCircuit()

		val result = run("""
			I O
			loop i 0..255 {
				(i + 0x100) (i)
			}
		""".trimIndent())

		assertEquals(256, result.collector.size)
		result.collector.testVectors.forEachIndexed { i, vector ->
			assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, i.toLong()), vector.getValue(0).value)
			assertEquals(Value.State.PASSED, vector.getValue(1).state)
		}
	}

	private fun run(script: String): TestRunResult =
		TestcaseCircuitRunner("test", script, circuit.graph as DigitalGraph).run()
}
