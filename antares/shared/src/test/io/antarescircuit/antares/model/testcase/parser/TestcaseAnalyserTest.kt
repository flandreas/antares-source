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

	@Test
	fun shouldAcceptLoopVariablesInScope() {
		TestcaseParser("""
			A B O1
			loop a 0..1 {
				loop b 0..1 {
					(a) (b) (a & b)
				}
				(a) 0 0
			}
		""".trimIndent(), TestcaseAnalyser(circuit)).parse()
	}

	@Test
	fun shouldRejectUnknownVariable() {
		expectSemanticError("antares.testcase.error.unknownVariable", """
			A B O1
			(a) 0 0
		""".trimIndent())
	}

	@Test
	fun shouldRejectLoopVariableOutsideOfItsLoop() {
		expectSemanticError("antares.testcase.error.unknownVariable", """
			A B O1
			loop a 0..1 {
				(a) 0 0
			}
			(a) 0 0
		""".trimIndent())
	}

	@Test
	fun shouldAcceptMaximumNumberOfTestVectors() {
		TestcaseParser("""
			A B O1
			loop a 1..1000 {
				loop b 1..1000 {
					(a) (b) 0
				}
			}
		""".trimIndent(), TestcaseAnalyser(circuit)).parse()
	}

	@Test
	fun shouldRejectTooManyTestVectorsFromNestedLoops() {
		expectSemanticError("antares.testcase.error.tooManyTestVectors", """
			A B O1
			loop a 0..1000 {
				loop b 1..1000 {
					(a) (b) 0
				}
			}
		""".trimIndent())
	}

	@Test
	fun shouldRejectTooManyTestVectorsFromDontCareInputsInLoop() {
		expectSemanticError("antares.testcase.error.tooManyTestVectors", """
			A B O1
			loop a 0..250000 {
				X X 0
			}
		""".trimIndent())
	}

	@Test
	fun shouldRejectHugeLoopRange() {
		expectSemanticError("antares.testcase.error.tooManyTestVectors", """
			A B O1
			loop a 0..0xFFFFFFFFFFFFFFFF {
				(a) 0 0
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