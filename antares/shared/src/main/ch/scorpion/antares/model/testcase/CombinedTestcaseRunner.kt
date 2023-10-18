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
	private val testName: String,
	private val text: String,
	private val circuit: DigitalGraph
) {

	private var testcaseCircuitRunner: TestcaseCircuitRunner? = null
	private var testcaseScriptRunner: TestcaseScriptRunner? = null

	fun run(): List<TestRunResult> {
		val results = mutableListOf<TestRunResult>()

		try {
			val testScript = TestcaseParser(text, TestcaseAnalyser(circuit)).parse() as TestScript

			if (!circuit.purelyScripted) {
				testcaseCircuitRunner = TestcaseCircuitRunner(testName, testScript, circuit)
				results.add(testcaseCircuitRunner!!.run())
			}
			if (StringUtils.isNotEmpty(circuit.script)) {
				testcaseScriptRunner = TestcaseScriptRunner(testName, testScript, circuit)
				results.add(testcaseScriptRunner!!.run())
			}
		} catch (e: Throwable) {
			results.add(TestRunResult.error(TestRunResult.Type.Script, testName, e.message ?: "Error"))
		} finally {
			testcaseCircuitRunner?.dispose()
			testcaseScriptRunner?.dispose()
		}

		return results
	}
}