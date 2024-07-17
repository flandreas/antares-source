package ch.scorpion.antares.model.hdl.verilog

import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.hdl.verilog.VerilogGenerator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.addressable.LookupTable
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.addressable.LookupTableView
import kotlin.test.Test
import kotlin.test.assertEquals

class VerilogLookupTableTest : AbstractVerilogTest() {

    @Test
    fun testLUT() {
        val builder = TestCircuitBuilder("test")
        val a = builder.addInput("A", BitWidth.BW_4)
        val d = builder.addOutput("D", BitWidth.BW_8)
        val lutView = builder.addVerticeView(LookupTableView(model = LookupTable(BitWidth.BW_4, BitWidth.BW_8)))
        lutView.model.name = "A"
        lutView.model.setDataAt(0, 1UL, null)
        lutView.model.setDataAt(1, 2UL, null)
        lutView.model.setDataAt(2, 3UL, null)
        lutView.model.setDataAt(3, 4UL, null)
        builder.connect(a, lutView)
        builder.connect(lutView, d)

        VerilogGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

        assertEquals("""
            
            module Verilog_LookupTable_A (
              input [3:0] A,
              output reg [7:0] D
            );
              reg [7:0] lut [0:3];
            
              always @ (*) begin
                D = lut[A];
              end
            
              initial begin
                lut[0] = 8'h1;
                lut[1] = 8'h2;
                lut[2] = 8'h3;
                lut[3] = 8'h4;
              end
            endmodule
            
            module test (
              input [3:0] A,
              output [7:0] D
            );
              Verilog_LookupTable_A Verilog_LookupTable_A_i0 (
                .A( A ),
                .D( D )
              );
            endmodule

        """.trimIndent(), printer.toString())
    }
}