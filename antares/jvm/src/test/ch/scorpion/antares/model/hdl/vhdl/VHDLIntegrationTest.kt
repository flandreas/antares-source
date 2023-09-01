package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.AbstractJvmCircuitTest
import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.TestLibraryBuilder
import ch.scorpion.antares.hdl.vhdl.VHDLGenerator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.gate.AbstractLogicGate
import ch.scorpion.antares.model.input.DipSwitch
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.antares.view.input.DipSwitchView
import ch.scorpion.antares.view.net.ConstantView
import ch.scorpion.antares.view.net.GroundView
import ch.scorpion.antares.view.net.PowerView
import ch.scorpion.jabbah.base.io.StringCodePrinter
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLIntegrationTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val library get() = LibraryModule.libraryHolder.library
	private val printer = StringCodePrinter()

	@BeforeTest
	fun setup() {
		AbstractJvmCircuitTest.setupLibrary()
	}

	@Test
	fun testNoOp() {
		VHDLGenerator(library, printer).generate(
			TestCircuitBuilder("test").buildNOP().graph as DigitalGraph)

		assertEquals("""
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;

			-- test
			entity main is
			  port (
			    I: in std_logic;
			    O: out std_logic);
			end main;

			architecture Behavioral of main is
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

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;

			-- NOP
			entity ${nop.uuid} is
			  port (
			    I: in std_logic;
			    O: out std_logic);
			end ${nop.uuid};

			architecture Behavioral of ${nop.uuid} is
			begin
			  O <= I;
			end Behavioral;

			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;
			
			-- test
			entity main is
			  port (
			    A: in std_logic;
			    B: out std_logic);
			end main;
			
			architecture Behavioral of main is
			  signal s0: std_logic;
			begin
			  node0: entity work.${nop.uuid}
			    port map (
			      I => A,
			      O => s0);
			  node1: entity work.${nop.uuid}
			    port map (
			      I => s0,
			      O => B);
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testCustomNotGate() {
		VHDLGenerator(library, printer)
			.generate(TestCircuitBuilder("test").buildCustomNot().graph as DigitalGraph)

		assertEquals("""
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;

			-- test
			entity main is
			  port (
			    I1: in std_logic;
			    O1: out std_logic);
			end main;

			architecture Behavioral of main is
			begin
			  O1 <= NOT I1 after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testCustomNand() {
		VHDLGenerator(library, printer)
			.generate(TestCircuitBuilder("test").buildCustomNAND(null).graph as DigitalGraph)


		// The result is not yet optimized (not yet implemented). The optimized result would be
		// O1 <= NOT (I1 AND I2), without the intermediate signal s0.
		assertEquals("""
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;

			-- test
			entity main is
			  port (
			    I1: in std_logic;
			    I2: in std_logic;
			    O1: out std_logic);
			end main;

			architecture Behavioral of main is
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

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;

			-- test
			entity main is
			  port (
			    A: in std_logic;
			    B: in std_logic;
			    O: out std_logic);
			end main;

			architecture Behavioral of main is
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

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;

			-- test
			entity main is
			  port (
			    I: in std_logic;
			    O: out std_logic);
			end main;

			architecture Behavioral of main is
			begin
			  ${expression};
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testConstant() {
		val builder = TestCircuitBuilder("test")
		val output = builder.addOutput("O")
		val constantView = builder.addVerticeView(ConstantView(DigitalSignalFactory.of(true)))
		builder.connect(constantView, output)

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;

			-- test
			entity main is
			  port (
			    O: out std_logic);
			end main;

			architecture Behavioral of main is
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

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;

			-- test
			entity main is
			  port (
			    O: out std_logic);
			end main;

			architecture Behavioral of main is
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

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;

			-- test
			entity main is
			  port (
			    O1: out std_logic;
			    O2: out std_logic);
			end main;

			architecture Behavioral of main is
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

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;

			-- test
			entity main is
			  port (
			    A: in std_logic;
			    B: in std_logic;
			    O: out std_logic);
			end main;

			architecture Behavioral of main is
			begin
			  O <= (NOT A AND B) after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}
}