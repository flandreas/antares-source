package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.testcase.parser.TestScript
import ch.scorpion.antares.model.testcase.parser.TestcaseAnalyser
import ch.scorpion.antares.model.testcase.parser.TestcaseParser
import ch.scorpion.jabbah.base.StringUtils

/**
 * A combined runner of [Testcase]s that runs a test script on a [DigitalGraph] and/or
 * its execution script, depending on whether the execution scripts exists and the
 * [DigitalGraph] is marked as [DigitalGraph.purelyScripted].
 */
class CombinedTestcaseRunner(
	private val testcase: Testcase,
	private val circuit: DigitalGraph
) {

	private var testcaseCircuitRunner: TestcaseCircuitRunner? = null
	private var testcaseScriptRunner: TestcaseScriptRunner? = null

	fun run(): CombinedTestRunResult {
		var circuitResults: TestRunResult? = null
		var scriptResults: TestRunResult? = null

		try {
			val testScript = TestcaseParser(testcase.testVectors.script!!, TestcaseAnalyser(circuit)).parse() as TestScript

			if (!circuit.purelyScripted) {
				testcaseCircuitRunner = TestcaseCircuitRunner(testcase.name.value, testScript, circuit)
				circuitResults = testcaseCircuitRunner!!.run()
			}
			if (StringUtils.isNotEmpty(circuit.script)) {
				testcaseScriptRunner = TestcaseScriptRunner(testcase.name.value, testScript, circuit)
				scriptResults = testcaseScriptRunner!!.run()
			}
		} catch (e: Throwable) {
			return CombinedTestRunResult.error(testcase, e.message ?: "Error")
		} finally {
			testcaseCircuitRunner?.dispose()
			testcaseScriptRunner?.dispose()
		}

		return CombinedTestRunResult(testcase, circuitResults, scriptResults)
	}
}