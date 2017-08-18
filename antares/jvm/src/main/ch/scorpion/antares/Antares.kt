package ch.scorpion.antares

import ch.scorpion.antares.view.DigitalComponentViewDrawer
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.gate.*
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.input.ClockView
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.input.ToggleButtonView
import ch.scorpion.antares.view.memory.MemoryContentsPanel
import ch.scorpion.antares.view.memory.OpenMemoryContentsRequest
import ch.scorpion.antares.view.memory.RAMView
import ch.scorpion.antares.view.memory.ROMView
import ch.scorpion.antares.view.net.*
import ch.scorpion.antares.view.output.LEDMatrixView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.antares.view.output.SevenSegmentDisplayView
import ch.scorpion.jabbah.app.AbstractApplicationFrame
import ch.scorpion.jabbah.app.AbstractDesktopApplication
import ch.scorpion.jabbah.app.MenuBarBuilder
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.container.ContainerPanel
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryFolder
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.ui.GraphFrame
import ch.scorpion.jabbah.graph.ui.GraphPanel
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.editor.GraphEditor
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCreator
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import java.nio.file.FileSystems
import java.nio.file.Path
import javax.swing.JDialog
import javax.swing.plaf.FontUIResource


/**
 * The main application class of the Antares digital circuit editor and simulator desktop application.
 */
