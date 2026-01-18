package ch.scorpion.antares.filebased

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.testcase.TestcaseCircuitRunner
import ch.scorpion.antares.model.testcase.Value
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DFlipFlopTestcase : AbstractFileBasedTest() {

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("b1089cae-01cc-4c2f-813a-4a61b0cf7c16"))
	}

	@Test
	fun shouldRunTestcase() {
		// C intentionally in first column to force the runner to sort the columns first
		val testScript = """
			C D Q '!Q'
			0 0 0 1
			0 1 0 1
			^1 1 1 0
		""".trimIndent()

		val result = TestcaseCircuitRunner("test", testScript, openedCircuitView.graph as DigitalGraph).run()

		assertEquals(3, result.collector.size)
		for (vector in result.collector) {
			assertEquals(Value.State.PASSED, vector.getValue(2).state)
			assertEquals(Value.State.PASSED, vector.getValue(3).state)
		}
	}
}