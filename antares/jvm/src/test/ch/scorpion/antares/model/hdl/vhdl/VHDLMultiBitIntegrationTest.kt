package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.hdl.vhdl.VHDLGenerator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.gate.AbstractLogicGate
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.net.Concentrator
import ch.scorpion.antares.model.net.Splitter
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.net.ConcentratorView
import ch.scorpion.antares.view.net.SplitterView
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLMultiBitIntegrationTest : AbstractVHDLTest() {

	@Test
	fun testNoOp() {
		VHDLGenerator(library, printer).generate(
			TestCircuitBuilder("test").buildNOP(bitWidth = BitWidth.BW_4).graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity main is
			  port (
			    I: in std_logic_vector(3 downto 0);
			    O: out std_logic_vector(3 downto 0));
			end main;

			architecture Behavioral of main is
			begin
			  O <= I;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testCustomNand() {
		VHDLGenerator(library, printer).generate(
			TestCircuitBuilder("test").buildCustomNAND(null, BitWidth.BW_4).graph as DigitalGraph)

		// The result is not yet optimized (not yet implemented). The optimized result would be
		// O1 <= NOT (I1 AND I2), without the intermediate signal s0.
		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity main is
			  port (
			    I1: in std_logic_vector(3 downto 0);
			    I2: in std_logic_vector(3 downto 0);
			    O1: out std_logic_vector(3 downto 0));
			end main;

			architecture Behavioral of main is
			  signal s0: std_logic_vector(3 downto 0);
			begin
			  O1 <= NOT s0 after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns;
			  s0 <= (I1 AND I2) after ${AbstractLogicGate.DEFAULT_PROPAGATION_DELAY} ns;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testConcentrator() {
		val builder = TestCircuitBuilder("test")
		val input1 = builder.addInput("I0")
		val input2 = builder.addInput("I1")
		val output = builder.addOutput("O", bitWidth = BitWidth.BW_2)
		val concentrator = builder.addVerticeView(ConcentratorView(model = Concentrator(BitWidth.BW_2, BranchCount.BC_2)))
		builder.connect(input1, concentrator, concentrator.model.getInput(2))
		builder.connect(input2, concentrator, concentrator.model.getInput(3))
		builder.connect(concentrator, concentrator.model.getOutput(1), output)

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;
			
			-- test
			entity main is
			  port (
			    I0: in std_logic;
			    I1: in std_logic;
			    O: out std_logic_vector(1 downto 0));
			end main;
			
			architecture Behavioral of main is
			begin
			  O(0) <= I0;
			  O(1) <= I1;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testWideConcentrator() {
		val builder = TestCircuitBuilder("test")
		val input1 = builder.addInput("I0", BitWidth.BW_2)
		val input2 = builder.addInput("I1", BitWidth.BW_2)
		val output = builder.addOutput("O", BitWidth.BW_4)
		val concentrator = builder.addVerticeView(ConcentratorView(model = Concentrator(BitWidth.BW_4, BranchCount.BC_2)))
		builder.connect(input1, concentrator, concentrator.model.getInput(2))
		builder.connect(input2, concentrator, concentrator.model.getInput(3))
		builder.connect(concentrator, concentrator.model.getOutput(1), output)

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;
			
			-- test
			entity main is
			  port (
			    I0: in std_logic_vector(1 downto 0);
			    I1: in std_logic_vector(1 downto 0);
			    O: out std_logic_vector(3 downto 0));
			end main;
			
			architecture Behavioral of main is
			begin
			  O(1 downto 0) <= I0;
			  O(3 downto 2) <= I1;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testSplitter() {
		val builder = TestCircuitBuilder("test")
		val input = builder.addInput("I", BitWidth.BW_2)
		val output1 = builder.addOutput("O1", BitWidth.BW_1)
		val output2 = builder.addOutput("O2", BitWidth.BW_1)
		val splitter = builder.addVerticeView(SplitterView(model = Splitter(BitWidth.BW_2, BranchCount.BC_2)))
		builder.connect(input, splitter)
		builder.connect(splitter, splitter.model.getOutput(2), output1)
		builder.connect(splitter, splitter.model.getOutput(3), output2)

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;
			
			-- test
			entity main is
			  port (
			    I: in std_logic_vector(1 downto 0);
			    O1: out std_logic;
			    O2: out std_logic);
			end main;
			
			architecture Behavioral of main is
			begin
			  O1 <= I(0);
			  O2 <= I(1);
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testWideSplitter() {
		val builder = TestCircuitBuilder("test")
		val input = builder.addInput("I", BitWidth.BW_4)
		val output1 = builder.addOutput("O1", BitWidth.BW_2)
		val output2 = builder.addOutput("O2", BitWidth.BW_2)
		val splitter = builder.addVerticeView(SplitterView(model = Splitter(BitWidth.BW_4, BranchCount.BC_2)))
		builder.connect(input, splitter)
		builder.connect(splitter, splitter.model.getOutput(2), output1)
		builder.connect(splitter, splitter.model.getOutput(3), output2)

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;
			
			-- test
			entity main is
			  port (
			    I: in std_logic_vector(3 downto 0);
			    O1: out std_logic_vector(1 downto 0);
			    O2: out std_logic_vector(1 downto 0));
			end main;
			
			architecture Behavioral of main is
			begin
			  O1 <= I(1 downto 0);
			  O2 <= I(3 downto 2);
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}
}