class Antares(
    args: Array<String>,
    eventBus: EventBus,
    private val viewManager: ViewManager,
    val storableCreator: StorableCreator,
    val schedulerProvider: () -> Scheduler
) : AbstractDesktopApplication(args, eventBus) {

    constructor(args: Array<String>): this(
        args,
        BaseModule.eventBus,
        DrawViewModule.viewManager,
        IOModule.storableCreator,
        { ExecutionModule.scheduler })

    companion object {

        val DISPLAY_NAME = "Antares"
        val SYSTEM_NAME = "antares"
        val FILE_EXTENSION_NAME = "cir"
        val DEFAULT_LIB_DIRECTORY = "library"
        val DEFAULT_LIB_FILENAME = "library.lib"

        @JvmStatic fun main(args: Array<String>) {
            BaseModuleJvm.require()
            Antares(args).start()
        }
    }

    init {
        eventBus.register(OpenMemoryContentsRequest::class, { handle(it) })
    }

    /** ---- [AbstractDesktopApplication] */

    override val displayName: String get() = DISPLAY_NAME

    override val systemName: String get() = SYSTEM_NAME

    override val fileExtension: String get() = FILE_EXTENSION_NAME

    override fun createNewApplicationData(): Storable {
        return MetaGraph()
    }

    override fun init() {
        AntaresModuleJvm(this).require()
        LibraryModule.libraryHolder.library.load()
        fillStandardLibrary(LibraryModule.libraryHolder.library)
        UiUtil.setUIFont(FontUIResource(Look.UI_FONT.family.javaName, Look.UI_FONT.style, Look.UI_FONT.size))
        super.init()
    }

    override fun defineOptions(options: Options) {
        super.defineOptions(options)
        options.addOption(Option.builder("t")
            .required(false)
            .longOpt("theme")
            .desc("Theme")
            .hasArg()
            .build())
    }

    override fun consumeCommandLine(commandLine: CommandLine) {
        super.consumeCommandLine(commandLine)
        if (commandLine.hasOption("t")) {
            Themes.setCurrent(commandLine.getOptionValue("t"))
        }
    }

    override fun createMenuBarBuilder(): MenuBarBuilder {
        return AntaresMenuBarBuilder(this, eventBus)
    }

    override fun createMainFrame(): AbstractApplicationFrame {
        // TODO Extract GraphEditor creation to factory in module
        val graphCanvas = CanvasJvm({
            val drawingView = DrawingViewImpl<Drawing<Component>>(GraphViewImpl<GraphElementView<*>>() as Drawing<Component>, it)
            drawingView.addDrawableDrawer(DigitalComponentViewDrawer())
            drawingView
        })
        val graphEditor = GraphEditor(graphCanvas.view as DrawingView<Drawing<Component>>)
        val graphPanel = GraphPanel(editor = graphEditor, viewManager = viewManager)
        graphPanel.libraryPanel.libraryPreviewPanel.addDrawableDrawer(DigitalComponentViewDrawer())
        val containerPanel = ContainerPanel(GraphViewModule.containerEditorFactory.invoke(eventBus), viewManager)

        val graphFrame = GraphFrame(this, containerPanel, eventBus, viewManager, schedulerProvider.invoke())
        graphFrame.desktop.masterGraphPanel = graphPanel

        return graphFrame
    }

    /** ---- [Antares] */

    fun getLibraryDirectoryPath(): Path {
        return FileSystems.getDefault().getPath(getHomeDirectoryPath().toString(), DEFAULT_LIB_DIRECTORY)
    }

    fun getLibraryFileName(): String {
        return DEFAULT_LIB_FILENAME
    }

    private fun fillStandardLibrary(library: Library) {
        val net = ensureLibraryFolder(library, Translations.getString("library.folder.net"))
        net.addBaseElement("Constant", "library.element.Constant", "/img/constant.png", storableCreator, ConstantView::class)
        net.addBaseElement("Splitter", "library.element.Splitter", "/img/splitter.png", storableCreator, SplitterView::class)
        net.addBaseElement("Concentrator", "library.element.Concentrator", "/img/concentrator.png", storableCreator, ConcentratorView::class)
        net.addBaseElement("Probe", "library.element.Probe", "/img/probe.png", storableCreator, ProbeView::class)
        net.addBaseElement("Tunnel", "library.element.Tunnel", "/img/tunnel.png", storableCreator, TunnelView::class)

        val base = ensureLibraryFolder(library, Translations.getString("library.folder.baseElements"))
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

        val input = ensureLibraryFolder(library, Translations.getString("library.folder.input"))
        input.addBaseElement("Input", "library.element.CircuitInput", "/img/input.png") {
            val view = storableCreator.create(CircuitInOutView::class) as CircuitInOutView
            view.portType = PortType.INPUT
            view
        }
        input.addBaseElement("Switch", "library.element.Switch", "/img/switch.png", storableCreator, SwitchView::class)
        input.addBaseElement("Switch", "library.element.Toggle", "/img/toggle.png", storableCreator, ToggleButtonView::class)
        input.addBaseElement("Clock", "library.element.Clock", "/img/clock.png", storableCreator, ClockView::class)

        val output = ensureLibraryFolder(library, Translations.getString("library.folder.output"))
        output.addBaseElement("Output", "library.element.CircuitOutput", "/img/output.png") {
            val view = storableCreator.create(CircuitInOutView::class) as CircuitInOutView
            view.portType = PortType.OUTPUT
            view
        }
        output.addBaseElement("LED", "library.element.LED", "/img/led.png", storableCreator, LEDView::class)
        output.addBaseElement("SevenSegmentDisplay", "library.element.SevenSegmentDisplay", "/img/7segment.png",
                storableCreator, SevenSegmentDisplayView::class)
        output.addBaseElement("LEDMatrix", "library.element.LEDMatrix", "/img/led-matrix.png", storableCreator, LEDMatrixView::class)

        val memory = ensureLibraryFolder(library, Translations.getString("library.folder.memory"))
        memory.addBaseElement("ROM", "library.element.ROM", "/img/rom.png", storableCreator, ROMView::class)
        memory.addBaseElement("RAM", "library.element.RAM", "/img/ram.png", storableCreator, RAMView::class)
    }

    /**
     * Ensures that the [Library] contains a [LibraryFolder] with the specified name. Creates and adds a
     * [LibraryFolder] if it doesn't exist yet.
     */
    private fun ensureLibraryFolder(library: Library, name: String): LibraryFolder {
        val item = library.get(name)
        if (item != null) {
            return item as LibraryFolder
        }
        return library.addFolder(name)
    }

    private fun handle(event: OpenMemoryContentsRequest) {
        val dialog = JDialog(mainFrame, true)
        dialog.title = Translations.getString("antares.action.memory.contents.title")
        dialog.contentPane.add(
                MemoryContentsPanel(event.memory, event.addressWidth, event.dataWidth, mainFrame.editor.commandManager))
        dialog.pack()
        dialog.setLocationRelativeTo(mainFrame)
        dialog.isVisible = true
    }
}