package ch.scorpion.antares.model.testcase

import ch.scorpion.jabbah.base.Translations

/**
 * The result provided by a [TestcaseCircuitRunner].
 *
 * @property testName the name of the [Testcase]
 * @property names the name of the input and output ports from the plaintext test script
 * @property isOutput `true` indicates that the corresponding column refers to an output
 * @property collector contains the collected [TestVector]s whose output column values
 * have been replaced with [MatchedValue] containing the test result.
 * @property errorMessage the error message if parsing or analysing the [Testcase] failed, `null` otherwise
 */
data class TestRunResult(
	val type: Type,
	val testName: String,
	val names: List<String>,
	val isOutput: List<Boolean>,
	val collector: TestVectorCollector,
	val errorMessage: String? = null
) {
	companion object {
		fun error(type: Type, testName: String, msg: String): TestRunResult =
			TestRunResult(type, testName, emptyList(), emptyList(), TestVectorCollector(), msg)
	}

	enum class Type {
		Circuit,
		Script;

		override fun toString(): String {
			return when(this) {
				Circuit -> Translations.getString("antares.testcase.result.type.circuit")
				Script -> Translations.getString("antares.testcase.result.type.script")
			}
		}
	}

	/** Returns the number of failed [TestVector]s.*/
	val failedCount: Int get() {
		val failedVectors = mutableSetOf<TestVector>()
		for (column in names.indices) {
			if (isOutput[column]) {
				collector.testVectors
					.filter { it.isFailed(column) }
					.forEach { failedVectors.add(it) }
			}
		}
		return failedVectors.size
	}
}