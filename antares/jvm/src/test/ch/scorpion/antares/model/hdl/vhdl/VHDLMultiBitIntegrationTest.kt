package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.AbstractJvmCircuitTest
import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.hdl.HDLModel
import ch.scorpion.antares.hdl.vhdl.VHDLCreator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.io.StringCodePrinter
import ch.scorpion.jabbah.graph.library.LibraryModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLMultiBitIntegrationTest {

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
	fun testNoOp() {
		val model = HDLModel(
			TestCircuitBuilder("test").buildNOP(bitWidth = BitWidth.BW_4).graph as DigitalGraph,
			library
		).create()

		VHDLCreator(printer).printCircuit(model.main)

		assertEquals("""
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;

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
		val model = HDLModel(
			TestCircuitBuilder("test").buildCustomNAND(null, BitWidth.BW_4).graph as DigitalGraph,
			library
		).create()

		VHDLCreator(printer).printCircuit(model.main)

		// The result is not yet optimized (not yet implemented). The optimized result would be
		// O1 <= NOT (I1 AND I2), without the intermediate signal s0.
		assertEquals("""
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;

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
			  O1 <= NOT s0;
			  s0 <= (I1 AND I2);
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}
}