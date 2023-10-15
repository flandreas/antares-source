package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.base.dsl.SemanticError
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class TestcaseAnalyserTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuit: DigitalGraph

	@BeforeTest
	fun buildCircuit() {
		val builder = TestCircuitBuilder("test")
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

	private fun expectSemanticError(msgKey: String, script: String) {
		assertFailsWith<SemanticError>(
			message = msgKey,
			block = { TestcaseParser(script, TestcaseAnalyser(circuit)).parse() }
		)
	}
}