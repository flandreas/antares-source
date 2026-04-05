package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus

/**
 * A request posted on [EventBus] to display newly produced test results.
 * Processed by UI components that display test results.
 */
data class DisplayTestRunResults(val results: List<CombinedTestRunResult>)

data class GeneralTestResult(
	val error: Boolean,
	val description: String
)

/**
 * The result provided by a [TestcaseCircuitRunner].
 *
 * @property testName the name of the [Testcase]
 * @property names the name of the input and output ports from the plaintext test script
 * @property isOutput `true` indicates that the corresponding column refers to an output
 * @property collector contains the collected [TestVector]s whose output column values
 * have been replaced with [MatchedValue] containing the test result.
 * @property errorMessage the error message if parsing or analysing the [Testcase] failed, `null` otherwise
 * @property duration the simulation time (in ns) it took to execute [source]
 */
data class TestRunResult(
	val source: DigitalGraph,
	val type: Type,
	val testName: String,
	val names: List<String>,
	val isOutput: List<Boolean>,
	val collector: TestVectorCollector,
	val errorMessage: String? = null,
	val duration: Long = 0L
) {
	companion object {
		fun error(source: DigitalGraph, type: Type, testName: String, msg: String): TestRunResult =
			TestRunResult(source, type, testName, emptyList(), emptyList(), TestVectorCollector(), msg)
	}

	enum class Type {
		Circuit,
		Script;

		override fun toString(): String {
			return when(this) {
				Circuit -> Translations.getString("antares.testcase.result.type.circuit.name")
				Script -> Translations.getString("antares.testcase.result.type.script.name")
			}
		}

		fun tooltip(): String {
			return when (this) {
				Circuit -> Translations.getString("antares.testcase.result.type.circuit.desc")
				Script -> Translations.getString("antares.testcase.result.type.script.desc")
			}
		}
	}

	/** Returns the number of failed [TestVector]s.*/
	val failedCount: Int

	init {
		val failedVectors = mutableSetOf<TestVector>()
		for (column in names.indices) {
			if (isOutput[column]) {
				collector.testVectors
					.filter { it.isFailed(column) }
					.forEach { failedVectors.add(it) }
			}
		}
		failedCount = failedVectors.size
	}
}

/**
 * The result provided by a [CombinedTestcaseRunner].
 *
 * @property generalTestResults the discrepancy between measured real circuit propagation delay (first pair value)
 * and the configured propagation delay of the subcircuit (second pair value)
 */
data class CombinedTestRunResult(
	val source: DigitalGraph,
	val testcase: Testcase,
	val circuitResults: TestRunResult?,
	val scriptResults: TestRunResult?,
	val error: String? = null,
	val ignored: Boolean = false,
	val generalTestResults: List<GeneralTestResult> = emptyList()
) {

	companion object {

		fun error(source: DigitalGraph, testcase: Testcase, error: String): CombinedTestRunResult =
			CombinedTestRunResult(source, testcase, null, null, error)

		fun ignored(source: DigitalGraph, testcase: Testcase): CombinedTestRunResult =
			CombinedTestRunResult(source, testcase, null, null, null, ignored = true)
	}

	val totalFailedCount: Int = if (error != null && !ignored) {
		1
	} else {
		val circuitFailedCount = circuitResults?.let {
			if (it.errorMessage != null) 1 else it.failedCount
		} ?: 0
		val scriptFailedCount = scriptResults?.let {
			if (it.errorMessage != null) 1 else it.failedCount
		} ?: 0
		val generalFailedCount = generalTestResults.count { it.error }
		circuitFailedCount + scriptFailedCount + generalFailedCount
	}

	val failed: Boolean get() = totalFailedCount > 0
}