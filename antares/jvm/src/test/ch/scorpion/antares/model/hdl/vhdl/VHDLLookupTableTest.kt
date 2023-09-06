package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.AbstractJvmCircuitTest
import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.hdl.vhdl.VHDLGenerator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.addressable.LookupTable
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.addressable.LookupTableView
import ch.scorpion.jabbah.base.io.StringCodePrinter
import ch.scorpion.jabbah.graph.library.LibraryModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLLookupTableTest {

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
	fun testLUT() {
		val builder = TestCircuitBuilder("test")
		val a = builder.addInput("A", BitWidth.BW_4)
		val d = builder.addOutput("D", BitWidth.BW_8)
		val lutView = builder.addVerticeView(LookupTableView(model = LookupTable(BitWidth.BW_4, BitWidth.BW_8)))
		lutView.model.name = "ABC"
		lutView.model.setDataAt(0, 1UL, null)
		lutView.model.setDataAt(1, 2UL, null)
		lutView.model.setDataAt(2, 3UL, null)
		lutView.model.setDataAt(3, 4UL, null)
		builder.connect(a, lutView)
		builder.connect(lutView, d)

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;
			
			entity VHDL_LookupTable_ABC is
			  port (
			    A: in std_logic_vector(3 downto 0);
			    D: out std_logic_vector(7 downto 0));
			end VHDL_LookupTable_ABC;

			architecture Behavioral of VHDL_LookupTable_ABC is
			  type memory is array (0 to 3) of std_logic_vector(7 downto 0);
			  constant lut : memory := (
			    "00000001",
			    "00000010",
			    "00000011",
			    "00000100");
			begin
			  process (A)
			  begin
			    if A >= "0100" then
			      D <= (others => '0');
			    else
			      D <= lut(to_integer(unsigned(A)));
			    end if;
			  end process;
			end Behavioral;

			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity main is
			  port (
			    A: in std_logic_vector(3 downto 0);
			    D: out std_logic_vector(7 downto 0));
			end main;
			
			architecture Behavioral of main is
			begin
			  node0: entity work.VHDL_LookupTable_ABC
			    port map (
			      A => A,
			      D => D);
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}
}