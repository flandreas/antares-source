package io.antarescircuit.antares.model

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.view.gate.LogicGateView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ControlledCircuitRunnerTest {

	@BeforeTest
	fun beforeTest() {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldNameOscillatingComponentWhenAborting() {
		// A NOT gate driving its own input never settles
		val builder = TestCircuitBuilder("ring")
		val not = builder.addVerticeView(LogicGateView.notGateView())
		builder.connect(not, not)
		val circuit = builder.graph as DigitalGraph
		val runner = ControlledCircuitRunner(maxIteration = 200)

		val error = assertFailsWith<ControlledCircuitRunner.TooManyIterations> { runner.run(circuit) }

		assertTrue(error.involved.any { it.endsWith("(${not.vertice.id})") }, error.involved.toString())
		assertTrue(error.describe("Aborted").startsWith("Aborted\n"))
		runner.dispose()
	}
}
