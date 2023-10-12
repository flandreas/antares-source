package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.hdl.vhdl.VHDLGenerator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.net.Tunnel
import ch.scorpion.antares.view.net.TunnelView
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLTunnelTest : AbstractVHDLTest() {

	@Test
	fun testTunnel() {
		val builder = TestCircuitBuilder("test")
		val a = builder.addInput("A")
		val b = builder.addOutput("B")
		val tunnelView1 = builder.addVerticeView(TunnelView(model = Tunnel("T")))
		val tunnelView2 = builder.addVerticeView(TunnelView(model = Tunnel("T")))
		builder.connect(a, tunnelView1)
		builder.connect(tunnelView2, b)

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity main is
			  port (
			    A: in std_logic;
			    B: out std_logic);
			end main;
			
			architecture Behavioral of main is
			begin
			  B <= A;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}

	@Test
	fun testCascadedTunnel() {
		val builder = TestCircuitBuilder("test")
		val a = builder.addInput("A")
		val b = builder.addOutput("B")
		val tunnelView11 = builder.addVerticeView(TunnelView(model = Tunnel("T1")))
		val tunnelView12 = builder.addVerticeView(TunnelView(model = Tunnel("T1")))
		val tunnelView21 = builder.addVerticeView(TunnelView(model = Tunnel("T2")))
		val tunnelView22 = builder.addVerticeView(TunnelView(model = Tunnel("T2")))
		builder.connect(a, tunnelView11)
		builder.connect(tunnelView12, tunnelView21)
		builder.connect(tunnelView22, b)

		VHDLGenerator(library, printer).generate(builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity main is
			  port (
			    A: in std_logic;
			    B: out std_logic);
			end main;
			
			architecture Behavioral of main is
			begin
			  B <= A;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}
}