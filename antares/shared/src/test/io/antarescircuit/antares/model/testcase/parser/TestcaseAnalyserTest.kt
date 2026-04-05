package io.antarescircuit.antares.model.testcase.parser

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.jabbah.base.dsl.SemanticError
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class TestcaseAnalyserTest {

	private lateinit var circuit: DigitalGraph
	private lateinit var builder: TestCircuitBuilder

	@BeforeTest
	fun buildCircuit() {
		AntaresTestRule.configure()
		builder = TestCircuitBuilder("test")
		builder.addInput("A")
		builder.addInput("B")
		builder.addOutput("O1")
		builder.addOutput("O2")
		circuit = builder.build().graph as DigitalGraph
	}

	@Test
	fun shouldRejectUnknownPort() {
		expectSemanticError("antares.testcase.error.unknownPort", """
			A Y O1
		""".trimIndent())
	}

	@Test
	fun shouldRejectNoInput() {
		expectSemanticError("antares.testcase.error.noInput", """
			O1 O2
		""".trimIndent())
	}

	@Test
	fun shouldRejectNoOutput() {
		expectSemanticError("antares.testcase.error.noOutput", """
			A B
		""".trimIndent())
	}

	@Test
	fun shouldRejectTooManyValues() {
		expectSemanticError("antares.testcase.error.tooManyValues", """
			A B O1
			0 0 0
			0 1 1 1
		""".trimIndent())
	}

	@Test
	fun shouldRejectTooFewValues() {
		expectSemanticError("antares.testcase.error.tooFewValues", """
			A B O1
			0 0 0
			0 1
		""".trimIndent())
	}

	@Test
	fun shouldRejectDontCareInRunBlock() {
		expectSemanticError("antares.testcase.error.dontCareInRunBlock", """
			A B O1
			run {
				X 0 1
			}
		""".trimIndent())
	}

	private fun expectSemanticError(msgKey: String, script: String) {
		assertFailsWith<SemanticError>(
			message = msgKey,
			block = { TestcaseParser(script, TestcaseAnalyser(circuit)).parse() }
		)
	}
}