package io.antarescircuit.antares.model.hdl.verilog

import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.TestLibraryBuilder
import io.antarescircuit.antares.hdl.vhdl.HDLException
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.gate.AbstractLogicGate
import io.antarescircuit.antares.model.input.Clock
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.input.ClockView
import io.antarescircuit.antares.view.net.ConstantView
import io.antarescircuit.antares.view.net.GroundView
import io.antarescircuit.antares.view.net.PowerView
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVertice
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VerilogIntegrationTest : AbstractVerilogTest() {

	private val delay get() = AbstractLogicGate.DEFAULT_PROPAGATION_DELAY

	@Test
	fun shouldStartWithTimescale() {
		generate(TestCircuitBuilder("test").buildNOP().graph as DigitalGraph)

		assertEquals(TIMESCALE, printer.toString().lines().first())
	}

	@Test
	fun shouldExportNoOp() {
		assertEquals("""
			// test
			module test (
			  input I,
			  output O
			);
			  assign O = I;
			endmodule

		""".trimIndent(), generate(TestCircuitBuilder("test").buildNOP().graph as DigitalGraph))
	}

	@Test
	fun shouldExportReferencedSubCircuitOnce() {
		val nop = TestLibraryBuilder().addNOP(library)
		val builder = TestCircuitBuilder("test")
		val input = builder.addInput("A")
		val output = builder.addOutput("B")
		val subGraphVV1 = builder.addVerticeView((library.get(TestLibraryBuilder.NOP) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<out SubGraphVertice>)
		val subGraphVV2 = builder.addVerticeView((library.get(TestLibraryBuilder.NOP) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<out SubGraphVertice>)
		builder.connect(input, subGraphVV1, subGraphVV1.model.getInput())
		builder.connect(subGraphVV1, subGraphVV1.model.getOutput(), subGraphVV2, subGraphVV2.model.getInput())
		builder.connect(subGraphVV2, subGraphVV2.model.getOutput(), output)

		val result = generate(builder.graph as DigitalGraph)

		val moduleName = testParams().renaming.checkName(nop.name)
		assertEquals("""
			// NOP
			module $moduleName (
			  input I,
			  output O
			);
			  assign O = I;
			endmodule

			// test
			module test (
			  input A,
			  output B
			);
			  wire s0;
			  $moduleName node0 (
			    .I(A),
			    .O(s0)
			  );
			  $moduleName node1 (
			    .I(s0),
			    .O(B)
			  );
			endmodule

		""".trimIndent(), result)
	}

	@Test
	fun shouldExportIntermediateWire() {
		assertEquals("""
			// test
			module test (
			  input I1,
			  input I2,
			  output O1
			);
			  wire s0;
			  assign #$delay O1 = ~s0;
			  assign #$delay s0 = (I1 & I2);
			endmodule

		""".trimIndent(), generate(TestCircuitBuilder("test").buildCustomNAND(null).graph as DigitalGraph))
	}

	@Test
	fun shouldOmitDelaysWithoutDelayModel() {
		val result = generate(TestCircuitBuilder("test").buildCustomNot().graph as DigitalGraph, useDelayModel = false)

		assertEquals("""
			// test
			module test (
			  input I1,
			  output O1
			);
			  assign O1 = ~I1;
			endmodule

		""".trimIndent(), result)
	}

	@Test
	fun shouldExportAndGate() {
		assertNonUnaryLogicGate(LogicGateView.andGateView(), "assign #$delay O = (A & B);")
	}

	@Test
	fun shouldExportOrGate() {
		assertNonUnaryLogicGate(LogicGateView.orGateView(), "assign #$delay O = (A | B);")
	}

	@Test
	fun shouldExportXorGate() {
		assertNonUnaryLogicGate(LogicGateView.xorGateView(), "assign #$delay O = (A ^ B);")
	}

	@Test
	fun shouldExportNandGate() {
		assertNonUnaryLogicGate(LogicGateView.nandGateView(), "assign #$delay O = ~(A & B);")
	}

	@Test
	fun shouldExportNorGate() {
		assertNonUnaryLogicGate(LogicGateView.norGateView(), "assign #$delay O = ~(A | B);")
	}

	@Test
	fun shouldExportXnorGate() {
		assertNonUnaryLogicGate(LogicGateView.xnorGateView(), "assign #$delay O = ~(A ^ B);")
	}

	@Test
	fun shouldExportNegatedGateInput() {
		val gateView = LogicGateView.andGateView()
		gateView.model.setNegateInput(1, true)

		assertNonUnaryLogicGate(gateView, "assign #$delay O = (~A & B);")
	}

	private fun assertNonUnaryLogicGate(gateView: LogicGateView, assignment: String) {
		val builder = TestCircuitBuilder("test")
		val inputA = builder.addInput("A")
		val inputB = builder.addInput("B")
		val output = builder.addOutput("O")
		builder.addVerticeView(gateView)
		builder.connect(inputA, gateView, gateView.model.getInput(1))
		builder.connect(inputB, gateView, gateView.model.getInput(2))
		builder.connect(gateView, output)

		assertEquals("""
			// test
			module test (
			  input A,
			  input B,
			  output O
			);
			  $assignment
			endmodule

		""".trimIndent(), generate(builder.graph as DigitalGraph))
	}

	@Test
	fun shouldExportNotGate() {
		assertUnaryLogicGate(LogicGateView.notGateView(), "assign #$delay O = ~I;")
	}

	@Test
	fun shouldExportBufferGate() {
		assertUnaryLogicGate(LogicGateView.bufferGateView(), "assign #$delay O = I;")
	}

	private fun assertUnaryLogicGate(gateView: LogicGateView, assignment: String) {
		val builder = TestCircuitBuilder("test")
		val input = builder.addInput("I")
		val output = builder.addOutput("O")
		builder.addVerticeView(gateView)
		builder.connect(input, gateView, gateView.model.getInput())
		builder.connect(gateView, output)

		assertEquals("""
			// test
			module test (
			  input I,
			  output O
			);
			  $assignment
			endmodule

		""".trimIndent(), generate(builder.graph as DigitalGraph))
	}

	@Test
	fun shouldExportConstantPowerAndGround() {
		val builder = TestCircuitBuilder("test")
		val output1 = builder.addOutput("O1")
		val output2 = builder.addOutput("O2")
		val output3 = builder.addOutput("O3")
		val constantView = builder.addVerticeView(ConstantView(LongValueImpl(1)))
		val powerView = builder.addVerticeView(PowerView())
		val groundView = builder.addVerticeView(GroundView())
		builder.connect(constantView, output1)
		builder.connect(powerView, output2)
		builder.connect(groundView, output3)

		assertEquals("""
			// test
			module test (
			  output O1,
			  output O2,
			  output O3
			);
			  assign O1 = 1'b1;
			  assign O2 = 1'b1;
			  assign O3 = 1'b0;
			endmodule

		""".trimIndent(), generate(builder.graph as DigitalGraph))
	}

	@Test
	fun shouldExportClockAsInput() {
		assertEquals("""
			// test
			module test (
			  input CLK,
			  output O
			);
			  assign O = CLK;
			endmodule

		""".trimIndent(), generate(buildClockCircuit("CLK")))
	}

	@Test
	fun shouldRejectClockWithoutName() {
		assertFailsWith(HDLException::class) {
			generate(buildClockCircuit(""))
		}
	}

	@Test
	fun shouldRenameKeywords() {
		val result = generate(TestCircuitBuilder("test").buildNOP(outputName = "output").graph as DigitalGraph)

		assertEquals("""
			// test
			module test (
			  input I,
			  output p_output
			);
			  assign p_output = I;
			endmodule

		""".trimIndent(), result)
	}

	@Test
	fun shouldKeepNamesDifferingOnlyInCase() {
		val builder = TestCircuitBuilder("test")
		val input = builder.addInput("a")
		val output = builder.addOutput("A")
		builder.connect(input, output)

		assertEquals("""
			// test
			module test (
			  input a,
			  output A
			);
			  assign A = a;
			endmodule

		""".trimIndent(), generate(builder.graph as DigitalGraph))
	}

	private fun buildClockCircuit(clockName: String?): DigitalGraph {
		val builder = TestCircuitBuilder("test")
		val clock = builder.addVerticeView(ClockView(model = Clock(clockName)))
		val output = builder.addOutput("O")
		builder.connect(clock, output)
		return builder.graph as DigitalGraph
	}
}
