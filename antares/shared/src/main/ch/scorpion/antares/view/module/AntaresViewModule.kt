package ch.scorpion.antares.view.module

import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.script.AntaresScriptGateway
import ch.scorpion.antares.view.DigitalComponentViewDrawer
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.arithmetic.RandomView
import ch.scorpion.antares.view.container.DigitalPortViewComponent
import ch.scorpion.antares.view.gate.*
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.input.*
import ch.scorpion.antares.view.memory.RAMView
import ch.scorpion.antares.view.memory.ROMView
import ch.scorpion.antares.view.net.*
import ch.scorpion.antares.view.oscilloscope.DigitalOscilloscopeViewFactory
import ch.scorpion.antares.view.output.*
import ch.scorpion.antares.view.port.DigitalPortFactory
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.signal.DigitalSignalSourceControlView
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.Style
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Grid
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.select.Handle
import ch.scorpion.jabbah.edit.select.SelectedColorSelectionModel
import ch.scorpion.jabbah.edit.select.SelectionModelFactory
import ch.scorpion.jabbah.edit.snap.ComponentSnapper
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.container.OriginIndicator
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.view.CurrentGraphViewAnimationType
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.editor.AutoConnectorHighlight
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.DragEdgePointHighlight
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewBelowSelectionModel
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewReplaceSelectionModel
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions for the [ch.scorpion.antares.view]
 */
object AntaresViewModule : AbstractModule() {

	private const val CONSTANT = "Constant"
	private const val SPLITTER = "Splitter"
	private const val CONCENTRATOR = "Concentrator"
	private const val PROBE = "Probe"
	private const val TUNNEL = "Tunnel"

	private const val AND = "AND"
	private const val OR = "OR"
	private const val NOT = "NOT"
	private const val NAND = "NAND"
	private const val NOR = "NOR"
	private const val XOR = "XOR"
	private const val XNOR = "XNOR"
	private const val BUFFER = "Buffer"
	private const val TRISTATE_BUFFER = "TriStateBuffer"
	private const val DELAY = "Delay"

	private const val INPUT = "Input"
	private const val SWITCH = "Switch"
	private const val DIP_SWITCH = "DipSwitch"
	private const val CLOCK = "Clock"
	private const val KEYBOARD = "Keyboard"
	private const val TERMINAL = "Terminal"

	private const val OUTPUT = "Output"
	private const val LED = "LED"
	private const val RGB_LED = "RgbLED"
	private const val SEVEN_SEGMENT_DISPLAY = "SevenSegmentDisplay"
	private const val LED_MATRIX = "LEDMatrix"

	private const val ROM = "ROM"
	private const val RAM = "RAM"

	private const val RANDOM = "Random"

	val currentSymbolStyle: CurrentSymbolStyle = CurrentSymbolStyle()
    val currentGraphViewAnimationType: CurrentGraphViewAnimationType by lazy { CurrentGraphViewAnimationType() }

    override fun initialize() {
        Translations.addBundle("antares")

        // Overwritten in order to change the [DrawableDrawer]
        EditModule.drawingViewFactory = {d,c ->
            val drawingView = DrawingViewImpl(d, c)
            drawingView.addDrawableDrawer(DigitalComponentViewDrawer())
            drawingView
        }

        GraphViewModule.portFactory = DigitalPortFactory(DrawStyleModule.styleProvider)
        GraphViewModule.oscilloscopeViewFactory = DigitalOscilloscopeViewFactory()
        val edgeViewFactory = DigitalEdgeViewFactory(
                DrawStyleModule.styleProvider,
                { GraphViewModule.edgeToPortConnector },
                { GraphViewModule.dragEdgeViewOriginConnector },
                { GraphViewModule.dragEdgeViewDestinationConnector },
                ExecutionModule.currentSystemSpeedCategory
        )
        GraphViewModule.setEdgeViewFactory(edgeViewFactory)
        GraphViewModule.setNodeViewFactory(DigitalNodeViewFactory(
                DrawStyleModule.styleProvider,
                ExecutionModule.currentSystemSpeedCategory)
        )

        GraphViewModule.require()
        AnimationModule.require()
        AntaresModelModule.require()

        customizeProperties(BaseModule.properties)

        configureTypeMap(IOModule.typeMap)
        configureSelectionModels(EditSelectModule.selectionModelFactory)

        ScriptModule.scriptGatewayProvider = { AntaresScriptGateway() }

	    registerBaseLibraryElements(LibraryModule.baseLibraryElementRepository)
    }

