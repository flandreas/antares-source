package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.hdl.HDLExportTestBenchParams
import ch.scorpion.antares.hdl.HDLModel
import ch.scorpion.antares.hdl.vhdl.VHDLRenaming
import ch.scorpion.antares.hdl.vhdl.VHDLTestBenchCreator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.testcase.Testcase
import ch.scorpion.jabbah.base.io.StringCodePrinter
import ch.scorpion.jabbah.graph.library.LibraryModule
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLTestBenchCreatorMultiBitTest : AbstractVHDLTest() {

	@Test
	fun shouldCreateMultiBitTestData() {
		val builder = TestCircuitBuilder("test")
		val input = builder.addInput("I", BitWidth.BW_8)
		val output = builder.addOutput("O", BitWidth.BW_8)
		builder.connect(input, output)

		val model = HDLModel(builder.graph as DigitalGraph, LibraryModule.libraryHolder.library)
			.create()
			.apply {
				renameLabels(VHDLRenaming())
			}

		val testcase = Testcase("test", """
			I O
			0 0
			255 255
		""".trimIndent())

		val params = HDLExportTestBenchParams(
			VHDLRenaming(),
			"multi_bit_tb",
			testcase,
			Paths.get("notUsed"),
			30)

		val out = StringCodePrinter()
		VHDLTestBenchCreator(out, model, "multi_bit", params).print()
		out.close()

		assertEquals("""
			-- Test bench for multi_bit
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;
			
			entity multi_bit_tb is
			end multi_bit_tb;
			
			architecture Behavioral of multi_bit_tb is
			  component main
			    port (
			      I: in std_logic_vector(7 downto 0);
			      O: out std_logic_vector(7 downto 0));
			  end component;

			  signal I: std_logic_vector(7 downto 0);
			  signal O: std_logic_vector(7 downto 0);
			
			  function to_string(v: std_logic_vector) return string is
			    variable s : string (1 to v'length) := (others => NUL);
			    variable si : integer := 1; 
			  begin
			    for i in v'range loop
			      s(si) := std_logic'image(v((i)))(2);
			      si := si + 1;
			    end loop;
			    return s;
			  end function;			
			
			begin
			  main_0 : main port map (
			    I => I,
			    O => O);
			  process
			    type test_data_type is record
			      I: std_logic_vector(7 downto 0);
			      O: std_logic_vector(7 downto 0);
			    end record;
			    type test_data_array is array (natural range <>) of test_data_type;
			    constant test_data : test_data_array := (
			      0 => ("00000000", "00000000"),
			      1 => ("11111111", "11111111"));
			  begin
			    for i0 in test_data'range loop
			      I <= test_data(i0).I;
			      wait for 30 ns;
			      assert std_match(O, test_data(i0).O) OR (O = "ZZZZZZZZ" AND test_data(i0).O = "ZZZZZZZZ")
			        report "assertion failed for O on line " & integer'image(i0) & ", expected " & to_string(test_data(i0).O) & ", actual is " & to_string(O)
			        severity error;
			    end loop;
			  end process;
			end Behavioral;
			
		""".trimIndent(), out.toString())
	}
}