package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.AbstractJvmCircuitTest
import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.TestLibraryBuilder
import ch.scorpion.antares.hdl.HDLModel
import ch.scorpion.jabbah.base.io.StringCodePrinter
import ch.scorpion.antares.hdl.vhdl.VHDLCreator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLIntegrationTest {

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
			TestCircuitBuilder("test").buildNOP().graph as DigitalGraph,
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
			    I: in std_logic;
			    O: out std_logic);
			end main;

			architecture Behavioral of main is
			begin
			  O <= I;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testSubCircuits() {
		val nop = TestLibraryBuilder().addNOP(library)
		val builder = TestCircuitBuilder("test")
		val input = builder.addInput("A")
		val output = builder.addOutput("B")
		val subGraphVV1 = builder.addVerticeView((library.get(TestLibraryBuilder.NOP) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<out SubGraphVertice>)
		val subGraphVV2 = builder.addVerticeView((library.get(TestLibraryBuilder.NOP) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<out SubGraphVertice>)
		builder.connect(input, subGraphVV1, subGraphVV1.model.getInput())
		builder.connect(subGraphVV1, subGraphVV1.model.getOutput(), subGraphVV2, subGraphVV2.model.getInput())
		builder.connect(subGraphVV2, subGraphVV2.model.getOutput(), output)
		val model = HDLModel(builder.graph as DigitalGraph, library).create()

		VHDLCreator(printer).printCircuit(model.main)

		assertEquals("""
			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;

			-- NOP
			entity ${nop.uuid} is
			  port (
			    I: in std_logic;
			    O: out std_logic);
			end ${nop.uuid};

			architecture Behavioral of ${nop.uuid} is
			begin
			  O <= I;
			end Behavioral;

			LIBRARY ieee;
			USE ieee.std_logic_1164.all;
			USE ieee.numeric_std.all;
			
			-- test
			entity main is
			  port (
			    A: in std_logic;
			    B: out std_logic);
			end main;
			
			architecture Behavioral of main is
			  signal s0: std_logic;
			begin
			  node0: entity work.${nop.uuid}
			    port map (
			      I => A,
			      O => s0);
			  node1: entity work.${nop.uuid}
			    port map (
			      I => s0,
			      O => B);
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}
}