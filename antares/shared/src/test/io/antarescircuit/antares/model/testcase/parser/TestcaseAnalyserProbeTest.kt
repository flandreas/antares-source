package io.antarescircuit.antares.model.testcase.parser

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.view.net.ProbeView
import io.antarescircuit.jabbah.base.dsl.SemanticError
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class TestcaseAnalyserProbeTest {

    private lateinit var circuit: DigitalGraph
    private lateinit var builder: TestCircuitBuilder

    @BeforeTest
    fun buildCircuit() {
        AntaresTestRule.configure()
        builder = TestCircuitBuilder("test")
        builder.addInput("A")
        builder.addVerticeView(createProbeView("O1"))
        circuit = builder.build().graph as DigitalGraph
    }

    @Test
    fun shouldAcceptProbeOutput() {
        TestcaseParser("""
            A O1
            0 0
        """.trimIndent(), TestcaseAnalyser(circuit)).parse()
    }

    @Test
    fun shouldRejectAmbiguousOutputNames() {
        builder.addInput("O1")

        expectSemanticError("antares.testcase.error.ambiguousOutputName", """
			A O1
			run {
				0 0 1
			}
		""".trimIndent())
    }

    private fun createProbeView(name: String): ProbeView {
        val probeView = ProbeView()
        probeView.name = "O1"
        return probeView
    }

    private fun expectSemanticError(msgKey: String, script: String) {
        assertFailsWith<SemanticError>(
            message = msgKey,
            block = { TestcaseParser(script, TestcaseAnalyser(circuit)).parse() }
        )
    }
}