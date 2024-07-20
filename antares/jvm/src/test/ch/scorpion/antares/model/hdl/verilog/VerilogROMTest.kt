package ch.scorpion.antares.model.hdl.verilog

import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.hdl.verilog.VerilogGenerator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.addressable.ROMView
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import kotlin.test.Test
import kotlin.test.assertEquals

class VerilogROMTest : AbstractVerilogTest() {

    @Test
    fun testROM() {
        val builder = TestCircuitBuilder("test")
        val a = builder.addInput("A", BitWidth.BW_4)
        val cs = builder.addInput("CS")
        val d = builder.addOutput("D", BitWidth.BW_8)
        val romView = builder.addVerticeView(ROMView().also { it.addressWidth = BitWidth.BW_4 })
        romView.model.setDataAt(0, 11UL, null)
        romView.model.setDataAt(1, 22UL, null)
        romView.model.setDataAt(5, 255UL, null)
        romView.text = TranslatableText("Abc")
        builder.connect(a, romView, romView.model.getAddressInput())
        builder.connect(cs, romView, romView.model.getChipSelectInput())
        builder.connect(romView, romView.model.getDataPort(), d)

        VerilogGenerator(testParams())
            .generateHDL(printer, builder.graph as DigitalGraph)

        assertEquals("""
            
            module Verilog_ROM_Abc (
              input [3:0] A,
              input CS,
              output reg [7:0] D
            );
              reg [7:0] my_rom [0:5];

              always @ (*) begin
                if (~CS)
                  D = 8'hz;
                else if (A >= 4'b0110)
                  D = 8'h0;
                else
                  D = my_rom[A];
              end

              initial begin
                my_rom[0] = 8'hB;
                my_rom[1] = 8'h16;
                my_rom[2] = 8'h0;
                my_rom[3] = 8'h0;
                my_rom[4] = 8'h0;
                my_rom[5] = 8'hFF;
              end
            endmodule

            module test (
              input [3:0] A,
              input CS,
              output [7:0] D
            );
              Verilog_ROM_Abc Verilog_ROM_Abc_i0 (
                .A( A ),
                .CS( CS ),
                .D( D )
              );
            endmodule
            
        """.trimIndent(), printer.toString())
    }
}