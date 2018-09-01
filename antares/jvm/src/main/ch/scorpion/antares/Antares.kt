package ch.scorpion.antares

import ch.scorpion.antares.view.arithmetic.RandomView
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
import ch.scorpion.jabbah.graph.library.BaseLibraryElement
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryDirectory
import ch.scorpion.jabbah.graph.library.LibraryService
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.io.StorableCreator
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.reflect.KClass

interface Antares : DesktopApplication {

	companion object {
		private const val DISPLAY_NAME = "Antares"
		private const val SYSTEM_NAME = "antares"
		private const val FILE_EXTENSION_NAME = "cir"
		private const val DEFAULT_LIB_DIRECTORY = "libraries"
		private const val DEFAULT_PROJECT_DIRECTORY = "projects"
		private const val DEFAULT_LIB_FILENAME = "library.lib"
	}

	/** ---- [DesktopApplication] */

	override val displayName: String get() = DISPLAY_NAME

	override val systemName: String get() = SYSTEM_NAME

	override val fileExtension: String get() = FILE_EXTENSION_NAME

	/** ---- [Antares] */

	val projectsDirectoryPath: Path get() = FileSystems.getDefault().getPath(homeDirectoryPath.toString(), DEFAULT_PROJECT_DIRECTORY)

	val libraryDirectoryPath: Path get() = FileSystems.getDefault().getPath(homeDirectoryPath.toString(), DEFAULT_LIB_DIRECTORY)

	val libraryFileName: String get() = DEFAULT_LIB_FILENAME

}

fun fillStandardLibrary(library: Library, service: LibraryService, storableCreator: StorableCreator) {
	val net = service.ensureFolder(library, Translations.getString("library.folder.net"), library)
	addBaseElement(net, "Constant", "library.element.Constant", "/img/constant.png", storableCreator, ConstantView::class)
	addBaseElement(net, "Splitter", "library.element.Splitter", "/img/splitter.png", storableCreator, SplitterView::class)
	addBaseElement(net, "Concentrator", "library.element.Concentrator", "/img/concentrator.png", storableCreator, ConcentratorView::class)
	addBaseElement(net, "Probe", "library.element.Probe", "/img/probe.png", storableCreator, ProbeView::class)
	addBaseElement(net, "Tunnel", "library.element.Tunnel", "/img/tunnel.png", storableCreator, TunnelView::class)

	val base = service.ensureFolder(library, Translations.getString("library.folder.baseElements"), library)
	addBaseElement(base, "AND", "library.element.AndGate", "/img/and.png", storableCreator, AndGateView::class)
	addBaseElement(base, "OR", "library.element.OrGate", "/img/or.png", storableCreator, OrGateView::class)
	addBaseElement(base, "NOT", "library.element.NotGate", "/img/not.png", storableCreator, NotGateView::class)
	addBaseElement(base, "NAND", "library.element.NandGate", "/img/nand.png", storableCreator, NandGateView::class)
	addBaseElement(base, "NOR", "library.element.NorGate", "/img/nor.png", storableCreator, NorGateView::class)
	addBaseElement(base, "XOR", "library.element.XorGate", "/img/xor.png", storableCreator, XorGateView::class)
	addBaseElement(base, "XNOR", "library.element.XnorGate", "/img/xnor.png", storableCreator, XnorGateView::class)
	addBaseElement(base, "Buffer", "library.element.Buffer", "/img/buffer.png", storableCreator, BufferGateView::class)
	addBaseElement(base, "TriStateBuffer", "library.element.TriStateBuffer", "/img/tristate-buffer.png", storableCreator, TriStateBufferGateView::class)
	addBaseElement(base, "Delay", "library.element.Delay", "/img/delay.png", storableCreator, DelayGateView::class)

	val input = service.ensureFolder(library, Translations.getString("library.folder.input"), library)
	addBaseElement(input, "Input", "library.element.CircuitInput", "/img/input.png") {
		val view = storableCreator.create(CircuitInOutView::class) as CircuitInOutView
		view.portType = PortType.INPUT
		view
	}
	addBaseElement(input, "Switch", "library.element.Switch", "/img/switch.png", storableCreator, SwitchView::class)
	addBaseElement(input, "Clock", "library.element.Clock", "/img/clock.png", storableCreator, ClockView::class)

	val output = service.ensureFolder(library, Translations.getString("library.folder.output"), library)
	addBaseElement(output, "Output", "library.element.CircuitOutput", "/img/output.png") {
		val view = storableCreator.create(CircuitInOutView::class) as CircuitInOutView
		view.portType = PortType.OUTPUT
		view
	}
	addBaseElement(output, "LED", "library.element.LED", "/img/led.png", storableCreator, LEDView::class)
	addBaseElement(output, "SevenSegmentDisplay", "library.element.SevenSegmentDisplay", "/img/7segment.png",
		storableCreator, SevenSegmentDisplayView::class)
	addBaseElement(output, "LEDMatrix", "library.element.LEDMatrix", "/img/led-matrix.png", storableCreator, LEDMatrixView::class)

	val memory = service.ensureFolder(library, Translations.getString("library.folder.memory"), library)
	addBaseElement(memory, "ROM", "library.element.ROM", "/img/rom.png", storableCreator, ROMView::class)
	addBaseElement(memory, "RAM", "library.element.RAM", "/img/ram.png", storableCreator, RAMView::class)

	val arithmetic = service.ensureFolder(library, Translations.getString("library.folder.arithmetic"), library)
	addBaseElement(arithmetic, "Random", "library.element.Random", "/img/random.png", storableCreator, RandomView::class)

}

private fun addBaseElement(directory: LibraryDirectory, name: String, translationKey: String, iconPath: String?, storableCreator: StorableCreator?, clazz: KClass<out GraphElementView<*>>) {
	val elem = BaseLibraryElement(name, translationKey, iconPath, storableCreator, clazz)
	directory.add(elem)
}

private fun addBaseElement(directory: LibraryDirectory, name: String, translationKey: String, iconPath: String?, supplier: () -> GraphElementView<out GraphElement>) {
	val elem = BaseLibraryElement(name, translationKey, iconPath, null, null, supplier)
	directory.add(elem)
}

