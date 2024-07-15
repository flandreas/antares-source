package ch.scorpion.antares.model.hdl.verilog

import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.hdl.verilog.VerilogGenerator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.view.gate.LogicGateView
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
}