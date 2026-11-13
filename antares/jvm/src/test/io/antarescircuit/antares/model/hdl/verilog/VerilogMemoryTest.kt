package io.antarescircuit.antares.model.hdl.verilog

import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.hdl.vhdl.HDLException
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.addressable.LookupTable
import io.antarescircuit.antares.model.addressable.RAM
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.view.addressable.LookupTableView
import io.antarescircuit.antares.view.addressable.RAMView
import io.antarescircuit.antares.view.addressable.ROMView
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class VerilogMemoryTest : AbstractVerilogTest() {

	@Test
	fun shouldExportRAMWithBidirectionalDataPort() {
		assertEquals("""
			module Verilog_RAM #(
			  parameter addrBitWidth = 8,
			  parameter dataBitWidth = 8
			) (
			  input [addrBitWidth-1:0] A,
			  input CLK,
			  input CS,
			  input WR,
			  input CLR,
			  inout [dataBitWidth-1:0] D);
			  reg [dataBitWidth-1:0] memory [0:(1 << addrBitWidth) - 1];
			  integer i;
			  always @(posedge CLK or posedge CLR) begin
			    if (CLR) begin
			      for (i = 0; i < (1 << addrBitWidth); i = i + 1)
			        memory[i] <= {dataBitWidth{1'b0}};
			    end else if (CS && WR) begin
			      memory[A] <= D;
			    end
			  end
			  assign D = (CS && !WR) ? memory[A] : {dataBitWidth{1'bz}};
			endmodule

			// test
			module test (
			  input [3:0] A,
			  input CS,
			  input WR,
			  input CLR,
			  input CLK,
			  output [15:0] D
			);
			  Verilog_RAM #(
			    .addrBitWidth(4),
			    .dataBitWidth(16)
			  ) node0 (
			    .A(A),
			    .CS(CS),
			    .WR(WR),
			    .CLR(CLR),
			    .CLK(CLK),
			    .D(D)
			  );
			endmodule

		""".trimIndent(), generate(buildRAMCircuit(RAM().apply {
			addressWidth = BitWidth.BW_4
			dataWidth = BitWidth.BW_16
		})))
	}

	@Test
	fun shouldExportRAMWithSeparateDataPorts() {
		val result = generate(buildRAMCircuit(RAM(separateDataPorts = true)))

		assertContains(result, "module Verilog_RAM_SeparateDataPorts #(")
		assertContains(result, "  input [dataBitWidth-1:0] DI,\n  output [dataBitWidth-1:0] D);")
		assertContains(result, "memory[A] <= DI;")
		assertContains(result, "assign D = CS ? memory[A] : {dataBitWidth{1'bz}};")
		assertContains(result, "    .DI(DI),\n")
	}

	@Test
	fun shouldRejectRAMWithoutClock() {
		assertFailsWith(HDLException::class) {
			generate(buildRAMCircuit(RAM(hasClock = false)))
		}
	}

	private fun buildRAMCircuit(ram: RAM): DigitalGraph {
		val builder = TestCircuitBuilder("test")
		val ramView = builder.addVerticeView(RAMView(model = ram))
		val a = builder.addInput("A", ram.addressWidth)
		val cs = builder.addInput("CS")
		val wr = builder.addInput("WR")
		val clr = builder.addInput("CLR")
		builder.connect(a, ramView, ram.getAddressInput())
		ram.getClockInput()?.let { builder.connect(builder.addInput("CLK"), ramView, it) }
		builder.connect(cs, ramView, ram.getChipSelectInput())
		builder.connect(wr, ramView, ram.getWriteInput())
		builder.connect(clr, ramView, ram.getClearInput())
		ram.getDataInput()?.let { builder.connect(builder.addInput("DI", ram.dataWidth), ramView, it) }
		builder.connect(ramView, ram.getDataPort(), builder.addOutput("D", ram.dataWidth))
		return builder.graph as DigitalGraph
	}

	@Test
	fun shouldExportROMContentAsCaseStatement() {
		val builder = TestCircuitBuilder("test")
		val a = builder.addInput("A", BitWidth.BW_8)
		val cs = builder.addInput("CS")
		val d = builder.addOutput("D", BitWidth.BW_8)
		val romView = builder.addVerticeView(ROMView())
		romView.model.setDataAt(0, 11UL, null)
		romView.model.setDataAt(1, 22UL, null)
		romView.text = TranslatableText("ABC")
		builder.connect(a, romView, romView.model.getAddressInput())
		builder.connect(cs, romView, romView.model.getChipSelectInput())
		builder.connect(romView, romView.model.getDataPort(), d)

		assertEquals("""
			module Verilog_ROM_ABC (
			  input [7:0] A,
			  input CS,
			  output [7:0] D);
			  reg [7:0] data;
			  always @(*) begin
			    case (A)
			      8'd0: data = 8'b00001011;
			      8'd1: data = 8'b00010110;
			      default: data = 8'b0;
			    endcase
			  end
			  assign D = CS ? data : 8'bz;
			endmodule

			// test
			module test (
			  input [7:0] A,
			  input CS,
			  output [7:0] D
			);
			  Verilog_ROM_ABC node0 (
			    .A(A),
			    .CS(CS),
			    .D(D)
			  );
			endmodule

		""".trimIndent(), generate(builder.graph as DigitalGraph))
	}

	@Test
	fun shouldExportLookupTableWithLegalizedLabel() {
		val builder = TestCircuitBuilder("test")
		val a = builder.addInput("A", BitWidth.BW_4)
		val d = builder.addOutput("D")
		val lutView = builder.addVerticeView(LookupTableView(model = LookupTable(BitWidth.BW_4, BitWidth.BW_1)))
		lutView.model.name = "my lut"
		lutView.model.setDataAt(1, 1UL, null)
		builder.connect(a, lutView)
		builder.connect(lutView, d)

		assertEquals("""
			module Verilog_LookupTable_my_lut (
			  input [3:0] A,
			  output reg D);
			  always @(*) begin
			    case (A)
			      4'd0: D = 1'b0;
			      4'd1: D = 1'b1;
			      default: D = 1'b0;
			    endcase
			  end
			endmodule

			// test
			module test (
			  input [3:0] A,
			  output D
			);
			  Verilog_LookupTable_my_lut node0 (
			    .A(A),
			    .D(D)
			  );
			endmodule

		""".trimIndent(), generate(builder.graph as DigitalGraph))
	}

	@Test
	fun shouldNumberUnlabeledLookupTablesWithDifferentContent() {
		val builder = TestCircuitBuilder("test")
		addLookupTable(builder, "1", 1UL)
		addLookupTable(builder, "2", 1UL)
		addLookupTable(builder, "3", 2UL)

		val result = generate(builder.graph as DigitalGraph)

		assertContains(result, "module Verilog_LookupTable (")
		assertContains(result, "module Verilog_LookupTable_1 (")
		assertFalse(result.contains("module Verilog_LookupTable_2"))
		assertContains(result, "  Verilog_LookupTable node0 (")
		assertContains(result, "  Verilog_LookupTable node1 (")
		assertContains(result, "  Verilog_LookupTable_1 node2 (")
	}

	private fun addLookupTable(builder: TestCircuitBuilder, suffix: String, value: ULong) {
		val lutView = builder.addVerticeView(LookupTableView(model = LookupTable(BitWidth.BW_1, BitWidth.BW_2)))
		lutView.model.setDataAt(0, value, null)
		builder.connect(builder.addInput("A$suffix"), lutView)
		builder.connect(lutView, builder.addOutput("D$suffix", BitWidth.BW_2))
	}
}
