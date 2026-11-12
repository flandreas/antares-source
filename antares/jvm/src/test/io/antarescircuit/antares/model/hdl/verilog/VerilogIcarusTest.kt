package io.antarescircuit.antares.model.hdl.verilog

import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.TestLibraryBuilder
import io.antarescircuit.antares.hdl.HDLExportParams
import io.antarescircuit.antares.hdl.verilog.VerilogGenerator
import io.antarescircuit.antares.hdl.verilog.VerilogRenaming
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.addressable.LookupTable
import io.antarescircuit.antares.model.addressable.RAM
import io.antarescircuit.antares.model.gate.TriStateBufferGate
import io.antarescircuit.antares.model.net.BranchCount
import io.antarescircuit.antares.model.net.Concentrator
import io.antarescircuit.antares.model.net.Splitter
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.view.addressable.LookupTableView
import io.antarescircuit.antares.view.addressable.RAMView
import io.antarescircuit.antares.view.addressable.ROMView
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.gate.TriStateBufferGateView
import io.antarescircuit.antares.view.net.ConcentratorView
import io.antarescircuit.antares.view.net.SplitterView
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVertice
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import org.junit.Assume.assumeTrue
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Compiles generated Verilog with Icarus Verilog (`iverilog`) in IEEE 1364-2005 mode.
 * Skipped if `iverilog` is not installed, as it is not part of the required tooling.
 */
class VerilogIcarusTest : AbstractVerilogTest() {

	companion object {
		private val isIcarusAvailable: Boolean by lazy {
			try {
				ProcessBuilder("iverilog", "-V").redirectErrorStream(true).start().apply {
					inputStream.readAllBytes()
				}.waitFor() == 0
			} catch (_: IOException) {
				false
			}
		}
	}

	private lateinit var dir: Path

	@BeforeTest
	fun checkIcarus() {
		assumeTrue("iverilog not installed", isIcarusAvailable)
		dir = Files.createTempDirectory("antares-verilog")
	}

	@AfterTest
	@OptIn(kotlin.io.path.ExperimentalPathApi::class)
	fun cleanUp() {
		if (::dir.isInitialized) {
			dir.deleteRecursively()
		}
	}

	@Test
	fun shouldCompileGatesAndSubCircuits() {
		TestLibraryBuilder().addNOP(library)
		val builder = TestCircuitBuilder("gates")
		val a = builder.addInput("A", BitWidth.BW_4)
		val b = builder.addInput("B", BitWidth.BW_4)
		val o = builder.addOutput("O", BitWidth.BW_4)
		val gate = builder.addVerticeView(LogicGateView.xnorGateView())
		gate.model.setNegateInput(1, true)
		builder.connect(a, gate, gate.model.getInput(1))
		builder.connect(b, gate, gate.model.getInput(2))
		builder.connect(gate, o)

		val i = builder.addInput("I")
		val j = builder.addOutput("J")
		val nop = builder.addVerticeView((library.get(TestLibraryBuilder.NOP) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<out SubGraphVertice>)
		builder.connect(i, nop, nop.model.getInput())
		builder.connect(nop, nop.model.getOutput(), j)

		assertCompiles(builder.graph as DigitalGraph)
	}

	@Test
	fun shouldCompileSplitterAndConcentrator() {
		val builder = TestCircuitBuilder("bus")
		val input = builder.addInput("I", BitWidth.BW_4)
		val output = builder.addOutput("O", BitWidth.BW_4)
		val splitter = builder.addVerticeView(SplitterView(model = Splitter(BitWidth.BW_4, BranchCount.BC_2)))
		val concentrator = builder.addVerticeView(ConcentratorView(model = Concentrator(BitWidth.BW_4, BranchCount.BC_2)))
		builder.connect(input, splitter)
		// Swap both halves
		builder.connect(splitter, splitter.model.getOutput(2), concentrator, concentrator.model.getInput(3))
		builder.connect(splitter, splitter.model.getOutput(3), concentrator, concentrator.model.getInput(2))
		builder.connect(concentrator, concentrator.model.getOutput(1), output)

		assertCompiles(builder.graph as DigitalGraph)
	}

	@Test
	fun shouldCompileMemoriesAndTriStateBuffers() {
		val builder = TestCircuitBuilder("memories")
		val ram = RAM().apply { addressWidth = BitWidth.BW_4 }
		val ramView = builder.addVerticeView(RAMView(model = ram))
		builder.connect(builder.addInput("A", BitWidth.BW_4), ramView, ram.getAddressInput())
		builder.connect(builder.addInput("CLK"), ramView, ram.getClockInput()!!)
		builder.connect(builder.addInput("CS"), ramView, ram.getChipSelectInput())
		builder.connect(builder.addInput("WR"), ramView, ram.getWriteInput())
		builder.connect(builder.addInput("CLR"), ramView, ram.getClearInput())
		builder.connect(ramView, ram.getDataPort(), builder.addOutput("D", BitWidth.BW_8))

		val romView = builder.addVerticeView(ROMView())
		romView.model.setDataAt(1, 22UL, null)
		romView.text = TranslatableText("Rom 1")
		builder.connect(builder.addInput("RA", BitWidth.BW_8), romView, romView.model.getAddressInput())
		builder.connect(builder.addInput("RCS"), romView, romView.model.getChipSelectInput())
		builder.connect(romView, romView.model.getDataPort(), builder.addOutput("RD", BitWidth.BW_8))

		val lutView = builder.addVerticeView(LookupTableView(model = LookupTable(BitWidth.BW_2, BitWidth.BW_1)))
		lutView.model.name = "lut"
		lutView.model.setDataAt(3, 1UL, null)
		builder.connect(builder.addInput("LA", BitWidth.BW_2), lutView)
		builder.connect(lutView, builder.addOutput("LD"))

		val tristate = builder.addVerticeView(TriStateBufferGateView(model = TriStateBufferGate(BitWidth.BW_4)))
		builder.connect(builder.addInput("TI", BitWidth.BW_4), tristate, tristate.model.getInputPort())
		builder.connect(builder.addInput("TEN"), tristate, tristate.model.getEnablePort())
		builder.connect(tristate, tristate.model.getOutputPort(), builder.addOutput("TO", BitWidth.BW_4))

		assertCompiles(builder.graph as DigitalGraph)
	}

	private fun assertCompiles(graph: DigitalGraph) {
		val file = dir.resolve("circuit.v")
		VerilogGenerator(HDLExportParams(VerilogRenaming(), "circuit", true, file, null)).generate(graph)
		val (exitCode, output) = run("iverilog", "-g2005", "-Wall", "-o", dir.resolve("circuit.vvp").toString(), file.toString())
		assertEquals(0, exitCode, "iverilog failed:\n$output\n${Files.readString(file)}")
	}

	private fun run(vararg command: String): Pair<Int, String> {
		val process = ProcessBuilder(*command).redirectErrorStream(true).start()
		val output = process.inputStream.readAllBytes().decodeToString()
		return process.waitFor() to output
	}
}
