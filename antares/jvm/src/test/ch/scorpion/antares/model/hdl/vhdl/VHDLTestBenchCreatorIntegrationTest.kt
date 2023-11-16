package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.hdl.HDLExportTestBenchParams
import ch.scorpion.antares.hdl.HDLModel
import ch.scorpion.antares.hdl.vhdl.VHDLRenaming
import ch.scorpion.antares.hdl.vhdl.VHDLTestBenchCreator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.standardlibrary.AbstractStandardLibraryBasedCircuitTest
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.io.StringCodePrinter
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.view.GraphView
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLTestBenchCreatorIntegrationTest : AbstractStandardLibraryBasedCircuitTest() {

	override fun createCircuit(): GraphView =
		LibraryModule.libraryHolder
			.getMetaGraph(UUID("52255dc4-c010-4f6f-8ea6-9c2c8f5f9a82"))
			.graph
			.graphView

	/** Test creating a VHDL test bench for the "Half Adder" from the standard library.*/
	@Test
	fun shouldCreateTestBench() {
		val model = HDLModel(getCircuitView().graph as DigitalGraph, LibraryModule.libraryHolder.library)
			.create()
			.apply {
				renameLabels(VHDLRenaming())
			}

		val params = HDLExportTestBenchParams(
			VHDLRenaming(),
			"half_adder_tb",
			(getCircuitView().graph as DigitalGraph).testcases.testcases.first(),
			Paths.get("notUsed"),
			30)

		val out = StringCodePrinter()
		VHDLTestBenchCreator(out, model, "half_adder", params).print()
		out.close()

		assertEquals("""
			-- Test bench for half_adder
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;
			
			entity half_adder_tb is
			end half_adder_tb;
			
			architecture Behavioral of half_adder_tb is
			  component main
			    port (
			      A: in std_logic;
			      B: in std_logic;
			      S: out std_logic;
			      C: out std_logic);
			  end component;

			  signal A: std_logic;
			  signal B: std_logic;
			  signal S: std_logic;
			  signal C: std_logic;
			
			begin
			  main_0 : main port map (
			    A => A,
			    B => B,
			    S => S,
			    C => C);
			  process
			    type test_data_type is record
			      A: std_logic;
			      B: std_logic;
			      S: std_logic;
			      C: std_logic;
			    end record;
			    type test_data_array is array (natural range <>) of test_data_type;
			    constant test_data : test_data_array := (
			      ('0', '0', '0', '0'),
			      ('0', '1', '1', '0'),
			      ('1', '0', '1', '0'),
			      ('1', '1', '0', '1'));
			  begin
			    for i in test_data'range loop
			      A <= test_data(i).A;
			      B <= test_data(i).B;
			      wait for 30 ns;
			      assert std_match(S, test_data(i).S) OR (S = 'Z' AND test_data(i).S = 'Z')
			        report "assertion failed for S on vector " & integer'image(i) & ", expected " & std_logic'image(test_data(i).S) & ", actual is " & std_logic'image(S)
			        severity error;
			      assert std_match(C, test_data(i).C) OR (C = 'Z' AND test_data(i).C = 'Z')
			        report "assertion failed for C on vector " & integer'image(i) & ", expected " & std_logic'image(test_data(i).C) & ", actual is " & std_logic'image(C)
			        severity error;
			    end loop;
			  end process;
			end Behavioral;
			
		""".trimIndent(), out.toString())
	}
}