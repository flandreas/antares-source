package io.antarescircuit.antares.model.hdl.verilog

import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.gate.TriStateBufferGate
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.view.gate.TriStateBufferGateView
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class VerilogTriStateBufferGateTest : AbstractVerilogTest() {

	@Test
	fun shouldShareModuleBetweenBitWidths() {
		val builder = TestCircuitBuilder("test")
		addGate(builder, "1", TriStateBufferGate())
		addGate(builder, "2", TriStateBufferGate(BitWidth.BW_4))

		assertEquals("""
			module Verilog_TriStateBufferGate #(
			  parameter bitWidth = 1
			) (
			  input [bitWidth-1:0] p1,
			  input EN,
			  output [bitWidth-1:0] p3);
			  assign p3 = (EN == 1'b1) ? p1 : {bitWidth{1'bz}};
			endmodule

			// test
			module test (
			  input I1,
			  input EN1,
			  input [3:0] I2,
			  input EN2,
			  output O1,
			  output [3:0] O2
			);
			  Verilog_TriStateBufferGate #(
			    .bitWidth(1)
			  ) node0 (
			    .p1(I1),
			    .EN(EN1),
			    .p3(O1)
			  );
			  Verilog_TriStateBufferGate #(
			    .bitWidth(4)
			  ) node1 (
			    .p1(I2),
			    .EN(EN2),
			    .p3(O2)
			  );
			endmodule

		""".trimIndent(), generate(builder.graph as DigitalGraph))
	}

	@Test
	fun shouldUseSeparateModuleForActiveLowEnable() {
		val builder = TestCircuitBuilder("test")
		addGate(builder, "1", TriStateBufferGate())
		addGate(builder, "2", TriStateBufferGate(enableLogic = Logic.NEGATIVE))

		val result = generate(builder.graph as DigitalGraph)

		assertContains(result, "module Verilog_TriStateBufferGate #(")
		assertContains(result, "module Verilog_TriStateBufferGate_ActiveLow #(")
		assertContains(result, "assign p3 = (EN == 1'b0) ? p1 : {bitWidth{1'bz}};")
		assertContains(result, "  Verilog_TriStateBufferGate_ActiveLow #(\n    .bitWidth(1)\n  ) node1 (")
	}

	private fun addGate(builder: TestCircuitBuilder, suffix: String, model: TriStateBufferGate) {
		val input = builder.addInput("I$suffix", model.bitWidth)
		val enable = builder.addInput("EN$suffix")
		val output = builder.addOutput("O$suffix", model.bitWidth)
		val gate = builder.addVerticeView(TriStateBufferGateView(model = model))
		builder.connect(input, gate, gate.model.getInputPort())
		builder.connect(enable, gate, gate.model.getEnablePort())
		builder.connect(gate, gate.model.getOutputPort(), output)
	}
}
