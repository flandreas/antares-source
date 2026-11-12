package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.signal.Bit.*
import io.antarescircuit.antares.model.testcase.parser.TestScript
import io.antarescircuit.antares.model.testcase.parser.TestcaseAnalyser
import io.antarescircuit.antares.model.testcase.parser.TestcaseParser
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.antares.model.truthtable.TruthTableService
import io.antarescircuit.antares.view.synthesis.CircuitSynthesisType
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import kotlin.test.Test
import kotlin.test.assertEquals

class TestcaseScriptGeneratorTest {

	init {
		AntaresTestRule.configure()
	}

	/** Full adder with a negated carry output that is "don't care" for the last row.*/
	private fun createTruthTable(): TruthTable {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B", "Cin"), outputColumnNames = listOf("S", "!Co"))
		truthTable.setColumnValues(3, False, True, True, False, True, False, False, True)
		truthTable.setColumnValues(4, True, True, True, False, True, False, False, Error)
		return truthTable
	}

	@Test
	fun shouldWriteHeaderAndOneTestVectorPerRow() {
		val script = TestcaseScriptGenerator.generate(createTruthTable())

		assertEquals(
			"""
				A B Cin S '!Co'
				0 0 0   0 1
				0 0 1   1 1
				0 1 0   1 1
				0 1 1   0 0
				1 0 0   1 1
				1 0 1   0 0
				1 1 0   0 0
				1 1 1   1 X

			""".trimIndent(),
			script
		)
	}

	@Test
	fun shouldPassForCircuitsSynthesizedFromSameTruthTable() {
		val truthTable = createTruthTable()
		for (type in CircuitSynthesisType.entries) {
			val result = run(synthesize(truthTable, type), TestcaseScriptGenerator.generate(truthTable))

			assertEquals(8, result.collector.size, "$type")
			assertEquals(List(16) { Value.State.PASSED }, outputStates(result), "$type")
		}
	}

	@Test
	fun shouldFailIfOneExpectedOutputIsFlipped() {
		val truthTable = createTruthTable()
		val circuit = synthesize(truthTable, CircuitSynthesisType.AndOrGate)
		truthTable.setValue(5, 3, True)

		val result = run(circuit, TestcaseScriptGenerator.generate(truthTable))

		assertEquals(List(16) { if (it == 10) Value.State.FAILED else Value.State.PASSED }, outputStates(result))
	}

	private fun synthesize(truthTable: TruthTable, type: CircuitSynthesisType): DigitalGraph {
		val metaGraph = MetaGraph.create(TranslatableText("Test"), AntaresGraphTypes.Digital)
		type.build(truthTable, false, metaGraph.graph)
		return metaGraph.graph.model as DigitalGraph
	}

	/** Parses and analyses [script] against [circuit] before running it.*/
	private fun run(circuit: DigitalGraph, script: String): TestRunResult {
		val testScript = TestcaseParser(script, TestcaseAnalyser(circuit)).parse() as TestScript
		return TestcaseCircuitRunner("test", testScript, circuit).run()
	}

	private fun outputStates(result: TestRunResult): List<Value.State> =
		result.collector.testVectors.flatMap { listOf(it.getValue(3).state, it.getValue(4).state) }
}
