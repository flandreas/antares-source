package ch.scorpion.antares.model.hdl.verilog

import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.hdl.verilog.VerilogGenerator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.gate.TriStateBufferGate
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.gate.TriStateBufferGateView
import kotlin.test.Test
import kotlin.test.assertEquals

class VerilogTriStateBufferGateTest : AbstractVerilogTest() {

    @Test
    fun testSingleBit() {
        val builder = TestCircuitBuilder("test")
        val input = builder.addInput("I")
        val enable = builder.addInput("EN")
        val output = builder.addOutput("O")
        val gate = builder.addVerticeView(TriStateBufferGateView())
        builder.connect(input, gate, gate.model.getInputPort())
        builder.connect(enable, gate, gate.model.getEnablePort())
        builder.connect(gate, gate.model.getOutputPort(), output)

        VerilogGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

        assertEquals("""
            
            module Verilog_TriStateBufferGate
            (
              input  p1,
              input EN,
              output  p3
            );
              assign p3 = (EN == 1'b1)? p1 : 1'bz;
            endmodule
            
            module test (
              input I,
              input EN,
              output O
            );
              Verilog_TriStateBufferGate Verilog_TriStateBufferGate_i0 (
                .p1( I ),
                .EN( EN ),
                .p3( O )
              );
            endmodule
            
        """.trimIndent(), printer.toString())
    }

    @Test
    fun testMultiBit() {
        val builder = TestCircuitBuilder("test")
        val input = builder.addInput("I", BitWidth.BW_4)
        val enable = builder.addInput("EN")
        val output = builder.addOutput("O", BitWidth.BW_4)
        val gate = builder.addVerticeView(TriStateBufferGateView(model = TriStateBufferGate(BitWidth.BW_4)))
        builder.connect(input, gate, gate.model.getInputPort())
        builder.connect(enable, gate, gate.model.getEnablePort())
        builder.connect(gate, gate.model.getOutputPort(), output)

        VerilogGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

        assertEquals("""
            
            module Verilog_TriStateBufferGate_MultiBit#(
              parameter BIT_WIDTH = 2
            )
            (
              input [(BIT_WIDTH - 1):0] p1,
              input EN,
              output [(BIT_WIDTH - 1):0] p3
            );
              assign p3 = (EN == 1'b1)? p1 : {BIT_WIDTH{1'bz}};
            endmodule
            
            module test (
              input [3:0] I,
              input EN,
              output [3:0] O
            );
              Verilog_TriStateBufferGate_MultiBit #(
                .BIT_WIDTH(4)
              )
              Verilog_TriStateBufferGate_MultiBit_i0 (
                .p1( I ),
                .EN( EN ),
                .p3( O )
              );
            endmodule
        
        """.trimIndent(), printer.toString())
    }
}