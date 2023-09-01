package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.AbstractJvmCircuitTest
import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.hdl.vhdl.VHDLGenerator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.base.io.StringCodePrinter
import ch.scorpion.jabbah.graph.library.LibraryModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLGeneratorTest {

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
	fun shouldRename() {
		VHDLGenerator(library, printer).generate(
			TestCircuitBuilder("test").buildNOP(outputName = "out").graph as DigitalGraph
		)

		assertEquals("""
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;

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