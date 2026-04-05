package io.antarescircuit.antares.model.hdl.vhdl

import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.hdl.vhdl.VHDLGenerator
import io.antarescircuit.antares.model.DigitalGraph
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
			entity test is
			  port (
			    I: in std_logic;
			    p_out: out std_logic);
			end test;

			architecture Behavioral of test is
			begin
			  p_out <= I;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}
}