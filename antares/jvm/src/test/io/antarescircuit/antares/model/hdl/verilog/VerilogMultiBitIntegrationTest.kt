package io.antarescircuit.antares.model.hdl.verilog

import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.gate.AbstractLogicGate
import io.antarescircuit.antares.model.net.BranchCount
import io.antarescircuit.antares.model.net.Concentrator
import io.antarescircuit.antares.model.net.Splitter
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.view.net.ConcentratorView
import io.antarescircuit.antares.view.net.SplitterView
import kotlin.test.Test
import kotlin.test.assertEquals

class VerilogMultiBitIntegrationTest : AbstractVerilogTest() {

	@Test
	fun shouldDeclareVectorPortsAndWires() {
		val delay = AbstractLogicGate.DEFAULT_PROPAGATION_DELAY

		assertEquals("""
			// test
			module test (
			  input [3:0] I1,
			  input [3:0] I2,
			  output [3:0] O1
			);
			  wire [3:0] s0;
			  assign #$delay O1 = ~s0;
			  assign #$delay s0 = (I1 & I2);
			endmodule

		""".trimIndent(), generate(TestCircuitBuilder("test").buildCustomNAND(null, BitWidth.BW_4).graph as DigitalGraph))
	}

	@Test
	fun shouldConcatenateConcentratorInputsMostSignificantFirst() {
		val builder = TestCircuitBuilder("test")
		val input1 = builder.addInput("I0", BitWidth.BW_2)
		val input2 = builder.addInput("I1", BitWidth.BW_2)
		val output = builder.addOutput("O", BitWidth.BW_4)
		val concentrator = builder.addVerticeView(ConcentratorView(model = Concentrator(BitWidth.BW_4, BranchCount.BC_2)))
		builder.connect(input1, concentrator, concentrator.model.getInput(2))
		builder.connect(input2, concentrator, concentrator.model.getInput(3))
		builder.connect(concentrator, concentrator.model.getOutput(1), output)

		assertEquals("""
			// test
			module test (
			  input [1:0] I0,
			  input [1:0] I1,
			  output [3:0] O
			);
			  assign O = {I1, I0};
			endmodule

		""".trimIndent(), generate(builder.graph as DigitalGraph))
	}

	@Test
	fun shouldSelectSingleBitsForNarrowSplitterOutputs() {
		assertSplitter(BitWidth.BW_2, BitWidth.BW_1, "I[0]", "I[1]")
	}

	@Test
	fun shouldSelectPartsForWideSplitterOutputs() {
		assertSplitter(BitWidth.BW_4, BitWidth.BW_2, "I[1:0]", "I[3:2]")
	}

	private fun assertSplitter(inputWidth: BitWidth, outputWidth: BitWidth, select1: String, select2: String) {
		val builder = TestCircuitBuilder("test")
		val input = builder.addInput("I", inputWidth)
		val output1 = builder.addOutput("O1", outputWidth)
		val output2 = builder.addOutput("O2", outputWidth)
		val splitter = builder.addVerticeView(SplitterView(model = Splitter(inputWidth, BranchCount.BC_2)))
		builder.connect(input, splitter)
		builder.connect(splitter, splitter.model.getOutput(2), output1)
		builder.connect(splitter, splitter.model.getOutput(3), output2)

		val range = if (outputWidth.width > 1) "[${outputWidth.width - 1}:0] " else ""
		assertEquals("""
			// test
			module test (
			  input [${inputWidth.width - 1}:0] I,
			  output ${range}O1,
			  output ${range}O2
			);
			  assign O1 = $select1;
			  assign O2 = $select2;
			endmodule

		""".trimIndent(), generate(builder.graph as DigitalGraph))
	}
}
