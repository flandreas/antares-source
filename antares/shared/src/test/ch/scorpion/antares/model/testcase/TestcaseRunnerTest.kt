package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TestcaseRunnerTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuit: GraphView

	@BeforeTest
	fun buildCircuit() {
		val builder = TestCircuitBuilder("test")
		val a = builder.addInput("A")
		val b = builder.addInput("B")
		val out = builder.addOutput("O")
		val andGate = builder.addVerticeView(LogicGateView.andGateView())
		builder.connect(a, andGate, andGate.model.getInput(1))
		builder.connect(b, andGate, andGate.model.getInput(2))
		builder.connect(andGate, out)
		circuit = builder.build()
	}

	@Test
	fun shouldPassCircuitTest() {
		val testScript = """
			A B O
			0 0 0
			0 1 0
			1 0 0
			1 1 1
		""".trimIndent()

		val result = TestcaseRunner(testScript, circuit.graph as DigitalGraph).run()

		assertEquals(3, result.names.size)
		assertEquals(4, result.collector.size)

		for (vector in result.collector) {
			// Check state only for output columns
			assertEquals(Value.State.PASSED, vector.getValue(2).state)
		}
	}

	@Test
	fun shouldFailCircuitTest() {
		val testScript = """
			A B O
			0 0 0
			0 1 0
			1 0 1
			1 1 1
		""".trimIndent()

		val result = TestcaseRunner(testScript, circuit.graph as DigitalGraph).run()

		assertEquals(3, result.names.size)
		assertEquals(4, result.collector.size)

		assertEquals(Value.State.FAILED, result.collector.get(2).getValue(2).state)
	}
}