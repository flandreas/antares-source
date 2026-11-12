package io.antarescircuit.antares.model.hdl.verilog

import io.antarescircuit.antares.AbstractJvmCircuitTest
import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.hdl.HDLExportParams
import io.antarescircuit.antares.hdl.verilog.VerilogGenerator
import io.antarescircuit.antares.hdl.verilog.VerilogRenaming
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.jabbah.base.io.StringCodePrinter
import io.antarescircuit.jabbah.graph.library.LibraryModule
import java.nio.file.Paths
import kotlin.test.BeforeTest

abstract class AbstractVerilogTest {

	companion object {

		const val TIMESCALE = "`timescale 1ns / 1ps"

		fun testParams(useDelayModel: Boolean = true): HDLExportParams =
			HDLExportParams(VerilogRenaming(), "test", useDelayModel, Paths.get("/tmp/none"), null)
	}

	protected val library get() = LibraryModule.libraryHolder.library
	protected val printer = StringCodePrinter()

	@BeforeTest
	fun setup() {
		AntaresTestRule.configure()
		AbstractJvmCircuitTest.setupLibrary()
	}

	/** Generates Verilog for [graph] and returns it without the leading timescale directive. */
	protected fun generate(graph: DigitalGraph, useDelayModel: Boolean = true): String {
		VerilogGenerator(testParams(useDelayModel)).generateHDL(printer, graph)
		return printer.toString().removePrefix("$TIMESCALE\n\n")
	}
}
