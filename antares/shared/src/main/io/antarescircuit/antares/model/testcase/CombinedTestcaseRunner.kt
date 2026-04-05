package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.testcase.parser.TestScript
import io.antarescircuit.antares.model.testcase.parser.TestcaseAnalyser
import io.antarescircuit.antares.model.testcase.parser.TestcaseParser
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.Node
import io.antarescircuit.jabbah.base.dsl.SemanticError
import io.antarescircuit.jabbah.base.dsl.SyntaxError
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import kotlin.math.abs

/**
 * A combined runner of [Testcase]s that runs a test script on a [DigitalGraph] and/or
 * its execution script, depending on whether the execution scripts exists and the
 * [DigitalGraph] is marked as [DigitalGraph.purelyScripted].
 */
class CombinedTestcaseRunner(
	private val testcase: Testcase,
	private val circuit: DigitalGraph,
	private val execScriptAST: Node? = null,
	private val subGraphPropagationDelay: Long = 0,
	private val inputLogicProvider: (String) -> Logic = { Logic.POSITIVE }
) {
	companion object {
		private val LOG by logger(CombinedTestcaseRunner::class)

		/** The maximum accepted deviation factor when comparing real and scripted propagation delay.*/
		private const val PROP_DELAY_DEVIATION = 0.1

		const val PROP_CHECK_PROP_DELAY_CONSISTENCY = "antares.testcase.checkPropDelayConsistency"
	}

	private var testcaseCircuitRunner: TestcaseCircuitRunner? = null
	private var testcaseScriptRunner: TestcaseScriptRunner? = null

	fun run(): CombinedTestRunResult {
		var circuitResults: TestRunResult? = null
		var scriptResults: TestRunResult? = null
		val generalTestResults = mutableListOf<GeneralTestResult>()

		if (StringUtils.isBlank(testcase.testVectors.script)) {
			return CombinedTestRunResult.error(circuit, testcase, Translations.getString("antares.testcase.error.empty"))
		}

		try {
			val testScript =
				TestcaseParser(testcase.testVectors.script!!, TestcaseAnalyser(circuit)).parse() as TestScript

			if (!circuit.purelyScripted) {
				testcaseCircuitRunner = TestcaseCircuitRunner(testcase.name.value, testScript, circuit, testcase.numberOfIterations)
				circuitResults = testcaseCircuitRunner!!.run()
			}
			if (execScriptAST != null) {
				testcaseScriptRunner = TestcaseScriptRunner(testcase.name.value, testScript, circuit, execScriptAST, inputLogicProvider)
				scriptResults = testcaseScriptRunner!!.run()
			}

			if (BaseModule.properties.getBoolean(PROP_CHECK_PROP_DELAY_CONSISTENCY)
				&& !testcase.skipPropDelayConsistenceCheck
				&& circuitResults != null
				&& scriptResults != null
			) {
				val propDelayDiff = abs(circuitResults.duration - subGraphPropagationDelay)
				if (propDelayDiff > PROP_DELAY_DEVIATION * circuitResults.duration) {
					// Test fail
					generalTestResults.add(
						GeneralTestResult(
							true,
							Translations.getString("antares.testcase.propDelayConsistency.failed", circuitResults.duration, subGraphPropagationDelay))
					)
				} else {
					generalTestResults.add(
						GeneralTestResult(false, Translations.getString("antares.testcase.propDelayConsistency.passed"))
					)
				}
			}

		} catch (e: SemanticError) {
			return CombinedTestRunResult.error(circuit, testcase, e.message ?: "Error")
		} catch (e: SyntaxError) {
			return CombinedTestRunResult.error(circuit, testcase, e.message ?: "Error")
		} catch (e: Throwable) {
			LOG.error("Error while running test '${testcase.name.value}' for circuit '${circuit.name.value}'", e)
			return CombinedTestRunResult.error(circuit, testcase, e.message ?: "Error")
		} finally {
			testcaseCircuitRunner?.dispose()
			testcaseScriptRunner?.dispose()
		}

		return CombinedTestRunResult(circuit, testcase, circuitResults, scriptResults, generalTestResults = generalTestResults)
	}
}