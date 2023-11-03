package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.PortType
import kotlin.test.Test
import kotlin.test.assertEquals

class TestcaseScriptRunnerTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuit: DigitalGraph

	@Test
	fun testRaisedInput() {
		buildDummyFlipFlopCircuit()

		// Script from "D Flip-Flop"
		val execScript = """
			init {
				Q = 0
				'!Q' = 1
			}

			if (^C) {
				Q = D
				'!Q' = not D
			}
		""".trimIndent()

		val testScript = """
			C	D	Q	'!Q'
			^1	1	1	0
		""".trimIndent()

		val execScriptAST = BaseModule.parserFactory(execScript, null).parse()
		val results = TestcaseScriptRunner("test", testScript, circuit, execScriptAST).run()

		assertEquals(Value.State.PASSED, results.collector.get(0).getValue(2).state)
	}

	@Test
	fun testMultiBit() {
		buildMultiBitNOPCircuit()

		val execScript = "O = I"

		val testScript = """
			I           O
			0x1         1			
			15          0xF
			10          0xA
			255         0b11111111
		""".trimIndent()

		val execScriptAST = BaseModule.parserFactory(execScript, null).parse()
		val results = TestcaseScriptRunner("test", testScript, circuit, execScriptAST).run()

		for (vector in results.collector) {
			assertEquals(Value.State.PASSED, vector.getValue(1).state)
		}
	}

	private fun buildDummyFlipFlopCircuit() {
		// Scripted only, no circuitry needed
		val builder = TestCircuitBuilder("test")
		builder.addInput("C")
		builder.addInput("D")
		builder.addOutput("Q")
		builder.addOutput("!Q")
		circuit = builder.build().graph as DigitalGraph
	}

	private fun buildMultiBitNOPCircuit() {
		val builder = TestCircuitBuilder("test")
		builder.add(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "I", bitWidth = BitWidth.BW_8, portType = PortType.INPUT)))
		builder.add(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "O", bitWidth = BitWidth.BW_8, portType = PortType.OUTPUT)))
		circuit = builder.build().graph as DigitalGraph
	}
}