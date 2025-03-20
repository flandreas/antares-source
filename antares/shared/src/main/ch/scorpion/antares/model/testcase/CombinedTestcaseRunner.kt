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
import ch.scorpion.jabbah.base.module.BaseModule
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
		var propagationDelayDiscrepancy: Pair<Long, Long>? = null

		if (StringUtils.isBlank(testcase.testVectors.script)) {
			return CombinedTestRunResult.error(circuit, testcase, Translations.getString("antares.testcase.error.empty"))
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

			if (BaseModule.properties.getBoolean(PROP_CHECK_PROP_DELAY_CONSISTENCY) && circuitResults != null && scriptResults != null) {
				val propDelayDiff = abs(circuitResults.duration - subGraphPropagationDelay)
				if (propDelayDiff > PROP_DELAY_DEVIATION * circuitResults.duration) {
					// Test fail
					propagationDelayDiscrepancy = Pair(circuitResults.duration, subGraphPropagationDelay)
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

		return CombinedTestRunResult(circuit, testcase, circuitResults, scriptResults, propagationDelayDiscrepancy = propagationDelayDiscrepancy)
	}
}