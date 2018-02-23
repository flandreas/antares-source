package ch.scorpion.antares

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.antares.view.gate.*
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.input.ClockView
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.memory.RAMView
import ch.scorpion.antares.view.memory.ROMView
import ch.scorpion.antares.view.net.*
import ch.scorpion.antares.view.output.LEDMatrixView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.antares.view.output.SevenSegmentDisplayView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCreator
import java.nio.file.FileSystems
import java.nio.file.Path

interface Antares : DesktopApplication {

	companion object {
		private val DISPLAY_NAME = "Antares"
		private val SYSTEM_NAME = "antares"
		private val FILE_EXTENSION_NAME = "cir"
		private val DEFAULT_LIB_DIRECTORY = "library"
		private val DEFAULT_LIB_FILENAME = "library.lib"
	}

	/** ---- [DesktopApplication] */

	override val displayName: String get() = DISPLAY_NAME

	override val systemName: String get() = SYSTEM_NAME

	override val fileExtension: String get() = FILE_EXTENSION_NAME

	/** ---- [Antares] */

	val libraryDirectoryPath: Path get() = FileSystems.getDefault().getPath(homeDirectoryPath.toString(), DEFAULT_LIB_DIRECTORY)

	val libraryFileName: String get() = DEFAULT_LIB_FILENAME

}

fun fillStandardLibrary(library: Library, storableCreator: StorableCreator) {
	val net = library.ensureFolder(Translations.getString("library.folder.net"))
	net.addBaseElement("Constant", "library.element.Constant", "/img/constant.png", storableCreator, ConstantView::class)
	net.addBaseElement("Splitter", "library.element.Splitter", "/img/splitter.png", storableCreator, SplitterView::class)
	net.addBaseElement("Concentrator", "library.element.Concentrator", "/img/concentrator.png", storableCreator, ConcentratorView::class)
	net.addBaseElement("Probe", "library.element.Probe", "/img/probe.png", storableCreator, ProbeView::class)
	net.addBaseElement("Tunnel", "library.element.Tunnel", "/img/tunnel.png", storableCreator, TunnelView::class)

	val base = library.ensureFolder(Translations.getString("library.folder.baseElements"))
	base.addBaseElement("AND", "library.element.AndGate", "/img/and.png", storableCreator, AndGateView::class)
	base.addBaseElement("OR", "library.element.OrGate", "/img/or.png", storableCreator, OrGateView::class)
	base.addBaseElement("NOT", "library.element.NotGate", "/img/not.png", storableCreator, NotGateView::class)
	base.addBaseElement("NAND", "library.element.NandGate", "/img/nand.png", storableCreator, NandGateView::class)
	base.addBaseElement("NOR", "library.element.NorGate", "/img/nor.png", storableCreator, NorGateView::class)
	base.addBaseElement("XOR", "library.element.XorGate", "/img/xor.png", storableCreator, XorGateView::class)
	base.addBaseElement("XNOR", "library.element.XnorGate", "/img/xnor.png", storableCreator, XnorGateView::class)
	base.addBaseElement("Buffer", "library.element.Buffer", "/img/buffer.png", storableCreator, BufferGateView::class)
	base.addBaseElement("TriStateBuffer", "library.element.TriStateBuffer", "/img/tristate-buffer.png", storableCreator, TriStateBufferGateView::class)
	base.addBaseElement("Delay", "library.element.Delay", "/img/delay.png", storableCreator, DelayGateView::class)

	val input = library.ensureFolder(Translations.getString("library.folder.input"))
	input.addBaseElement("Input", "library.element.CircuitInput", "/img/input.png") {
		val view = storableCreator.create(CircuitInOutView::class) as CircuitInOutView
		view.portType = PortType.INPUT
		view
	}
	input.addBaseElement("Switch", "library.element.Switch", "/img/switch.png", storableCreator, SwitchView::class)
	input.addBaseElement("Clock", "library.element.Clock", "/img/clock.png", storableCreator, ClockView::class)

	val output = library.ensureFolder(Translations.getString("library.folder.output"))
	output.addBaseElement("Output", "library.element.CircuitOutput", "/img/output.png") {
		val view = storableCreator.create(CircuitInOutView::class) as CircuitInOutView
		view.portType = PortType.OUTPUT
		view
	}
	output.addBaseElement("LED", "library.element.LED", "/img/led.png", storableCreator, LEDView::class)
	output.addBaseElement("SevenSegmentDisplay", "library.element.SevenSegmentDisplay", "/img/7segment.png",
		storableCreator, SevenSegmentDisplayView::class)
	output.addBaseElement("LEDMatrix", "library.element.LEDMatrix", "/img/led-matrix.png", storableCreator, LEDMatrixView::class)

	val memory = library.ensureFolder(Translations.getString("library.folder.memory"))
	memory.addBaseElement("ROM", "library.element.ROM", "/img/rom.png", storableCreator, ROMView::class)
	memory.addBaseElement("RAM", "library.element.RAM", "/img/ram.png", storableCreator, RAMView::class)
}

