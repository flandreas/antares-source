package io.antarescircuit.antares.model.hdl.verilog

import io.antarescircuit.antares.hdl.HDLExportTestBenchParams
import io.antarescircuit.antares.hdl.HDLModel
import io.antarescircuit.antares.hdl.verilog.VerilogGenerator
import io.antarescircuit.antares.hdl.verilog.VerilogRenaming
import io.antarescircuit.antares.hdl.verilog.VerilogTestBenchCreator
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.standardlibrary.AbstractStandardLibraryBasedCircuitTest
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.io.StringCodePrinter
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.view.GraphView
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/** Exports circuits of Antares' standard library to Verilog. */
class VerilogStandardLibraryTest : AbstractStandardLibraryBasedCircuitTest() {

	companion object {
		private const val HALF_ADDER = "52255dc4-c010-4f6f-8ea6-9c2c8f5f9a82"
		private const val ASCII_16_SEG_DECODER = "dc031693-10ee-4310-8ad3-e43710e5f25f"
		private const val BUS_TRANSCEIVER_N_BIT = "74c772de-7336-48a7-814a-7d5bc7d1829b"
	}

	override fun createCircuit(): GraphView =
		LibraryModule.libraryHolder.getMetaGraph(UUID(HALF_ADDER)).graph.graphView

	@Test
	fun shouldExportHalfAdder() {
		assertEquals("""
			// Half Adder
			module Half_Adder (
			  input A,
			  input B,
			  output S,
			  output C
			);
			  assign #20 S = (A ^ B);
			  assign #20 C = (A & B);
			endmodule

		""".trimIndent(), design(circuit(HALF_ADDER)))
	}

	@Test
	fun shouldCreateHalfAdderTestBenchFromTestcase() {
		val result = testBench(circuit(HALF_ADDER))

		assertContains(result, "  Half_Adder main_0 (\n    .A(A),\n    .B(B),\n    .S(S),\n    .C(C)\n  );")
		assertContains(result, """
			|    A = 1'b1;
			|    B = 1'b1;
			|    #30;
			|    if (S !== 1'b0) begin
			|      ${'$'}display("assertion failed for S on line 3, expected %b, actual is %b", 1'b0, S);
			|      errors = errors + 1;
			|    end
			|    if (C !== 1'b1) begin
		""".trimMargin())
	}

	@Test
	fun shouldExportUnlabeledLookupTableOfAsciiDecoder() {
		val result = design(circuit(ASCII_16_SEG_DECODER))

		assertContains(result, "module Verilog_LookupTable (\n  input [7:0] A,\n  output reg [15:0] D);")
		assertContains(result, "      8'd0: D = 16'b0000000011111111;\n      8'd1: D = 16'b0000000000001100;\n")
		assertContains(result, "  Verilog_LookupTable node0 (\n    .A(I),\n    .D(O)\n  );")
	}

	@Test
	fun shouldExportBusTransceiverWithInOutPins() {
		val graph = circuit(BUS_TRANSCEIVER_N_BIT)

		val design = design(graph)
		assertContains(design, "  inout [3:0] A,\n  inout [3:0] B\n);")
		assertContains(design, "    .p1(A),\n    .EN(s1),\n    .p3(B)\n")

		// The first testcase drives B (">B") and checks A ("<A")
		val testBench = testBench(graph)
		assertContains(testBench, "    B_drv = 4'b0001;\n    #30;\n    if (A !== 4'b0001) begin")
	}

	private fun circuit(uuid: String): DigitalGraph =
		LibraryModule.libraryHolder.getMetaGraph(UUID(uuid)).graph.graphView.graph as DigitalGraph

	private fun design(graph: DigitalGraph): String {
		val printer = StringCodePrinter()
		VerilogGenerator(AbstractVerilogTest.testParams()).generateHDL(printer, graph)
		return printer.toString().removePrefix("${AbstractVerilogTest.TIMESCALE}\n\n")
	}

	private fun testBench(graph: DigitalGraph): String {
		val model = HDLModel(graph, VerilogRenaming())
			.create()
			.apply { renameLabels() }
		val params = HDLExportTestBenchParams(VerilogRenaming(), "tb", graph.testcases.testcases.first(), Paths.get("notUsed"), 30)
		val out = StringCodePrinter()
		VerilogTestBenchCreator(out, model, "tb", params).print()
		return out.toString()
	}
}
