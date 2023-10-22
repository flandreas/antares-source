package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.Test
import kotlin.test.assertEquals

class TestcaseScriptRunnerRest {

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

	private fun buildDummyFlipFlopCircuit() {
		// Scripted only, no circuitry needed
		val builder = TestCircuitBuilder("test")
		builder.addInput("C")
		builder.addInput("D")
		builder.addOutput("Q")
		builder.addOutput("!Q")
		circuit = builder.build().graph as DigitalGraph
	}
}