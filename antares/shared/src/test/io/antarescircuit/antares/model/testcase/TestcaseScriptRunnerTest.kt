package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.model.PortType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestcaseScriptRunnerTest {

	private lateinit var circuit: DigitalGraph

	init {
		AntaresTestRule.configure()
	}

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

	// Regression test for #1251
	@Test
	fun testSetUndefined() {
		buildMultiBitNOPCircuit()

		val execScript = """
			O = I
		""".trimIndent()

		val testScript = """
				I			O
			run {
				255			255
				0bZZZZZZZZ	0bZZZZZZZZ
			}
		""".trimIndent()

		val execScriptAST = BaseModule.parserFactory(execScript, null).parse()
		val results = TestcaseScriptRunner("test", testScript, circuit, execScriptAST).run()

		for (vector in results.collector) {
			assertEquals(Value.State.PASSED, vector.getValue(1).state)
		}
	}

	@Test
	fun shouldOnlyToggleOnFallingEdges() {
		val result = runToggleScript("""
			C D Q '!Q'
			run {
				0 0 1 0
				0 0 1 0
				1 0 1 0
				1 1 1 0
				0 1 0 1
				0 1 0 1
				1 1 0 1
				0 1 1 0
			}
		""".trimIndent(), Logic.NEGATIVE)

		assertPassed(result, 8)
	}

	@Test
	fun shouldOnlyToggleOnRisingEdges() {
		val result = runToggleScript("""
			C D Q '!Q'
			run {
				0 0 1 0
				1 0 0 1
				1 1 0 1
				1 1 0 1
				0 1 0 1
				1 1 1 0
			}
		""".trimIndent(), Logic.POSITIVE)

		assertPassed(result, 6)
	}

	@Test
	fun shouldReportIncorrectInitializationInsteadOfInventingFallingEdge() {
		val result = runToggleScript("""
			C Q '!Q'
			run {
				0 0 1
				1 0 1
				0 1 0
				1 1 0
				0 0 1
			}
		""".trimIndent(), Logic.NEGATIVE)

		assertNull(result.errorMessage)
		assertEquals(5, result.collector.size)
		assertEquals(5, result.failedCount)
		assertEquals(1UL, result.collector.get(0).getValue(1).value.toLong())
	}

	@Test
	fun shouldResetInputHistoryBetweenRuns() {
		val result = runToggleScript("""
			C D Q '!Q'
			run {
				0 0 1 0
				1 0 1 0
			}
			run {
				0 0 1 0
				1 0 1 0
				0 0 0 1
			}
		""".trimIndent(), Logic.NEGATIVE)

		assertPassed(result, 5)
	}

	@Test
	fun shouldEvaluateCombinationalScriptAtStartupWithoutInputChanges() {
		buildMultiBitNOPCircuit()
		val ast = BaseModule.parserFactory("O = not I", null).parse()
		val result = TestcaseScriptRunner("test", "I O\n0 255", circuit, ast).run()

		assertPassed(result, 1)
	}

	@Test
	fun shouldApplyInitialInputLevelsBeforeEvaluatingStartup() {
		val result = runPresetClearScript("""
			C D '!(PRE)' '!(CLR)' Q '!Q'
			run {
				0 0 1 1 0 1
				0 0 1 1 0 1
				0 1 1 1 0 1
				^1 1 1 1 1 0
				1 0 1 1 1 0
				0 0 1 1 1 0
				^1 0 1 1 0 1
				0 1 1 0 0 1
				0 0 0 1 1 0
				0 0 0 0 1 1
			}
		""".trimIndent())

		assertPassed(result, 10)
	}

	@Test
	fun shouldApplyInitialInputLevelsForEachIndependentVector() {
		val result = runPresetClearScript("""
			C D '!(PRE)' '!(CLR)' Q '!Q'
			0 0 1 1 0 1
			0 1 1 1 0 1
			1 1 1 1 1 0
			^1 1 1 1 1 0
			0 0 0 1 1 0
			0 0 1 1 0 1
		""".trimIndent())

		assertPassed(result, 6)
	}

	private fun runPresetClearScript(testScript: String): TestRunResult {
		val builder = TestCircuitBuilder("test")
		builder.addInput("C")
		builder.addInput("D")
		builder.addInput("!(PRE)")
		builder.addInput("!(CLR)")
		builder.addOutput("Q")
		builder.addOutput("!Q")
		circuit = builder.build().graph as DigitalGraph
		val script = """
			init {
				Q = 0
				'!Q' = 1
			}
			if (not '!(PRE)' and not '!(CLR)') {
				Q = 1
				'!Q' = 1
				return
			}
			if (not '!(PRE)') {
				Q = 1
				'!Q' = 0
				return
			}
			if (not '!(CLR)') {
				Q = 0
				'!Q' = 1
				return
			}
			if (^C) {
				Q = D
				'!Q' = not D
			}
		""".trimIndent()
		val ast = BaseModule.parserFactory(script, null).parse()
		return TestcaseScriptRunner("test", testScript, circuit, ast).run()
	}

	private fun runToggleScript(testScript: String, logic: Logic): TestRunResult {
		buildDummyFlipFlopCircuit()
		val script = """
			init {
				store qStore = 1
				Q = qStore
				'!Q' = not qStore
			}
			if (^C) {
				qStore = not qStore
				Q = qStore
				'!Q' = not qStore
			}
		""".trimIndent()
		val ast = BaseModule.parserFactory(script, null).parse()
		return TestcaseScriptRunner("test", testScript, circuit, ast) { logic }.run()
	}

	private fun assertPassed(result: TestRunResult, expectedVectors: Int) {
		assertNull(result.errorMessage)
		assertEquals(expectedVectors, result.collector.size)
		for (vector in result.collector) {
			result.isOutput.forEachIndexed { index, isOutput ->
				if (isOutput) {
					assertEquals(Value.State.PASSED, vector.getValue(index).state, "${vector.description}, column $index")
				}
			}
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