    private fun customizeProperties(properties: Properties) {
        properties.set(Style.PROP_FOREGROUND_COLOR, Themes.get<GraphTheme>().vertice.color.foregroundColor)
        properties.set(Style.PROP_BACKGROUND_COLOR, Themes.get<GraphTheme>().vertice.color.backgroundColor)
        properties.set(Style.PROP_TEXT_COLOR, Themes.get<GraphTheme>().vertice.color.textColor)
        properties.set(Style.PROP_STROKE, Themes.get<GraphTheme>().vertice.stroke)
        properties.set(Style.PROP_FONT, Themes.get<GraphTheme>().vertice.font)

        properties.set(Grid.PROP_GRID_DEFAULT_DISTANCE, Look.GRID)
        properties.set(Grid.PROP_GRID_DEFAULT_PAINT_FACTOR, 2)
	    properties.set(Grid.PROP_GRID_MIN_DISTANCE, 12)

        properties.set(ComponentSnapper.PROP_SNAP_HIGHLIGHT_COLOR, Themes.get<GraphTheme>().selection.foregroundColor)
        properties.set(ComponentSnapper.PROP_SNAP_HIGHLIGHT_STROKE, Stroke(0.5f))

        properties.set(OriginIndicator.PROP_SELECTION_COLOR, Themes.get<GraphTheme>().selection.foregroundColor)

        properties.set(Handle.PROP_BORDER_COLOR, Themes.get<GraphTheme>().selection.foregroundColor)
        properties.set(Handle.PROP_FILL_COLOR, Themes.get<GraphTheme>().selection.backgroundColor)

        properties.set(AutoConnectorHighlight.PROP_COLOR, Themes.get<GraphTheme>().selection.foregroundColor)
        properties.set(DragEdgePointHighlight.PROP_COLOR, Themes.get<GraphTheme>().selection.foregroundColor)

        properties.set(CircuitInOutView.PROP_INPUT_ICON_PATH, "/img/input.png")
        properties.set(CircuitInOutView.PROP_OUTPUT_ICON_PATH, "/img/output.png")
        properties.set(CircuitInOutView.PROP_INOUT_ICON_PATH, "/img/inout.png")
        properties.set(SwitchView.PROP_ICON_PATH, "/img/switch.png")
	    properties.set(DipSwitchView.PROP_ICON_PATH, "/img/dip-switch.png")
        properties.set(ProbeView.PROP_ICON_PATH, "/img/probe.png")
        properties.set(LEDView.PROP_ICON_PATH, "/img/led.png")
	    properties.set(RgbLEDView.PROP_ICON_PATH, "/img/rgb-led.png")
        properties.set(LEDMatrixView.PROP_ICON_PATH, "/img/led-matrix.png")
        properties.set(SevenSegmentDisplayView.PROP_ICON_PATH, "/img/7segment.png")
	    properties.set(TerminalView.PROP_ICON_PATH, "/img/terminal.png")
	    properties.set(KeyboardView.PROP_ICON_PATH, "/img/keyboard.png")

	    properties.set(AndGateView.PROP_DATA_FLOW_ENABLED, true)
    }

