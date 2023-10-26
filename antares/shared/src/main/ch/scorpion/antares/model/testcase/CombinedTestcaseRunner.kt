package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.testcase.parser.TestScript
import ch.scorpion.antares.model.testcase.parser.TestcaseAnalyser
import ch.scorpion.antares.model.testcase.parser.TestcaseParser
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.SemanticError
import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.logger

/**
 * A combined runner of [Testcase]s that runs a test script on a [DigitalGraph] and/or
 * its execution script, depending on whether the execution scripts exists and the
 * [DigitalGraph] is marked as [DigitalGraph.purelyScripted].
 */
class CombinedTestcaseRunner(
	private val testcase: Testcase,
	private val circuit: DigitalGraph,
	private val execScriptAST: Node? = null,
	private val inputLogicProvider: (String) -> Logic = { Logic.POSITIVE }
) {
	companion object {
		private val LOG by logger(CombinedTestcaseRunner::class)
	}

	private var testcaseCircuitRunner: TestcaseCircuitRunner? = null
	private var testcaseScriptRunner: TestcaseScriptRunner? = null

	fun run(): CombinedTestRunResult {
		var circuitResults: TestRunResult? = null
		var scriptResults: TestRunResult? = null

		if (StringUtils.isBlank(testcase.testVectors.script)) {
			return CombinedTestRunResult.error(testcase, Translations.getString("antares.testcase.error.empty"))
		}

		try {
			val testScript =
				TestcaseParser(testcase.testVectors.script!!, TestcaseAnalyser(circuit)).parse() as TestScript

			if (!circuit.purelyScripted) {
				testcaseCircuitRunner = TestcaseCircuitRunner(testcase.name.value, testScript, circuit)
				circuitResults = testcaseCircuitRunner!!.run()
			}
			if (execScriptAST != null) {
				testcaseScriptRunner = TestcaseScriptRunner(testcase.name.value, testScript, circuit, execScriptAST, inputLogicProvider)
				scriptResults = testcaseScriptRunner!!.run()
			}
		} catch (e: SemanticError) {
			return CombinedTestRunResult.error(testcase, e.message ?: "Error")
		} catch (e: SyntaxError) {
			return CombinedTestRunResult.error(testcase, e.message ?: "Error")
		} catch (e: Throwable) {
			LOG.error("Error while running test '${testcase.name.value}' for circuit '${circuit.name.value}'", e)
			return CombinedTestRunResult.error(testcase, e.message ?: "Error")
		} finally {
			testcaseCircuitRunner?.dispose()
			testcaseScriptRunner?.dispose()
		}

		return CombinedTestRunResult(testcase, circuitResults, scriptResults)
	}
}