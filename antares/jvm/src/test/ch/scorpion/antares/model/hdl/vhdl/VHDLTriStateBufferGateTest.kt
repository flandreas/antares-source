package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.AbstractJvmCircuitTest
import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.hdl.vhdl.VHDLGenerator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.gate.TriStateBufferGate
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.gate.TriStateBufferGateView
import ch.scorpion.jabbah.base.io.StringCodePrinter
import ch.scorpion.jabbah.graph.library.LibraryModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLTriStateBufferGateTest {

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
	fun testSingleBit() {
		val builder = TestCircuitBuilder("test")
		val input = builder.addInput("I")
		val enable = builder.addInput("EN")
		val output = builder.addOutput("O")
		val gate = builder.addVerticeView(TriStateBufferGateView())
		builder.connect(input, gate, gate.model.getInputPort())
		builder.connect(enable, gate, gate.model.getEnablePort())
		builder.connect(gate, gate.model.getOutputPort(), output)

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			
			entity VHDL_TriStateBufferGate is
			  port (
			    p1: in std_logic;
			    EN: in std_logic;
			    p3: out std_logic);
			end VHDL_TriStateBufferGate;
			
			architecture Behavioral of VHDL_TriStateBufferGate is
			begin
			  p3 <= p1 when EN = '1' else 'Z';
			end Behavioral;
			
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;
			
			-- test
			entity main is
			  port (
			    I: in std_logic;
			    EN: in std_logic;
			    O: out std_logic);
			end main;
			
			architecture Behavioral of main is
			begin
			  node0: entity work.VHDL_TriStateBufferGate
			    port map (
			      p1 => I,
			      EN => EN,
			      p3 => O);
			end Behavioral;

		""".trimIndent(), printer.toString())
	}

	@Test
	fun testSingleBitNegated() {
		val builder = TestCircuitBuilder("test")
		val input = builder.addInput("I")
		val enable = builder.addInput("EN")
		val output = builder.addOutput("O")
		val gate = builder.addVerticeView(TriStateBufferGateView(model = TriStateBufferGate(enableLogic = Logic.NEGATIVE)))
		builder.connect(input, gate, gate.model.getInputPort())
		builder.connect(enable, gate, gate.model.getEnablePort())
		builder.connect(gate, gate.model.getOutputPort(), output)

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			
			entity VHDL_TriStateBufferGate is
			  port (
			    p1: in std_logic;
			    EN: in std_logic;
			    p3: out std_logic);
			end VHDL_TriStateBufferGate;
			
			architecture Behavioral of VHDL_TriStateBufferGate is
			begin
			  p3 <= p1 when EN = '0' else 'Z';
			end Behavioral;
			
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;
			
			-- test
			entity main is
			  port (
			    I: in std_logic;
			    EN: in std_logic;
			    O: out std_logic);
			end main;
			
			architecture Behavioral of main is
			begin
			  node0: entity work.VHDL_TriStateBufferGate
			    port map (
			      p1 => I,
			      EN => EN,
			      p3 => O);
			end Behavioral;

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

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;

			entity VHDL_TriStateBufferGate_MultiBit is
			  generic (bitWidth : integer);
			  port (
			    p1: in std_logic_vector((bitWidth - 1) downto 0);
			    EN: in std_logic;
			    p3: out std_logic_vector((bitWidth - 1) downto 0));
			end VHDL_TriStateBufferGate_MultiBit;
			
			architecture Behavioral of VHDL_TriStateBufferGate_MultiBit is
			begin
			  p3 <= p1 when EN = '1' else (others => 'Z');
			end Behavioral;
			
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;
			
			-- test
			entity main is
			  port (
			    I: in std_logic_vector(3 downto 0);
			    EN: in std_logic;
			    O: out std_logic_vector(3 downto 0));
			end main;
			
			architecture Behavioral of main is
			begin
			  node0: entity work.VHDL_TriStateBufferGate_MultiBit
			    generic map (
			      bitWidth => 4)
			    port map (
			      p1 => I,
			      EN => EN,
			      p3 => O);
			end Behavioral;

		""".trimIndent(), printer.toString())
	}

	@Test
	fun shouldDistinguishBitWidth() {
		val builder = TestCircuitBuilder("test")

		val input = builder.addInput("I")
		val enable = builder.addInput("EN")
		val output = builder.addOutput("O")
		val gate = builder.addVerticeView(TriStateBufferGateView())
		builder.connect(input, gate, gate.model.getInputPort())
		builder.connect(enable, gate, gate.model.getEnablePort())
		builder.connect(gate, gate.model.getOutputPort(), output)

		val input2 = builder.addInput("I2", BitWidth.BW_4)
		val enable2 = builder.addInput("EN2")
		val output2 = builder.addOutput("O2", BitWidth.BW_4)
		val gate2 = builder.addVerticeView(TriStateBufferGateView(model = TriStateBufferGate(BitWidth.BW_4)))
		builder.connect(input2, gate2, gate2.model.getInputPort())
		builder.connect(enable2, gate2, gate2.model.getEnablePort())
		builder.connect(gate2, gate2.model.getOutputPort(), output2)

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;

			entity VHDL_TriStateBufferGate is
			  port (
			    p1: in std_logic;
			    EN: in std_logic;
			    p3: out std_logic);
			end VHDL_TriStateBufferGate;
			
			architecture Behavioral of VHDL_TriStateBufferGate is
			begin
			  p3 <= p1 when EN = '1' else 'Z';
			end Behavioral;

			library ieee;
			use ieee.std_logic_1164.all;

			entity VHDL_TriStateBufferGate_MultiBit is
			  generic (bitWidth : integer);
			  port (
			    p1: in std_logic_vector((bitWidth - 1) downto 0);
			    EN: in std_logic;
			    p3: out std_logic_vector((bitWidth - 1) downto 0));
			end VHDL_TriStateBufferGate_MultiBit;
			
			architecture Behavioral of VHDL_TriStateBufferGate_MultiBit is
			begin
			  p3 <= p1 when EN = '1' else (others => 'Z');
			end Behavioral;
			
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;
			
			-- test
			entity main is
			  port (
			    I: in std_logic;
			    EN: in std_logic;
			    I2: in std_logic_vector(3 downto 0);
			    EN2: in std_logic;
			    O: out std_logic;
			    O2: out std_logic_vector(3 downto 0));
			end main;
			
			architecture Behavioral of main is
			begin
			  node0: entity work.VHDL_TriStateBufferGate
			    port map (
			      p1 => I,
			      EN => EN,
			      p3 => O);
			  node1: entity work.VHDL_TriStateBufferGate_MultiBit
			    generic map (
			      bitWidth => 4)
			    port map (
			      p1 => I2,
			      EN => EN2,
			      p3 => O2);
			end Behavioral;

		""".trimIndent(), printer.toString())
	}
}