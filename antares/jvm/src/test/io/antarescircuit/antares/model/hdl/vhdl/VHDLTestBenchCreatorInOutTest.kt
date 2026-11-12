package io.antarescircuit.antares.model.hdl.vhdl

import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.hdl.HDLExportTestBenchParams
import io.antarescircuit.antares.hdl.HDLModel
import io.antarescircuit.antares.hdl.vhdl.VHDLRenaming
import io.antarescircuit.antares.hdl.vhdl.VHDLTestBenchCreator
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.testcase.Testcase
import io.antarescircuit.antares.view.gate.TriStateBufferGateView
import io.antarescircuit.jabbah.base.io.StringCodePrinter
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class VHDLTestBenchCreatorInOutTest : AbstractVHDLTest() {

	// The loop variable is "i0", since "i" clashes with the port "I" in case-insensitive VHDL
	@Test
	fun shouldCheckInOutPinMarkedAsOutput() {
		val result = testBench(buildTriStateToInOut(), "I EN <IO\n1 1 1\n0 0 Z")

		assertContains(result, "signal IO: std_logic;\n")
		assertContains(result, "signal IO_drv: std_logic := 'Z';\n")
		assertContains(result, "IO <= IO_drv;\n")
		assertContains(result, "IO => IO")
		assertContains(result, "IO: std_logic;\n")
		assertContains(result, "assert std_match(IO, test_data(i0).IO)")
		assertFalse(result.contains("IO_drv <= test_data"))
	}

	@Test
	fun shouldDriveInOutPinMarkedAsInput() {
		val result = testBench(buildTriStateToInOut(), "I EN >IO\n0 0 1")

		assertContains(result, "IO_drv <= test_data(i0).IO;\n")
		assertFalse(result.contains("std_match(IO,"))
	}

	private fun buildTriStateToInOut(): DigitalGraph {
		val builder = TestCircuitBuilder("test")
		val gate = builder.addVerticeView(TriStateBufferGateView())
		builder.connect(builder.addInput("I"), gate, gate.model.getInputPort())
		builder.connect(builder.addInput("EN"), gate, gate.model.getEnablePort())
		builder.connect(gate, gate.model.getOutputPort(), builder.addInOut("IO"))
		return builder.graph as DigitalGraph
	}

	private fun testBench(graph: DigitalGraph, script: String): String {
		val model = HDLModel(graph, VHDLRenaming())
			.create()
			.apply { renameLabels() }
		val params = HDLExportTestBenchParams(VHDLRenaming(), "test_tb", Testcase("test", script), Paths.get("notUsed"), 30)
		val out = StringCodePrinter()
		VHDLTestBenchCreator(out, model, "test", params).print()
		return out.toString()
	}
}
