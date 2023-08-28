package ch.scorpion.antares.filebased.vhdl

import ch.scorpion.antares.filebased.AbstractFileBasedTest
import ch.scorpion.antares.hdl.HDLModel
import ch.scorpion.antares.hdl.vhdl.VHDLCreator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.io.StringCodePrinter
import ch.scorpion.jabbah.graph.library.LibraryModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Emulates a bus driver according to https://github.com/hneemann/Digital/issues/394,
 * but with only using an output instead of an inout due to HDL limitations.
 * Also tests a wire with two connected outputs, requiring a separate signal.
 */
class PseudoBusDriverTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
		}
	}

	private val printer = StringCodePrinter()

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("ee6b64f0-7189-4604-b6a9-e0d063336787"))
	}

	@Test
	fun test() {
		val model = HDLModel(
			openedCircuitView.graph as DigitalGraph,
			LibraryModule.libraryHolder.library
		).create()

		VHDLCreator(printer).printCircuit(model.main)

		assertEquals("""
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			
			entity VHDL_TriStateBufferGate is
			  port (
			    p1: in std_logic;
			    EN: in std_logic;
			    p3: out std_logic);
			end VHDL_TriStateBufferGate;
			
			architecture Behavioral of VHDL_TriStateBufferGate is
			begin
			  p3 <= p1 when EN = '1' else 'Z';
			end Behavioral;
			
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;
			
			-- NodeView
			entity main is
			  port (
			    WR: in std_logic;
			    OE: in std_logic;
			    PIN: out std_logic;
			    RD: out std_logic);
			end main;
			
			architecture Behavioral of main is
			  signal RD_net: std_logic;
			begin
			  node0: entity work.VHDL_TriStateBufferGate
			    port map (
			      p1 => WR,
			      EN => OE,
			      p3 => RD_net);
			  PIN <= RD_net;
			  RD <= RD_net;
			end Behavioral;

		""".trimIndent(), printer.toString())
	}
}