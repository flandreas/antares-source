package io.antarescircuit.antares.model.hdl.verilog

import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.hdl.HDLExportTestBenchParams
import io.antarescircuit.antares.hdl.HDLModel
import io.antarescircuit.antares.hdl.verilog.VerilogRenaming
import io.antarescircuit.antares.hdl.verilog.VerilogTestBenchCreator
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.testcase.Testcase
import io.antarescircuit.antares.view.gate.TriStateBufferGateView
import io.antarescircuit.jabbah.base.io.StringCodePrinter
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class VerilogTestBenchCreatorTest : AbstractVerilogTest() {

	@Test
	fun shouldApplyInputsAndCheckOutputsPerTestVector() {
		val builder = TestCircuitBuilder("test")
		builder.connect(builder.addInput("I", BitWidth.BW_8), builder.addOutput("O", BitWidth.BW_8))

		assertEquals("""
			// Test bench for test
			module test_tb;
			  reg [7:0] I;
			  wire [7:0] O;
			  integer errors;

			  test main_0 (
			    .I(I),
			    .O(O)
			  );

			  initial begin
			    errors = 0;
			    I = 8'b00000000;
			    #30;
			    if (O !== 8'b00000000) begin
			      ${'$'}display("assertion failed for O on line 0, expected %b, actual is %b", 8'b00000000, O);
			      errors = errors + 1;
			    end
			    I = 8'b11111111;
			    #30;
			    if (O !== 8'b11111111) begin
			      ${'$'}display("assertion failed for O on line 1, expected %b, actual is %b", 8'b11111111, O);
			      errors = errors + 1;
			    end
			    if (errors == 0)
			      ${'$'}display("All tests passed.");
			    else
			      ${'$'}display("%0d assertion(s) failed.", errors);
			    ${'$'}finish;
			  end
			endmodule

		""".trimIndent(), testBench(builder.graph as DigitalGraph, "I O\n0 0\n255 255"))
	}

	@Test
	fun shouldPrecedeClockedValueWithBothClockLevels() {
		val builder = TestCircuitBuilder("test")
		builder.connect(builder.addInput("I"), builder.addOutput("O"))

		val result = testBench(builder.graph as DigitalGraph, "I O\n^1 1")

		assertContains(result, """
			|    I = 1'b0;
			|    #30;
			|    I = 1'b1;
			|    #30;
			|    I = 1'b1;
			|    #30;
			|    if (O !== 1'b1) begin
		""".trimMargin())
		assertEquals(1, Regex("if \\(O").findAll(result).count())
	}

	@Test
	fun shouldCheckHighImpedanceAndSkipDontCare() {
		val builder = TestCircuitBuilder("test")
		builder.connect(builder.addInput("I"), builder.addOutput("O"))

		val result = testBench(builder.graph as DigitalGraph, "I O\nZ Z\n1 X")

		assertContains(result, "    I = 1'bz;\n    #30;\n    if (O !== 1'bz) begin")
		assertContains(result, "    I = 1'b1;\n    #30;\n    if (errors == 0)")
	}

	@Test
	fun shouldKeepPartiallyHighImpedanceValues() {
		val builder = TestCircuitBuilder("test")
		builder.connect(builder.addInput("I", BitWidth.BW_4), builder.addOutput("O", BitWidth.BW_4))

		val result = testBench(builder.graph as DigitalGraph, "I O\n0b1Z01 0b1Z01")

		assertContains(result, "I = 4'b1Z01;")
		assertContains(result, "if (O !== 4'b1Z01) begin")
	}

	@Test
	fun shouldCheckInOutPinMarkedAsOutput() {
		val result = testBench(buildTriStateToInOut(), "I EN <IO\n1 1 1\n0 0 Z")

		assertContains(result, "  wire IO;\n  reg IO_drv;\n  integer errors;\n  assign IO = IO_drv;\n")
		assertContains(result, "    .IO(IO)\n")
		assertContains(result, "    IO_drv = 1'bz;\n    I = 1'b1;\n    EN = 1'b1;\n    #30;\n    if (IO !== 1'b1) begin")
		assertContains(result, "if (IO !== 1'bz) begin")
		assertFalse(result.contains(Regex("IO_drv = 1'b[01]")))
	}

	@Test
	fun shouldDriveInOutPinMarkedAsInput() {
		val result = testBench(buildTriStateToInOut(), "I EN >IO\n0 0 1")

		assertContains(result, "    EN = 1'b0;\n    IO_drv = 1'b1;\n    #30;\n")
		assertFalse(result.contains("if (IO"))
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
		val model = HDLModel(graph, VerilogRenaming())
			.create()
			.apply { renameLabels() }
		val params = HDLExportTestBenchParams(VerilogRenaming(), "test_tb", Testcase("test", script), Paths.get("notUsed"), 30)
		val out = StringCodePrinter()
		VerilogTestBenchCreator(out, model, "test", params).print()
		return out.toString()
	}
}
