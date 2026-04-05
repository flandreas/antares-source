package io.antarescircuit.antares.model.hdl.vhdl

import io.antarescircuit.antares.AbstractJvmCircuitTest
import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.hdl.HDLExportParams
import io.antarescircuit.antares.hdl.vhdl.VHDLRenaming
import io.antarescircuit.jabbah.base.io.StringCodePrinter
import io.antarescircuit.jabbah.graph.library.LibraryModule
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