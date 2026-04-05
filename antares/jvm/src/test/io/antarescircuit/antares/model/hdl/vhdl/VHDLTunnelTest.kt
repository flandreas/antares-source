package io.antarescircuit.antares.model.hdl.vhdl

import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.hdl.vhdl.VHDLGenerator
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.net.Tunnel
import io.antarescircuit.antares.view.net.tunnel.TunnelView
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

		VHDLGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity test is
			  port (
			    A: in std_logic;
			    B: out std_logic);
			end test;
			
			architecture Behavioral of test is
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

		VHDLGenerator(testParams()).generateHDL(printer, builder.graph as DigitalGraph)

		assertEquals("""
			library ieee;
			use ieee.std_logic_1164.all;
			use ieee.numeric_std.all;

			-- test
			entity test is
			  port (
			    A: in std_logic;
			    B: out std_logic);
			end test;
			
			architecture Behavioral of test is
			begin
			  B <= A;
			end Behavioral;
			
		""".trimIndent(), printer.toString())
	}
}