    private fun configureTypeMap(typeMap: TypeMap) {
        typeMap.register("circuitInOutView", CircuitInOutView::class)
        typeMap.register("digitalEdgeView", DigitalEdgeView::class)
        typeMap.register("digitalNodeView", DigitalNodeView::class)
        typeMap.register("digitalPortView", DigitalPortView::class)
        typeMap.register("digitalPortViewComp", DigitalPortViewComponent::class)
        typeMap.register("digitalSignalSourceCV", DigitalSignalSourceControlView::class)

        typeMap.register("notGateView", NotGateView::class)
        typeMap.register("andGateView", AndGateView::class)
        typeMap.register("nandGateView", NandGateView::class)
        typeMap.register("orGateView", OrGateView::class)
        typeMap.register("norGateView", NorGateView::class)
        typeMap.register("xorGateView", XorGateView::class)
        typeMap.register("xnorGateView", XnorGateView::class)
        typeMap.register("bufferGateView", BufferGateView::class)
        typeMap.register("triStateBufferGateView", TriStateBufferGateView::class)

        typeMap.register("switchView", SwitchView::class)
	    typeMap.register("dipSwitchView", DipSwitchView::class)
        typeMap.register("clockView", ClockView::class)
        typeMap.register("ledView", LEDView::class)
	    typeMap.register("RgbLedView", RgbLEDView::class)
        typeMap.register("sevenSegmentDisplayView", SevenSegmentDisplayView::class)
        typeMap.register("splitterView", SplitterView::class)
        typeMap.register("concentratorView", ConcentratorView::class)
        typeMap.register("constantView", ConstantView::class)
        typeMap.register("probeView", ProbeView::class)
        typeMap.register("ramView", RAMView::class)
        typeMap.register("romView", ROMView::class)
        typeMap.register("delayView", DelayGateView::class)
        typeMap.register("tunnelView", TunnelView::class)
        typeMap.register("ledMatrixView", LEDMatrixView::class)
	    typeMap.register("randomView", RandomView::class)
	    typeMap.register("keyboardView", KeyboardView::class)
	    typeMap.register("terminalView", TerminalView::class)
    }

    private fun configureSelectionModels(factory: SelectionModelFactory) {
        factory.register(SelectionDrawingStrategy.REPLACE, DigitalNodeView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.REPLACE, DigitalPortViewComponent::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.REPLACE, DigitalSignalSourceControlView::class.simpleName!!) { SelectedColorSelectionModel(it) }

	    factory.register(SelectionDrawingStrategy.REPLACE, LabelComponent::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.REPLACE, DigitalEdgeView::class.simpleName!!) { EdgeViewReplaceSelectionModel(it as EdgeView<*>) }
	    factory.register(SelectionDrawingStrategy.BELOW, DigitalEdgeView::class.simpleName!!) { EdgeViewBelowSelectionModel(it as EdgeView<*>) }

	    factory.register(SelectionDrawingStrategy.REPLACE, SplitterView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.REPLACE, ConcentratorView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.REPLACE, ProbeView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.REPLACE, ConstantView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.REPLACE, TunnelView::class.simpleName!!) { SelectedColorSelectionModel(it) }

	    factory.register(SelectionDrawingStrategy.REPLACE, AndGateView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.BELOW, AndGateView::class.simpleName!!) { BoxGateViewBelowSelectionModel(it as BoxGateView<*>) }
	    factory.register(SelectionDrawingStrategy.REPLACE, OrGateView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.BELOW, OrGateView::class.simpleName!!) { BoxGateViewBelowSelectionModel(it as BoxGateView<*>) }
	    factory.register(SelectionDrawingStrategy.REPLACE, NotGateView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.BELOW, NotGateView::class.simpleName!!) { BoxGateViewBelowSelectionModel(it as BoxGateView<*>) }
	    factory.register(SelectionDrawingStrategy.REPLACE, NandGateView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.BELOW, NandGateView::class.simpleName!!) { BoxGateViewBelowSelectionModel(it as BoxGateView<*>) }
	    factory.register(SelectionDrawingStrategy.REPLACE, NorGateView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.BELOW, NorGateView::class.simpleName!!) { BoxGateViewBelowSelectionModel(it as BoxGateView<*>) }
	    factory.register(SelectionDrawingStrategy.REPLACE, XorGateView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.BELOW, XorGateView::class.simpleName!!) { BoxGateViewBelowSelectionModel(it as BoxGateView<*>) }
	    factory.register(SelectionDrawingStrategy.REPLACE, XnorGateView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.BELOW, XnorGateView::class.simpleName!!) { BoxGateViewBelowSelectionModel(it as BoxGateView<*>) }
	    factory.register(SelectionDrawingStrategy.REPLACE, BufferGateView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.BELOW, BufferGateView::class.simpleName!!) { BoxGateViewBelowSelectionModel(it as BoxGateView<*>) }
	    factory.register(SelectionDrawingStrategy.REPLACE, TriStateBufferGateView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.BELOW, TriStateBufferGateView::class.simpleName!!) { BoxGateViewBelowSelectionModel(it as BoxGateView<*>) }
	    factory.register(SelectionDrawingStrategy.REPLACE, DelayGateView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.BELOW, DelayGateView::class.simpleName!!) { BoxGateViewBelowSelectionModel(it as BoxGateView<*>) }

	    factory.register(SelectionDrawingStrategy.REPLACE, SwitchView::class.simpleName!!) { SwitchViewSelectionModel(it as SwitchView) }
	    factory.register(SelectionDrawingStrategy.REPLACE, DipSwitchView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.REPLACE, ClockView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.REPLACE, CircuitInOutView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.REPLACE, KeyboardView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.REPLACE, TerminalView::class.simpleName!!) { SelectedColorSelectionModel(it) }

	    factory.register(SelectionDrawingStrategy.REPLACE, LEDView::class.simpleName!!) { LEDViewSelectionModel(it as LEDView) }
	    factory.register(SelectionDrawingStrategy.REPLACE, RgbLEDView::class.simpleName!!) { LEDViewSelectionModel(it as RgbLEDView) }
	    factory.register(SelectionDrawingStrategy.REPLACE, SevenSegmentDisplayView::class.simpleName!!) { SevenSegmentDisplayViewSelectionModel(it as SevenSegmentDisplayView) }
	    factory.register(SelectionDrawingStrategy.REPLACE, LEDMatrixView::class.simpleName!!) { LEDMatrixViewSelectionModel(it as LEDMatrixView) }

	    factory.register(SelectionDrawingStrategy.REPLACE, ROMView::class.simpleName!!) { SelectedColorSelectionModel(it) }
	    factory.register(SelectionDrawingStrategy.REPLACE, RAMView::class.simpleName!!) { SelectedColorSelectionModel(it) }

	    factory.register(SelectionDrawingStrategy.REPLACE, RandomView::class.simpleName!!) { SelectedColorSelectionModel(it) }
    }

