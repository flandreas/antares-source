package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.AbstractJvmCircuitTest
import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.hdl.HDLExportParams
import ch.scorpion.antares.hdl.vhdl.VHDLRenaming
import ch.scorpion.jabbah.base.io.StringCodePrinter
import ch.scorpion.jabbah.graph.library.LibraryModule
import java.nio.file.Paths
import kotlin.test.BeforeTest

abstract class AbstractVHDLTest {

	companion object {

		fun testParams(): HDLExportParams =
			HDLExportParams(VHDLRenaming(), "test", true, Paths.get("/tmp/none"), null)
	}

	protected val library get() = LibraryModule.libraryHolder.library
	protected val printer = StringCodePrinter()

	@BeforeTest
	fun setup() {
		AntaresTestRule.configure()
		AbstractJvmCircuitTest.setupLibrary()
	}
}