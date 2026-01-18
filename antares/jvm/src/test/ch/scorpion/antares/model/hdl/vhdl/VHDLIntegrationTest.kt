package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.TestLibraryBuilder
import ch.scorpion.antares.hdl.vhdl.HDLException
import ch.scorpion.antares.hdl.vhdl.VHDLGenerator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.gate.AbstractLogicGate
import ch.scorpion.antares.model.input.Clock
import ch.scorpion.antares.model.input.DipSwitch
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.antares.view.input.ClockView
import ch.scorpion.antares.view.input.DipSwitchView
import ch.scorpion.antares.view.net.ConstantView
import ch.scorpion.antares.view.net.GroundView
import ch.scorpion.antares.view.net.PowerView
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VHDLIntegrationTest : AbstractVHDLTest() {

	@Test
	fun testNoOp() {
		VHDLGenerator(testParams()).generateHDL(
			printer,
			TestCircuitBuilder("test").buildNOP().graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity test is
			  port (
			    I: in std_logic;
			    O: out std_logic);
			end test;

			architecture Behavioral of test is
			begin
			  O <= I;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testSubCircuits() {
		val nop = TestLibraryBuilder().addNOP(library)
		val builder = TestCircuitBuilder("test")
		val input = builder.addInput("A")
		val output = builder.addOutput("B")
		val subGraphVV1 = builder.addVerticeView((library.get(TestLibraryBuilder.NOP) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<out SubGraphVertice>)
		val subGraphVV2 = builder.addVerticeView((library.get(TestLibraryBuilder.NOP) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<out SubGraphVertice>)
		builder.connect(input, subGraphVV1, subGraphVV1.model.getInput())
		builder.connect(subGraphVV1, subGraphVV1.model.getOutput(), subGraphVV2, subGraphVV2.model.getInput())
		builder.connect(subGraphVV2, subGraphVV2.model.getOutput(), output)

		val params = testParams()
		VHDLGenerator(params).generateHDL(printer, builder.graph as DigitalGraph)

		val entityName = params.renaming.checkName(nop.name)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- NOP
			entity $entityName is
			  port (
			    I: in std_logic;
			    O: out std_logic);
			end $entityName;

			architecture Behavioral of $entityName is
			begin
			  O <= I;
			end Behavioral;

			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;
			
			-- test
			entity test is
			  port (
			    A: in std_logic;
			    B: out std_logic);
			end test;
			
			architecture Behavioral of test is
			  signal s0: std_logic;
			begin
			  node0: entity work.$entityName
			    port map (
			      I => A,
			      O => s0);
			  node1: entity work.$entityName
			    port map (
			      I => s0,
			      O => B);
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testCustomNotGate() {
		VHDLGenerator(testParams())
			.generateHDL(printer, TestCircuitBuilder("test").buildCustomNot().graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity test is
			  port (
			    I1: in std_logic;
			    O1: out std_logic);
			end test;

			architecture Behavioral of test is
			begin
			  O1 <= NOT I1 after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testCustomNand() {
		VHDLGenerator(testParams())
			.generateHDL(printer, TestCircuitBuilder("test").buildCustomNAND(null).graph as DigitalGraph)


		// The result is not yet optimized (not yet implemented). The optimized result would be
		// O1 <= NOT (I1 AND I2), without the intermediate signal s0.
		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity test is
			  port (
			    I1: in std_logic;
			    I2: in std_logic;
			    O1: out std_logic);
			end test;

			architecture Behavioral of test is
			  signal s0: std_logic;
			begin
			  O1 <= NOT s0 after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns;
			  s0 <= (I1 AND I2) after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testAndGate() {
		assertNonUnaryLogicGate(LogicGateView.andGateView(), "O <= (A AND B) after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns")
	}

	@Test
	fun testOrGate() {
		assertNonUnaryLogicGate(LogicGateView.orGateView(), "O <= (A OR B) after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns")
	}

	@Test
	fun testXorGate() {
		assertNonUnaryLogicGate(LogicGateView.xorGateView(), "O <= (A XOR B) after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns")
	}

	@Test
	fun testNandGate() {
		assertNonUnaryLogicGate(LogicGateView.nandGateView(), "O <= NOT (A AND B) after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns")
	}

	@Test
	fun testNorGate() {
		assertNonUnaryLogicGate(LogicGateView.norGateView(), "O <= NOT (A OR B) after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns")
	}

	@Test
	fun testXnorGate() {
		assertNonUnaryLogicGate(LogicGateView.xnorGateView(), "O <= NOT (A XOR B) after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns")
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

		VHDLGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity test is
			  port (
			    A: in std_logic;
			    B: in std_logic;
			    O: out std_logic);
			end test;

			architecture Behavioral of test is
			begin
			  ${expression};
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testNotGate() {
		assertUnaryLogicGate(LogicGateView.notGateView(), "O <= NOT I after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns")
	}

	@Test
	fun testBufferGate() {
		assertUnaryLogicGate(LogicGateView.bufferGateView(), "O <= I after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns")
	}

	private fun assertUnaryLogicGate(gateView: LogicGateView, expression: String) {
		val builder = TestCircuitBuilder("test")
		val input = builder.addInput("I")
		val output = builder.addOutput("O")
		builder.addVerticeView(gateView)
		builder.connect(input, gateView, gateView.model.getInput())
		builder.connect(gateView, output)

		VHDLGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity test is
			  port (
			    I: in std_logic;
			    O: out std_logic);
			end test;

			architecture Behavioral of test is
			begin
			  ${expression};
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testConstant() {
		val builder = TestCircuitBuilder("test")
		val output = builder.addOutput("O")
		val constantView = builder.addVerticeView(ConstantView(LongValueImpl(1)))
		builder.connect(constantView, output)

		VHDLGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity test is
			  port (
			    O: out std_logic);
			end test;

			architecture Behavioral of test is
			begin
			  O <= '1';
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testDipSwitch() {
		val builder = TestCircuitBuilder("test")
		val output = builder.addOutput("O")
		val dipSwitch = builder.addVerticeView(DipSwitchView(model = DipSwitch().also {
			it.initialValue = DigitalSignalFactory.of(true) }
		))
		builder.connect(dipSwitch, output)

		VHDLGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity test is
			  port (
			    O: out std_logic);
			end test;

			architecture Behavioral of test is
			begin
			  O <= '1';
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testPowerAndGround() {
		val builder = TestCircuitBuilder("test")
		val output1 = builder.addOutput("O1")
		val output2 = builder.addOutput("O2")
		val powerView = builder.addVerticeView(PowerView())
		val groundView = builder.addVerticeView(GroundView())
		builder.connect(powerView, output1)
		builder.connect(groundView, output2)

		VHDLGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity test is
			  port (
			    O1: out std_logic;
			    O2: out std_logic);
			end test;

			architecture Behavioral of test is
			begin
			  O1 <= '1';
			  O2 <= '0';
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testNegatedGateInput() {
		val builder = TestCircuitBuilder("test")
		val input1 = builder.addInput("A")
		val input2 = builder.addInput("B")
		val output = builder.addOutput("O")
		val gateView = builder.addVerticeView(LogicGateView.andGateView())
		gateView.model.setNegateInput(1, true)
		builder.connect(input1, gateView, gateView.model.getInput(1))
		builder.connect(input2, gateView, gateView.model.getInput(2))
		builder.connect(gateView, output)

		VHDLGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity test is
			  port (
			    A: in std_logic;
			    B: in std_logic;
			    O: out std_logic);
			end test;

			architecture Behavioral of test is
			begin
			  O <= (NOT A AND B) after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testClock() {
		VHDLGenerator(testParams()).generateHDL(printer, buildClockCircuit("CLK"))

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity test is
			  port (
			    CLK: in std_logic;
			    O: out std_logic);
			end test;

			architecture Behavioral of test is
			begin
			  O <= CLK;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun shouldRejectClockWithoutName() {
		assertFailsWith(HDLException::class) {
			VHDLGenerator(testParams()).generateHDL(printer, buildClockCircuit(""))
		}
	}

	@Test
	fun shouldRejectClockWithoutUniqueName() {
		assertFailsWith(HDLException::class) {
			VHDLGenerator(testParams()).generateHDL(printer, buildClockCircuit("O"))
		}
	}

	private fun buildClockCircuit(clockName: String?): DigitalGraph {
		val builder = TestCircuitBuilder("test")
		val clock = builder.addVerticeView(ClockView(model = Clock(clockName)))
		val output = builder.addOutput("O")
		builder.connect(clock, output)
		return builder.graph as DigitalGraph
	}
}