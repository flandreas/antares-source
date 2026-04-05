package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.DigitalGraph
import kotlin.test.Test
import kotlin.test.assertEquals

class TestcaseCircuitRunnerProbeTest : AbstractTestcaseCircuitRunnerTest(true) {

    @Test
    fun shouldPassAndGateCircuitTest() {
        buildAndGateCircuit()
        val testScript = """
			A B O
			0 0 0
			0 1 0
			1 0 0
			1 1 1
		""".trimIndent()

        val result = TestcaseCircuitRunner("test", testScript, circuit.graph as DigitalGraph).run()

        assertEquals(3, result.names.size)
        assertEquals(4, result.collector.size)

        for (vector in result.collector) {
            // Check state only for output columns
            assertEquals(Value.State.PASSED, vector.getValue(2).state)
        }
    }
}