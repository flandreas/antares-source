package io.antarescircuit.antares.model.hdl.vhdl

import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.hdl.vhdl.VHDLGenerator
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.view.addressable.RAMView
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLRAMSyncTest : AbstractVHDLTest() {

	@Test
	fun testRAM() {
		val builder = TestCircuitBuilder("test")
		val ramView = builder.addVerticeView(RAMView())
		val a = builder.addInput("A", BitWidth.BW_8)
		val clk = builder.addInput("CLK")
		val cs = builder.addInput("CS")
		val wr = builder.addInput("WR")
		val clr = builder.addInput("CLR")
		val d = builder.addOutput("D", BitWidth.BW_8)
		builder.connect(a, ramView, ramView.model.getAddressInput())
		builder.connect(clk, ramView, ramView.model.getClockInput()!!)
		builder.connect(cs, ramView, ramView.model.getChipSelectInput())
		builder.connect(wr, ramView, ramView.model.getWriteInput())
		builder.connect(clr, ramView, ramView.model.getClearInput())
		builder.connect(ramView, ramView.model.getDataPort(), d)

		VHDLGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;
			
			entity VHDL_RAM is
			  generic (
			    addrBitWidth: integer;
			    dataBitWidth: integer);
			  port (
			    A: in std_logic_vector((addrBitWidth - 1) downto 0);
			    CLK: in std_logic;
			    CS: in std_logic;
			    WR: in std_logic;
			    CLR: in std_logic;
			    D: inout std_logic_vector((dataBitWidth - 1) downto 0));
			end VHDL_RAM;
			
			architecture Behavioral of VHDL_RAM is
			  type memoryType is array(0 to (2**addrBitWidth) - 1) of std_logic_vector((dataBitWidth - 1) downto 0);
			  signal memory: memoryType;
			begin
			  process (CLK, CLR)
			  begin
			    if rising_edge(CLR) then
			      memory := (others => 0)
			    elsif rising_edge(CLK) and (CS='1') and (WR='1') then
			      memory(to_integer(unsigned(A))) <= D;
			    end if;
			  end process;
			  D <= memory(to_integer(unsigned(A))) when CS='1' and WR='0' else (others => 'Z');
			end Behavioral;

			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity test is
			  port (
			    A: in std_logic_vector(7 downto 0);
			    CLK: in std_logic;
			    CS: in std_logic;
			    WR: in std_logic;
			    CLR: in std_logic;
			    D: out std_logic_vector(7 downto 0));
			end test;
			
			architecture Behavioral of test is
			begin
			  node0: entity work.VHDL_RAM
			    generic map (
			      addrBitWidth => 8,
			      dataBitWidth => 8)
			    port map (
			      A => A,
			      CS => CS,
			      WR => WR,
			      CLR => CLR,
			      CLK => CLK,
			      D => D);
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}
}