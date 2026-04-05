package io.antarescircuit.antares.model.testcase.parser

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.testcase.TestcaseScriptRunner
import io.antarescircuit.antares.model.testcase.Value
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.jabbah.base.module.BaseModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestcaseScriptRunnerTest {

	private lateinit var circuit: DigitalGraph

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldPassAndGateCircuitTest() {
		buildAndGateCircuit()
		circuit.script = "O = A and B"

		val testScript = """
			A B O
			0 0 0
			0 1 0
			1 0 0
			1 1 1
		""".trimIndent()

		val execScriptAST = BaseModule.parserFactory(circuit.script!!, null).parse()
		val result = TestcaseScriptRunner("test", testScript, circuit, execScriptAST).run()

		assertNull(result.errorMessage)
		assertEquals(3, result.names.size)
		assertEquals(4, result.collector.size)

		for (vector in result.collector) {
			// Check state only for output columns
			assertEquals(Value.State.PASSED, vector.getValue(2).state)
		}
	}

	private fun buildAndGateCircuit() {
		val builder = TestCircuitBuilder("test")
		val a = builder.addInput("A")
		val b = builder.addInput("B")
		val out = builder.addOutput("O")
		val andGate = builder.addVerticeView(LogicGateView.andGateView())
		builder.connect(a, andGate, andGate.model.getInput(1))
		builder.connect(b, andGate, andGate.model.getInput(2))
		builder.connect(andGate, out)
		circuit = (builder.build().graph) as DigitalGraph
	}
}