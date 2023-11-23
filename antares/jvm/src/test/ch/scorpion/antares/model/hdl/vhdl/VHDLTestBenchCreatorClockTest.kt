package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.hdl.HDLExportTestBenchParams
import ch.scorpion.antares.hdl.HDLModel
import ch.scorpion.antares.hdl.vhdl.VHDLRenaming
import ch.scorpion.antares.hdl.vhdl.VHDLTestBenchCreator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.testcase.Testcase
import ch.scorpion.jabbah.base.io.StringCodePrinter
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLTestBenchCreatorClockTest : AbstractVHDLTest() {

	@Test
	fun shouldCreateClockedTestData() {
		val builder = TestCircuitBuilder("test")
		val input = builder.addInput("I")
		val output = builder.addOutput("O")
		builder.connect(input, output)

		val model = HDLModel(builder.graph as DigitalGraph)
			.create()
			.apply {
				renameLabels(VHDLRenaming())
			}

		val testcase = Testcase("test", """
			I O
			^1 1
		""".trimIndent()
		)

		val params = HDLExportTestBenchParams(
			VHDLRenaming(),
			"clocked_tb",
			testcase,
			Paths.get("notUsed"),
			30)

		val out = StringCodePrinter()
		VHDLTestBenchCreator(out, model, "clocked", params).print()
		out.close()

		assertEquals("""
			-- Test bench for clocked
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;
			
			entity clocked_tb is
			end clocked_tb;
			
			architecture Behavioral of clocked_tb is
			  component main
			    port (
			      I: in std_logic;
			      O: out std_logic);
			  end component;

			  signal I: std_logic;
			  signal O: std_logic;

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
			      I: std_logic;
			      O: std_logic;
			    end record;
			    type test_data_array is array (natural range <>) of test_data_type;
			    constant test_data : test_data_array := (
			      0 => ('0', '-'),
			      1 => ('1', '-'),
			      2 => ('1', '1'));
			  begin
			    for i0 in test_data'range loop
			      I <= test_data(i0).I;
			      wait for 30 ns;
			      assert std_match(O, test_data(i0).O) OR (O = 'Z' AND test_data(i0).O = 'Z')
			        report "assertion failed for O on line " & integer'image(i0) & ", expected " & std_logic'image(test_data(i0).O) & ", actual is " & std_logic'image(O)
			        severity error;
			    end loop;
			  end process;
			end Behavioral;
			
		""".trimIndent(), out.toString())
	}
}