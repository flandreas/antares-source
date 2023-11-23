package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.hdl.vhdl.VHDLGenerator
import ch.scorpion.antares.model.DigitalGraph
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLGeneratorTest : AbstractVHDLTest() {

	@Test
	fun shouldRename() {
		VHDLGenerator(testParams()).generateHDL(
			printer,
			TestCircuitBuilder("test").buildNOP(outputName = "out").graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity main is
			  port (
			    I: in std_logic;
			    p_out: out std_logic);
			end main;

			architecture Behavioral of main is
			begin
			  p_out <= I;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}
}