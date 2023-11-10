package ch.scorpion.antares.hdl.vhdl

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.testcase.Testcase
import ch.scorpion.jabbah.base.io.CodePrinter
import ch.scorpion.jabbah.graph.library.LibraryModule
import java.io.FileOutputStream
import java.nio.file.Path

object VHDLService {

	// Logging is done in VHDLGenerator

	/**
	 * Exports a [DigitalGraph] as VHDL to a file.
	 * @param circuit the [DigitalGraph] to be exported
	 * @param useDelayModel `true` if the generated VHDL file should contain "after 20 ns" terms when calculating signals
	 * @param vhdlFile the [Path] to the VHDL file to be created
	 * @param testCase the [Testcase] used to create the test bench
	 * @param tbFile the [Path] to the test bench VHDL file to be created, if any
	 * @param waitTime the time (in ns) between test vectors generated in the test bench
	 */
	fun export(
		circuit: DigitalGraph,
		useDelayModel: Boolean,
		vhdlFile: Path,
		testCase: Testcase?,
		tbFile: Path?,
		waitTime: Int?
	) {
		FileOutputStream(vhdlFile.toFile()).use {
			val printer = CodePrinter(it)
			VHDLGenerator(LibraryModule.libraryHolder.library, printer, generateComment = true).generate(circuit)
			printer.close()
		}
	}
}