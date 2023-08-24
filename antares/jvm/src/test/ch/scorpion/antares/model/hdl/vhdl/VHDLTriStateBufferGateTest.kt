package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.AbstractJvmCircuitTest
import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.hdl.HDLModel
import ch.scorpion.antares.hdl.vhdl.VHDLCreator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.view.gate.TriStateBufferGateView
import ch.scorpion.jabbah.base.io.StringCodePrinter
import ch.scorpion.jabbah.graph.library.LibraryModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLTriStateBufferGateTest {

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
	fun test() {
		val builder = TestCircuitBuilder("test")
		val input = builder.addInput("I")
		val enable = builder.addInput("EN")
		val output = builder.addOutput("O")
		val gate = builder.addVerticeView(TriStateBufferGateView())
		builder.connect(input, gate, gate.model.getInputPort())
		builder.connect(enable, gate, gate.model.getEnablePort())
		builder.connect(gate, gate.model.getOutputPort(), output)

		val model = HDLModel(
			builder.graph as DigitalGraph,
			library
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
			end DRIVER_INV_GATE;
			
			architecture Behavioral of VHDL_TriStateBufferGate is
			begin
			  p3 <= p1 when EN = '1' else 'Z';
			end Behavioral;
			
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;
			
			-- test
			entity main is
			  port (
			    I: in std_logic;
			    EN: in std_logic;
			    O: out std_logic);
			end main;
			
			architecture Behavioral of main is
			begin
			  node0: entity work.VHDL_TriStateBufferGate
			    port map (
			      p1 => I,
			      EN => EN,
			      p3 => O);
			end Behavioral;

		""".trimIndent(), printer.toString())
	}
}