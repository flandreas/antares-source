package io.antarescircuit.antares.model.hdl.vhdl

import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.hdl.vhdl.VHDLGenerator
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.view.addressable.ROMView
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLROMTest : AbstractVHDLTest() {

	@Test
	fun testROM() {
		val builder = TestCircuitBuilder("test")
		val a = builder.addInput("A", BitWidth.BW_8)
		val cs = builder.addInput("CS")
		val d = builder.addOutput("D", BitWidth.BW_8)
		val romView = builder.addVerticeView(ROMView())
		romView.model.setDataAt(0, 11UL, null)
		romView.model.setDataAt(1, 22UL, null)
		romView.text = TranslatableText("ABC")
		builder.connect(a, romView, romView.model.getAddressInput())
		builder.connect(cs, romView, romView.model.getChipSelectInput())
		builder.connect(romView, romView.model.getDataPort(), d)

		VHDLGenerator(testParams())
			.generateHDL(printer, builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;
			
			entity VHDL_ROM_ABC is
			  port (
			    D: out std_logic_vector(7 downto 0);
			    A: in std_logic_vector(7 downto 0);
			    CS: in std_logic);
			end VHDL_ROM_ABC;
			
			architecture Behavioral of VHDL_ROM_ABC is
			  type memory is array (0 to 1) of std_logic_vector(7 downto 0);
			  constant rom : memory := (
			    "00001011",
			    "00010110");
			begin
			  process (A, CS)
			  begin
			    if CS='0' then
			      D <= (others => 'Z');
			    elsif A >= "00000010" then
			      D <= (others => '0');
			    else
			      D <= rom(to_integer(unsigned(A)));
			    end if;
			  end process;
			end Behavioral;

			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity test is
			  port (
			    A: in std_logic_vector(7 downto 0);
			    CS: in std_logic;
			    D: out std_logic_vector(7 downto 0));
			end test;
			
			architecture Behavioral of test is
			begin
			  node0: entity work.VHDL_ROM_ABC
			    port map (
			      A => A,
			      CS => CS,
			      D => D);
			end Behavioral;

		""".trimIndent(), printer.toString())
	}
}