package ch.scorpion.antares.model.hdl.verilog

import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.TestLibraryBuilder
import ch.scorpion.antares.hdl.verilog.VerilogGenerator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.input.DipSwitch
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.antares.view.input.DipSwitchView
import ch.scorpion.antares.view.net.ConstantView
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

class VerilogIntegrationTest : AbstractVerilogTest() {

    @Test
    fun testNandGate() {
        assertNonUnaryLogicGate(LogicGateView.nandGateView(), "assign O = ~ (A & B)")
    }

    @Test
    fun testOrGate() {
        assertNonUnaryLogicGate(LogicGateView.orGateView(), "assign O = (A | B)")
    }

    @Test
    fun testAndGate() {
        assertNonUnaryLogicGate(LogicGateView.andGateView(), "assign O = (A & B)")
    }

    @Test
    fun testXorGate() {
        assertNonUnaryLogicGate(LogicGateView.xorGateView(), "assign O = (A ^ B)")
    }

    private fun assertNonUnaryLogicGate(gateView: LogicGateView, expression: String) {
        val builder = TestCircuitBuilder("test")
        val inputA = builder.addInput("A")
        val inputB = builder.addInput("B")
        val output = builder.addOutput("O")
        builder.addVerticeView(gateView)
        builder.connect(inputA, gateView, gateView.model.getInput(1))
        builder.connect(inputB, gateView, gateView.model.getInput(2))
        builder.connect(gateView, output)

        VerilogGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

        assertEquals("""
            
            module test (
              input A,
              input B,
              output O
            );
              $expression;
            endmodule
            
        """.trimIndent(), printer.toString())
    }

    @Test
    fun testSubCircuit() {
        val nop = TestLibraryBuilder().addNOP(library)
        val builder = TestCircuitBuilder("test")
        val input = builder.addInput("A")
        val output = builder.addOutput("B")
        val subGraphVV1 = builder.addVerticeView((library.get(TestLibraryBuilder.NOP) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<out SubGraphVertice>)
        val subGraphVV2 = builder.addVerticeView((library.get(TestLibraryBuilder.NOP) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<out SubGraphVertice>)
        builder.connect(input, subGraphVV1, subGraphVV1.model.getInput())
        builder.connect(subGraphVV1, subGraphVV1.model.getOutput(), subGraphVV2, subGraphVV2.model.getInput())
        builder.connect(subGraphVV2, subGraphVV2.model.getOutput(), output)

        VerilogGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

        assertEquals("""
            
            module NOP (
              input I,
              output O
            );
              assign O = I;
            endmodule

            module test (
              input A,
              output B
            );
              wire s0;
              NOP NOP_i0 (
                .I( A ),
                .O( s0 )
              );
              NOP NOP_i1 (
                .I( s0 ),
                .O( B )
              );
            endmodule
            
        """.trimIndent(), printer.toString())
    }

    @Test
    fun testConstant() {
        val builder = TestCircuitBuilder("constant")
        val output = builder.addOutput("O")
        val constantView = builder.addVerticeView(ConstantView(DigitalSignalFactory.of(true)))
        builder.connect(constantView, output)

        VerilogGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

        assertEquals("""
            
            module constant (
              output O
            );
              assign O = 1'b1;
            endmodule
            
        """.trimIndent(), printer.toString())
    }

    @Test
    fun testDipSwitch() {
        val builder = TestCircuitBuilder("dip-switch")
        val output = builder.addOutput("O")
        val dipSwitch = builder.addVerticeView(DipSwitchView(model = DipSwitch().also {
            it.initialValue = DigitalSignalFactory.of(false) }
        ))
        builder.connect(dipSwitch, output)

        VerilogGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

        assertEquals("""
            
            module \dip-switch  (
              output O
            );
              assign O = 1'b0;
            endmodule
            
        """.trimIndent(), printer.toString())
    }
}