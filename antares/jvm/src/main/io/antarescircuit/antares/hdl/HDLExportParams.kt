package io.antarescircuit.antares.hdl

import io.antarescircuit.antares.model.testcase.Testcase
import java.nio.file.Path

/**
 * Parameters controlling the export of a [HDLModel] given by the user.
 *
 * @property baseName the VHDL component name
 * @property useDelayModel `true` if the generated VHDL file should contain "after 20 ns" terms when calculating signals
 * @property hdlFile the [Path] to the VHDL file to be created
 * @property testBenchParams parameters controlling the creation of a test bench, if required
 */
data class HDLExportParams(
	val renaming: HDLRenaming,
	val baseName: String,
	val useDelayModel: Boolean,
	val hdlFile: Path,
	val testBenchParams: HDLExportTestBenchParams?
)

/**
 * Parameters controlling the creation of a test bench for a [HDLModel].
 *
 * @property renaming the [HDLRenaming] that was used for determining the [testBenchName]
 * @property testBenchName the name of the test bench component to be created
 * @property testCase the [Testcase] used to create the test bench
 * @property tbFile the [Path] to the test bench VHDL file to be created, if any
 * @property waitTime the time (in ns) between test vectors generated in the test bench
 */
data class HDLExportTestBenchParams(
	val renaming: HDLRenaming,
	val testBenchName: String,
	val testCase: Testcase,
	val tbFile: Path,
	val waitTime: Int
)