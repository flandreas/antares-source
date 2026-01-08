package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.antares.view.gate.TriStateBufferGateView
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.Test
import kotlin.test.assertEquals

class TestcaseCircuitRunnerTest {

	private lateinit var circuit: GraphView

	init {
		AntaresTestRule.configure()
	}

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

	@Test
	fun shouldFailAndGateCircuitTest() {
		buildAndGateCircuit()
		val testScript = """
			A B O
			0 0 0
			0 1 0
			1 0 1
			1 1 1
		""".trimIndent()

		val result = TestcaseCircuitRunner("test", testScript, circuit.graph as DigitalGraph).run()

		assertEquals(3, result.names.size)
		assertEquals(4, result.collector.size)

		assertEquals(Value.State.FAILED, result.collector.get(2).getValue(2).state)
	}

	@Test
	fun shouldUseInOutAnnotationInAndGate() {
		buildAndGateCircuit()
		val testScript = """
			>A >B <O
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

	@Test
	fun shouldAcceptDontCareOutput() {
		buildAndGateCircuit()
		val testScript = """
			A B O
			0 0 X
		""".trimIndent()

		val result = TestcaseCircuitRunner("test", testScript, circuit.graph as DigitalGraph).run()

		assertEquals(3, result.names.size)
		assertEquals(1, result.collector.size)

		for (vector in result.collector) {
			assertEquals(Value.State.PASSED, vector.getValue(2).state)
		}
	}

	@Test
	fun shouldPassTriStateBufferCircuitTest() {
		buildTriStateBufferCircuit()
		val testScript = """
			I EN O
			0 0 Z
			1 0 Z
			0 1 0
			1 1 1
		""".trimIndent()

		val result = TestcaseCircuitRunner("test", testScript, circuit.graph as DigitalGraph).run()
		for (vector in result.collector) {
			assertEquals(Value.State.PASSED, vector.getValue(2).state)
		}
	}

	@Test
	fun shouldFailTriStateBufferCircuitTest() {
		buildTriStateBufferCircuit()
		val testScript = """
			I EN O
			0 0 Z
			1 0 1
		""".trimIndent()

		val result = TestcaseCircuitRunner("test", testScript, circuit.graph as DigitalGraph).run()
		assertEquals(Value.State.PASSED, result.collector.get(0).getValue(2).state)
		assertEquals(Value.State.FAILED, result.collector.get(1).getValue(2).state)
		assertEquals(Value.Z, result.collector.get(1).getValue(2))
		assertEquals(DigitalSignalFactory.of(Bit.True), (result.collector.get(1).getValue(2) as MatchedValue).expected.value)
	}

	@Test
	fun shouldPassMultiBitTest() {
		buildMultiBitNOPCircuit()
		val testScript = """
			I           O
			0x1         1			
			15          0xF
			10          0xA
			255         0b11111111
		""".trimIndent()

		val result = TestcaseCircuitRunner("test", testScript, circuit.graph as DigitalGraph).run()

		for (vector in result.collector) {
			assertEquals(Value.State.PASSED, vector.getValue(1).state)
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
		circuit = builder.build()
	}

	private fun buildTriStateBufferCircuit() {
		val builder = TestCircuitBuilder("test")
		val i = builder.addInput("I")
		val o = builder.addOutput("O")
		val en = builder.addInput("EN")
		val gate = builder.addVerticeView(TriStateBufferGateView())
		builder.connect(i, gate, gate.model.getInput(1))
		builder.connect(en, gate, gate.model.getInput(2))
		builder.connect(gate, gate.model.getOutput(3), o)
		circuit = builder.build()
	}

	private fun buildMultiBitNOPCircuit() {
		val builder = TestCircuitBuilder("test")
		val i = builder.add(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "I", bitWidth = BitWidth.BW_8, portType = PortType.INPUT)))
		val o = builder.add(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "O", bitWidth = BitWidth.BW_8, portType = PortType.OUTPUT)))
		builder.connect(i, o)
		circuit = builder.build()
	}
}