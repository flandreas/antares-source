package io.antarescircuit.antares.hdl

import io.antarescircuit.antares.model.testcase.Testcase
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.richtext.RichText

/**
 * Parameters controlling the export of a [HDLModel] given by the user.
 *
 * @property baseName the HDL component name, also the base name of the exported files
 * @property useDelayModel `true` if the generated HDL code should contain delays (e.g. "after 20 ns") when calculating signals
 * @property testBenchParams parameters controlling the creation of a test bench, if required
 */
data class HDLExportParams(
	val renaming: HDLRenaming,
	val baseName: String,
	val useDelayModel: Boolean,
	val testBenchParams: HDLExportTestBenchParams?
)

/**
 * Parameters controlling the creation of a test bench for a [HDLModel].
 *
 * @property renaming the [HDLRenaming] that was used for determining the [testBenchName]
 * @property testBenchName the name of the test bench component to be created, also the base name of its file
 * @property testCase the [Testcase] used to create the test bench
 * @property waitTime the time (in ns) between test vectors generated in the test bench
 */
data class HDLExportTestBenchParams(
	val renaming: HDLRenaming,
	val testBenchName: String,
	val testCase: Testcase,
	val waitTime: Int
)

/** The code created by an HDL export: the circuit and, if requested, the test bench. */
data class HDLExportResult(val hdl: String, val testBench: String?)

/** Suffix used for naming the generated test bench. */
const val HDL_TEST_BENCH_SUFFIX = "_tb"

/** Returns the test bench name for [baseName] and [testcase], e.g. "adder_full_tb". */
fun createHDLTestBenchName(baseName: String, testcase: Testcase, renaming: HDLRenaming): String {
	val testName = StringUtils.simplify(RichText.stripToPlainText(testcase.name.value))
	return if (StringUtils.isNotBlank(testName)) {
		"${baseName}_${renaming.checkName(testName)}$HDL_TEST_BENCH_SUFFIX"
	} else {
		"$baseName$HDL_TEST_BENCH_SUFFIX"
	}
}