	private fun registerBaseLibraryElements(repository: BaseLibraryElementRepository) {
		repository.register(CONSTANT, "library.element.Constant", "/img/constant.png", ConstantView::class)
		repository.register(SPLITTER, "library.element.Splitter", "/img/splitter.png", SplitterView::class)
		repository.register(CONCENTRATOR, "library.element.Concentrator", "/img/concentrator.png", ConcentratorView::class)
		repository.register(PROBE, "library.element.Probe", "/img/probe.png", ProbeView::class)
		repository.register(TUNNEL, "library.element.Tunnel", "/img/tunnel.png", TunnelView::class)

		repository.register(AND, "library.element.AndGate", "/img/and.png",  AndGateView::class)
		repository.register(OR, "library.element.OrGate", "/img/or.png",  OrGateView::class)
		repository.register(NOT, "library.element.NotGate", "/img/not.png",  NotGateView::class)
		repository.register(NAND, "library.element.NandGate", "/img/nand.png",  NandGateView::class)
		repository.register(NOR, "library.element.NorGate", "/img/nor.png",  NorGateView::class)
		repository.register(XOR, "library.element.XorGate", "/img/xor.png", XorGateView::class)
		repository.register(XNOR, "library.element.XnorGate", "/img/xnor.png", XnorGateView::class)
		repository.register(BUFFER, "library.element.Buffer", "/img/buffer.png", BufferGateView::class)
		repository.register(TRISTATE_BUFFER, "library.element.TriStateBuffer", "/img/tristate-buffer.png", TriStateBufferGateView::class)
		repository.register(DELAY, "library.element.Delay", "/img/delay.png", DelayGateView::class)

		repository.register(INPUT, "library.element.CircuitInput", "/img/input.png") {
			val view = it.create(CircuitInOutView::class) as CircuitInOutView
			view.portType = PortType.INPUT
			view
		}
		repository.register(SWITCH, "library.element.Switch", "/img/switch.png", SwitchView::class)
		repository.register(DIP_SWITCH, "library.element.DipSwitch", "/img/dip-switch.png", DipSwitchView::class)
		repository.register(CLOCK, "library.element.Clock", "/img/clock.png", ClockView::class)
		repository.register(KEYBOARD, "library.element.Keyboard", "/img/keyboard.png", KeyboardView::class)
		repository.register(TERMINAL, "library.element.Terminal", "/img/terminal.png", TerminalView::class)

		repository.register(OUTPUT, "library.element.CircuitOutput", "/img/output.png") {
			val view = it.create(CircuitInOutView::class) as CircuitInOutView
			view.portType = PortType.OUTPUT
			view
		}
		repository.register(LED, "library.element.RgbLED", "/img/rgb-led.png", RgbLEDView::class)
		repository.register(RGB_LED, "library.element.LED", "/img/led.png", LEDView::class)
		repository.register(SEVEN_SEGMENT_DISPLAY, "library.element.SevenSegmentDisplay", "/img/7segment.png", SevenSegmentDisplayView::class)
		repository.register(LED_MATRIX, "library.element.LEDMatrix", "/img/led-matrix.png", LEDMatrixView::class)

		repository.register(ROM, "library.element.ROM", "/img/rom.png", ROMView::class)
		repository.register(RAM, "library.element.RAM", "/img/ram.png", RAMView::class)

		repository.register(RANDOM, "library.element.Random", "/img/random.png", RandomView::class)
	}

