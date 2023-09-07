package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.AbstractJvmCircuitTest
import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.hdl.vhdl.VHDLGenerator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.addressable.RAMView
import ch.scorpion.jabbah.base.io.StringCodePrinter
import ch.scorpion.jabbah.graph.library.LibraryModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLRAMSyncTest {

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

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

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
			entity main is
			  port (
			    A: in std_logic_vector(7 downto 0);
			    CLK: in std_logic;
			    CS: in std_logic;
			    WR: in std_logic;
			    CLR: in std_logic;
			    D: out std_logic_vector(7 downto 0));
			end main;
			
			architecture Behavioral of main is
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