	fun fillBaseElementLibrary(library: Library) {
		val net = LibraryFolder(Translations.getString("library.folder.net"))
		addLibraryItem(library, BaseLibraryElement(CONSTANT), net)
		addLibraryItem(library, BaseLibraryElement(SPLITTER), net)
		addLibraryItem(library, BaseLibraryElement(CONCENTRATOR), net)
		addLibraryItem(library, BaseLibraryElement(PROBE), net)
		addLibraryItem(library, BaseLibraryElement(TUNNEL), net)
		addLibraryItem(library, net, library)

		val base = LibraryFolder(Translations.getString("library.folder.baseElements"))
		addLibraryItem(library, BaseLibraryElement(AND), base)
		addLibraryItem(library, BaseLibraryElement(OR), base)
		addLibraryItem(library, BaseLibraryElement(NOT), base)
		addLibraryItem(library, BaseLibraryElement(NAND), base)
		addLibraryItem(library, BaseLibraryElement(NOR), base)
		addLibraryItem(library, BaseLibraryElement(XOR), base)
		addLibraryItem(library, BaseLibraryElement(XNOR), base)
		addLibraryItem(library, BaseLibraryElement(BUFFER), base)
		addLibraryItem(library, BaseLibraryElement(TRISTATE_BUFFER), base)
		addLibraryItem(library, BaseLibraryElement(DELAY), base)
		addLibraryItem(library, base, library)

		val input = LibraryFolder(Translations.getString("library.folder.input"))
		addLibraryItem(library, BaseLibraryElement(INPUT), input)
		addLibraryItem(library, BaseLibraryElement(SWITCH), input)
		addLibraryItem(library, BaseLibraryElement(DIP_SWITCH), input)
		addLibraryItem(library, BaseLibraryElement(CLOCK), input)
		addLibraryItem(library, BaseLibraryElement(KEYBOARD), input)
		addLibraryItem(library, input, library)

		val output = LibraryFolder(Translations.getString("library.folder.output"))
		addLibraryItem(library, BaseLibraryElement(OUTPUT), output)
		addLibraryItem(library, BaseLibraryElement(LED), output)
		addLibraryItem(library, BaseLibraryElement(RGB_LED), output)
		addLibraryItem(library, BaseLibraryElement(SEVEN_SEGMENT_DISPLAY), output)
		addLibraryItem(library, BaseLibraryElement(LED_MATRIX), output)
		addLibraryItem(library, output, library)

		val memory = LibraryFolder(Translations.getString("library.folder.memory"))
		addLibraryItem(library, BaseLibraryElement(ROM), memory)
		addLibraryItem(library, BaseLibraryElement(RAM), memory)
		addLibraryItem(library, memory, library)

		val arithmetic = LibraryFolder(Translations.getString("library.folder.arithmetic"))
		addLibraryItem(library, BaseLibraryElement(RANDOM), arithmetic)
		addLibraryItem(library, arithmetic, library)
	}

	private fun addLibraryItem(library: Library, item: LibraryItem, directory: LibraryDirectory) {
		item.bindTo(library)
		directory.add(item)
	}
}