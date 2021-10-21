package ch.scorpion.antares.view.module

import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalNotation
import ch.scorpion.antares.view.*
import ch.scorpion.antares.view.addressable.RAMView
import ch.scorpion.antares.view.addressable.ROMView
import ch.scorpion.antares.view.app.DigitalGraphViewService
import ch.scorpion.antares.view.arithmetic.BitExtenderView
import ch.scorpion.antares.view.arithmetic.RandomView
import ch.scorpion.antares.view.container.DigitalPortViewComponent
import ch.scorpion.antares.view.container.DilCase
import ch.scorpion.antares.view.container.DilCaseDragDestinationHighlight
import ch.scorpion.antares.view.gate.*
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.input.*
import ch.scorpion.antares.view.net.*
import ch.scorpion.antares.view.oscilloscope.DigitalOscilloscopeProbeNameStrategy
import ch.scorpion.antares.view.oscilloscope.DigitalOscilloscopeViewFactory
import ch.scorpion.antares.view.oscilloscope.DigitalSignalHistoryDrawer
import ch.scorpion.antares.view.output.*
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.port.DigitalPortViewFactory
import ch.scorpion.antares.view.signal.DigitalSignalSourceControlView
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyleToString
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.Style
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.DrawingViewFactory
import ch.scorpion.jabbah.edit.Grid
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.drag.DragDestinationHighlightFactoryRegistry
import ch.scorpion.jabbah.edit.drag.EditDragModule
import ch.scorpion.jabbah.edit.highlight.EditHighlightModule
import ch.scorpion.jabbah.edit.model.rectangle.AbstractRectangularComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangularBelowSelectionModel
import ch.scorpion.jabbah.edit.model.rectangle.RectangularHandleSelectionModel
import ch.scorpion.jabbah.edit.model.rectangle.RectangularReplaceSelectionModel
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.select.*
import ch.scorpion.jabbah.edit.snap.ComponentSnapper
import ch.scorpion.jabbah.edit.style.EditStyleType
import ch.scorpion.jabbah.edit.style.EditTheme
import ch.scorpion.jabbah.edit.view.AttentionDrawerImpl
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.graph.container.OriginIndicator
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.module.GraphModule
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.app.GraphViewAppService
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
	private const val BIDIRECTIONAL_SPLITTER = "BidirectionalSplitter"
	private const val SPLITTER = "Splitter"
	private const val CONCENTRATOR = "Concentrator"
	private const val PROBE = "Probe"
	private const val TUNNEL = "Tunnel"
	private const val BREAK = "Break"
	private const val PULL_RESISTOR = "PullResistor"
	private const val TRANSISTOR = "Transistor"
	private const val GROUND = "Ground"
	private const val POWER = "Power"

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
	private const val JOYSTICK = "Joystick"
	private const val REAL_SWITCH = "RealSwitch"

	private const val OUTPUT = "Output"
	private const val LED = "LED"
	private const val RGB_LED = "RgbLED"
	private const val SEVEN_SEGMENT_DISPLAY = "SevenSegmentDisplay"
	private const val LED_MATRIX = "LEDMatrix"

	private const val ROM = "ROM"
	private const val RAM = "RAM"

	private const val RANDOM = "Random"
	private const val BIT_EXTENDER = "BitExtender"

	val currentSymbolStyle: CurrentSymbolStyle by lazy {CurrentSymbolStyle() }

	override fun initialize() {
		Translations.addBundle("antares")

		// Overwritten in order to change the [DrawableDrawer]
		EditModule.drawingViewFactory = DrawingViewFactory { drawing, contextHolder, displayGlobalMessages ->
			val drawingView = DrawingViewImpl(drawing, applicationContextHolder = contextHolder, displayGlobalMessages = displayGlobalMessages)
			drawingView.addDrawableDrawer(DigitalComponentViewDrawer())
			drawingView
		}
		EditModule.drawingAppService = DigitalGraphViewService()

		EditModule.attentionDrawerFactory = { signal ->
			if (signal is DigitalSignal) {
				AttentionDrawerImpl(color = Themes.get<GraphTheme>().selection.color.foregroundColor)
			} else {
				AttentionDrawerImpl()
			}
		}

		GraphViewModule.graphViewFactory = { DigitalGraphView(it ?: Translations.getString("graph.name.unknown")) }
		GraphViewModule.graphViewAppService = EditModule.drawingAppService as GraphViewAppService
		GraphViewModule.portViewFactory = DigitalPortViewFactory(DrawStyleModule.styleProvider)
		GraphViewModule.oscilloscopeViewFactory = DigitalOscilloscopeViewFactory()
		GraphViewModule.oscilloscopeProbeNameStrategy = DigitalOscilloscopeProbeNameStrategy()
		val edgeViewFactory = DigitalEdgeViewFactory(
			DrawStyleModule.styleProvider,
			{ GraphViewModule.edgeToPortConnector },
			{ GraphViewModule.dragEdgeViewOriginConnector },
			{ GraphViewModule.dragEdgeViewDestinationConnector })
		GraphViewModule.setEdgeViewFactory(edgeViewFactory)
		GraphViewModule.setNodeViewFactory(DigitalNodeViewFactory(
			DrawStyleModule.styleProvider))
		GraphViewModule.graphNavigationViewControllerExtension = { AntaresGraphNavigationViewControllerExtension(it) }
		GraphViewModule.graphViewExecutionAnimationFactory = AntaresExecutionAnimationFactory()

		GraphModule.require()
		AnimationModule.require()
		AntaresModelModule.require()

		customizeProperties(BaseModule.properties)

		configureTypeMap(IOModule.typeMap)
		configureSelectionModels(EditSelectModule.selectionModelFactory)
		configureHighlightModels(EditHighlightModule.highlightModelFactory)
		configureDragDestinationHighlights(EditDragModule.dragDestinationHighlightFactoryRegistry)

		registerBaseLibraryElements(LibraryModule.baseLibraryElementRepository)
	}

	private fun customizeProperties(properties: Properties) {
		properties.set(Look.PROP_FILL_BASIC_COMPONENTS, true)
		Look.initialize(BaseModule.eventBus)

		properties.set(Style.PROP_FOREGROUND_COLOR, Themes.get<GraphTheme>().vertice.color.foregroundColor)
		properties.set(Style.PROP_BACKGROUND_COLOR, Themes.get<GraphTheme>().vertice.color.backgroundColor)
		properties.set(Style.PROP_TEXT_COLOR, Themes.get<GraphTheme>().vertice.color.textColor)
		properties.set(Style.PROP_STROKE, Themes.get<GraphTheme>().vertice.stroke)
		properties.set(Style.PROP_FONT, Themes.get<GraphTheme>().vertice.font)

		properties.set(Grid.PROP_GRID_DEFAULT_DISTANCE, Look.GRID)
		properties.set(Grid.PROP_GRID_DEFAULT_PAINT_FACTOR, 2)
		properties.set(Grid.PROP_GRID_MIN_DISTANCE, 12)

		properties.set(ComponentSnapper.PROP_SNAP_HIGHLIGHT_COLOR, Themes.get<EditTheme>().snap.color.foregroundColor)
		properties.set(ComponentSnapper.PROP_SNAP_HIGHLIGHT_STROKE,Themes.get<EditTheme>().snap.stroke)

		properties.set(OriginIndicator.PROP_SELECTION_COLOR, Themes.get<GraphTheme>().selection.color.foregroundColor)

		properties.set(Handle.PROP_BORDER_COLOR, Themes.get<GraphTheme>().selection.color.foregroundColor)
		properties.set(Handle.PROP_FILL_COLOR, Themes.get<GraphTheme>().selection.color.backgroundColor)

		properties.set(DragEdgePointHighlight.PROP_COLOR, Themes.get<GraphTheme>().selection.color.foregroundColor)

		properties.set(EdgeView.PROP_MIN_EDGE_VIEW_LENGTH, DigitalPortView.LENGTH + 5)
		properties.set(DigitalEdgeView.PROP_WIDE_BUS_STROKE, true)

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
		properties.set(ClockView.PROP_ICON_PATH, "/img/clock.png")
		properties.set(JoystickView.PROP_ICON_PATH, "/img/joystick.png")

		properties.set(AndGateView.PROP_DATA_FLOW_ENABLED, true)
		properties.set(TransistorView.PROP_TRANSISTOR_CIRCLE, true)

		properties.set(LightColor.PROP_DEFAULT_LIGHT_COLOR, LightColor.RED.customName)
		properties.set(DigitalSignalNotation.PROP_DIGITAL_SIGNAL_NOTATION, DigitalSignalNotation.BASE_SUBSCRIPT.customName)
		properties.set(TunnelViewFace.PROP_TUNNEL_FACE, TunnelViewFace.ARROW.customName)

		properties.set(DigitalSignalHistoryDrawer.PROP_FILL_SIGNAL, true)
		properties.set(SymbolStyle.PROP_SYMBOL_STYLE, SymbolStyle.AMERICAN.customName)
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
		typeMap.register("clockControlView", ClockControlView::class)
		typeMap.register("ledView", LEDView::class)
		typeMap.register("RgbLedView", RgbLEDView::class)
		typeMap.register("sevenSegmentDisplayView", SevenSegmentDisplayView::class)
		typeMap.register("bidirectionalSplitterView", BidirectionalSplitterView::class)
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
		typeMap.register("breakView", BreakView::class)
		typeMap.register("pullResistorView", PullResistorView::class)
		typeMap.register("transistorView", TransistorView::class)
		typeMap.register("groundView", GroundView::class)
		typeMap.register("powerView", PowerView::class)
		typeMap.register("joystickView", JoystickView::class)
		typeMap.register("realSwitchView", RealSwitchView::class)
		typeMap.register("bitExtenderView", BitExtenderView::class)

		typeMap.register("graphView", DigitalGraphView::class)
		typeMap.register("dilCase", DilCase::class)
	}

	private fun configureSelectionModels(factory: SelectionModelFactory) {
		factory.register(SelectionDrawingStrategy.REPLACE, DigitalNodeView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, DigitalPortViewComponent::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, DigitalSignalSourceControlView::class) { SelectedColorSelectionModel(it) }

		factory.register(SelectionDrawingStrategy.REPLACE, LabelComponent::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, DigitalEdgeView::class) { EdgeViewReplaceSelectionModel(it as EdgeView<*>) }

		factory.register(SelectionDrawingStrategy.REPLACE, BidirectionalSplitterView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, SplitterView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, ConcentratorView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, ProbeView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, ConstantView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, TunnelView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, BreakView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, PullResistorView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, TransistorView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, GroundView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, PowerView::class) { SelectedColorSelectionModel(it) }

		factory.register(SelectionDrawingStrategy.REPLACE, AndGateView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, OrGateView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, NotGateView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, NandGateView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, NorGateView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, XorGateView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, XnorGateView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, BufferGateView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, TriStateBufferGateView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, DelayGateView::class) { SelectedColorSelectionModel(it) }

		factory.register(SelectionDrawingStrategy.REPLACE, SwitchView::class) { SwitchViewSelectionModel(it as SwitchView) }
		factory.register(SelectionDrawingStrategy.REPLACE, DipSwitchView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, ClockView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, ClockControlView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, CircuitInOutView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, KeyboardView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, TerminalView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, JoystickView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, RealSwitchView::class) { SelectedColorSelectionModel(it) }

		factory.register(SelectionDrawingStrategy.REPLACE, LEDView::class) { LEDViewSelectionModel(it as LEDView) }
		factory.register(SelectionDrawingStrategy.REPLACE, RgbLEDView::class) { LEDViewSelectionModel(it as RgbLEDView) }
		factory.register(SelectionDrawingStrategy.REPLACE, SevenSegmentDisplayView::class) { SevenSegmentDisplayViewSelectionModel(it as SevenSegmentDisplayView) }
		factory.register(SelectionDrawingStrategy.REPLACE, LEDMatrixView::class) { LEDMatrixViewSelectionModel(it as LEDMatrixView) }

		factory.register(SelectionDrawingStrategy.REPLACE, ROMView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, RAMView::class) { SelectedColorSelectionModel(it) }

		factory.register(SelectionDrawingStrategy.REPLACE, RandomView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, BitExtenderView::class) { SelectedColorSelectionModel(it) }

		factory.register(SelectionDrawingStrategy.BELOW, DilCase::class) { RectangularBelowSelectionModel(it as AbstractRectangularComponent) }
		factory.register(SelectionDrawingStrategy.ABOVE, DilCase::class) { RectangularHandleSelectionModel(it as AbstractRectangularComponent) }
		factory.register(SelectionDrawingStrategy.REPLACE, DilCase::class) { RectangularReplaceSelectionModel(it as AbstractRectangularComponent) }
	}

	private fun configureHighlightModels(factory: SelectionModelFactory) {
		factory.register(SelectionDrawingStrategy.BELOW, DigitalEdgeView::class) { EdgeViewBelowSelectionModel(component = it as EdgeView<*>, styleType = EditStyleType.HIGHLIGHT) }

		factory.register(SelectionDrawingStrategy.BELOW, AndGateView::class) { BoxGateViewBelowSelectionModel(component = it as BoxGateView<*>, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, OrGateView::class) { BoxGateViewBelowSelectionModel(component = it as BoxGateView<*>, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, NotGateView::class) { BoxGateViewBelowSelectionModel(component = it as BoxGateView<*>, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, NandGateView::class) { BoxGateViewBelowSelectionModel(component = it as BoxGateView<*>, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, NorGateView::class) { BoxGateViewBelowSelectionModel(component = it as BoxGateView<*>, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, XorGateView::class) { BoxGateViewBelowSelectionModel(component = it as BoxGateView<*>, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, XnorGateView::class) { BoxGateViewBelowSelectionModel(component = it as BoxGateView<*>, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, BufferGateView::class) { BoxGateViewBelowSelectionModel(component = it as BoxGateView<*>, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, TriStateBufferGateView::class) { BoxGateViewBelowSelectionModel(component = it as BoxGateView<*>, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, DelayGateView::class) { BoxGateViewBelowSelectionModel(component = it as BoxGateView<*>, styleType = EditStyleType.HIGHLIGHT) }

		factory.register(SelectionDrawingStrategy.BELOW, RAMView::class) { BoundingBoxBelowSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.BELOW, ROMView::class) { BoundingBoxBelowSelectionModel(it) }
	}

	private fun configureDragDestinationHighlights(registry: DragDestinationHighlightFactoryRegistry) {
		registry.register(DilCase::class) { DilCaseDragDestinationHighlight(it) }
	}

	private fun registerBaseLibraryElements(repository: BaseLibraryElementRepository) {
		repository.register(CONSTANT, "library.element.Constant", { "/img/constant.png" }, ConstantView::class)
		repository.register(SPLITTER, "library.element.Splitter", { "/img/splitter.png" }, SplitterView::class)
		repository.register(CONCENTRATOR, "library.element.Concentrator", { "/img/concentrator.png" }, ConcentratorView::class)
		repository.register(PROBE, "library.element.Probe", { "/img/probe.png" }, ProbeView::class)
		repository.register(TUNNEL, "library.element.Tunnel", { "/img/tunnel.png" }, TunnelView::class)
		repository.register(BREAK, "library.element.Break", { "/img/break.png" }, BreakView::class)
		repository.register(PULL_RESISTOR, "library.element.PullResistor", { "/img/pull-resistor.png" }, PullResistorView::class)
		repository.register(TRANSISTOR, "library.element.Transistor", { "/img/transistor.png" }, TransistorView::class)
		repository.register(GROUND, "library.element.Ground", { "/img/ground.png" }, GroundView::class)
		repository.register(POWER, "library.element.Power", { "/img/power.png" }, PowerView::class)
		repository.register(BIDIRECTIONAL_SPLITTER, "library.element.BidirectionalSplitter", { "/img/splitter.png" }, BidirectionalSplitterView::class)

		repository.register(AND,
			"library.element.AndGate",
			CurrentSymbolStyleToString(mapOf(SymbolStyle.AMERICAN to "/img/and.png", SymbolStyle.EUROPEAN to "/img/and-iec.png"))::evaluate,
			AndGateView::class)
		repository.register(OR,
			"library.element.OrGate",
			CurrentSymbolStyleToString(mapOf(SymbolStyle.AMERICAN to "/img/or.png", SymbolStyle.EUROPEAN to "/img/or-iec.png"))::evaluate,
			OrGateView::class)
		repository.register(NOT,
			"library.element.NotGate",
			CurrentSymbolStyleToString(mapOf(SymbolStyle.AMERICAN to "/img/not.png", SymbolStyle.EUROPEAN to "/img/not-iec.png"))::evaluate,
			NotGateView::class)
		repository.register(NAND,
			"library.element.NandGate",
			CurrentSymbolStyleToString(mapOf(SymbolStyle.AMERICAN to "/img/nand.png", SymbolStyle.EUROPEAN to "/img/nand-iec.png"))::evaluate,
			NandGateView::class)
		repository.register(NOR,
			"library.element.NorGate",
			CurrentSymbolStyleToString(mapOf(SymbolStyle.AMERICAN to "/img/nor.png", SymbolStyle.EUROPEAN to "/img/nor-iec.png"))::evaluate,
			NorGateView::class)
		repository.register(XOR,
			"library.element.XorGate",
			CurrentSymbolStyleToString(mapOf(SymbolStyle.AMERICAN to "/img/xor.png", SymbolStyle.EUROPEAN to "/img/xor-iec.png"))::evaluate,
			XorGateView::class)
		repository.register(XNOR,
			"library.element.XnorGate",
			CurrentSymbolStyleToString(mapOf(SymbolStyle.AMERICAN to "/img/xnor.png", SymbolStyle.EUROPEAN to "/img/xnor-iec.png"))::evaluate,
			XnorGateView::class)
		repository.register(BUFFER,
			"library.element.Buffer",
			CurrentSymbolStyleToString(mapOf(SymbolStyle.AMERICAN to "/img/buffer.png", SymbolStyle.EUROPEAN to "/img/buffer-iec.png"))::evaluate,
			BufferGateView::class)
		repository.register(TRISTATE_BUFFER, "library.element.TriStateBuffer", { "/img/tristate-buffer.png" }, TriStateBufferGateView::class)
		repository.register(DELAY, "library.element.Delay", { "/img/delay.png" }, DelayGateView::class)
		repository.register(INPUT, "library.element.GraphInput", { "/img/input.png" }) {
			CircuitInOutView(model = CircuitInOutImpl(portType = PortType.INPUT))
		}

		repository.register(SWITCH, "library.element.Toggle", { "/img/switch.png" }, SwitchView::class)
		repository.register(DIP_SWITCH, "library.element.DipSwitch", { "/img/dip-switch.png" }, DipSwitchView::class)
		repository.register(CLOCK, "library.element.Clock", { "/img/clock.png" }, ClockView::class)
		repository.register(KEYBOARD, "library.element.Keyboard", { "/img/keyboard.png" }, KeyboardView::class)
		repository.register(TERMINAL, "library.element.Terminal", { "/img/terminal.png" }, TerminalView::class)
		repository.register(OUTPUT, "library.element.GraphOutput", { "/img/output.png" }) {
			CircuitInOutView(model = CircuitInOutImpl(portType = PortType.OUTPUT))
		}
		repository.register(JOYSTICK, "library.element.Joystick", { "/img/joystick.png" }, JoystickView::class)
		repository.register(REAL_SWITCH, "library.element.RealSwitch", { "/img/real-switch.png" }, RealSwitchView::class)

		repository.register(LED, "library.element.LED", { "/img/led.png" }, LEDView::class)
		repository.register(RGB_LED, "library.element.RgbLED", { "/img/rgb-led.png" }, RgbLEDView::class)
		repository.register(SEVEN_SEGMENT_DISPLAY, "library.element.SevenSegmentDisplay", { "/img/7segment.png" }, SevenSegmentDisplayView::class)
		repository.register(LED_MATRIX, "library.element.LEDMatrix", { "/img/led-matrix.png" }, LEDMatrixView::class)

		repository.register(ROM, "library.element.ROM", { "/img/rom.png" }, ROMView::class)
		repository.register(RAM, "library.element.RAM", { "/img/ram.png" }, RAMView::class)

		repository.register(RANDOM, "library.element.Random", { "/img/random.png" }, RandomView::class)
		repository.register(BIT_EXTENDER, "library.element.BitExtender", { "/img/bitextender.png" }, BitExtenderView::class)
	}

	fun fillBaseElementLibrary(library: Library) {
		val fu = LibraryFolder(Translations.getString("library.folder.frequentlyUsed"))
		addLibraryItem(library, BaseLibraryElement(SWITCH), fu)
		addLibraryItem(library, BaseLibraryElement(LED), fu)
		addLibraryItem(library, BaseLibraryElement(INPUT), fu)
		addLibraryItem(library, BaseLibraryElement(OUTPUT), fu)
		addLibraryItem(library, BaseLibraryElement(AND), fu)
		addLibraryItem(library, BaseLibraryElement(OR), fu)
		addLibraryItem(library, BaseLibraryElement(NOT), fu)
		addLibraryItem(library, fu, library)

		val net = LibraryFolder(Translations.getString("library.folder.net"))
		addLibraryItem(library, BaseLibraryElement(CONSTANT), net)
		addLibraryItem(library, BaseLibraryElement(SPLITTER), net)
		addLibraryItem(library, BaseLibraryElement(CONCENTRATOR), net)
		addLibraryItem(library, BaseLibraryElement(PROBE), net)
		addLibraryItem(library, BaseLibraryElement(TUNNEL), net)
		addLibraryItem(library, BaseLibraryElement(BREAK), net)
		addLibraryItem(library, BaseLibraryElement(PULL_RESISTOR), net)
		addLibraryItem(library, BaseLibraryElement(TRANSISTOR), net)
		addLibraryItem(library, BaseLibraryElement(GROUND), net)
		addLibraryItem(library, BaseLibraryElement(POWER), net)
		addLibraryItem(library, BaseLibraryElement(REAL_SWITCH), net)
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
		addLibraryItem(library, BaseLibraryElement(JOYSTICK), input)
		addLibraryItem(library, input, library)

		val output = LibraryFolder(Translations.getString("library.folder.output"))
		addLibraryItem(library, BaseLibraryElement(OUTPUT), output)
		addLibraryItem(library, BaseLibraryElement(LED), output)
		addLibraryItem(library, BaseLibraryElement(RGB_LED), output)
		addLibraryItem(library, BaseLibraryElement(SEVEN_SEGMENT_DISPLAY), output)
		addLibraryItem(library, BaseLibraryElement(LED_MATRIX), output)
		addLibraryItem(library, BaseLibraryElement(TERMINAL), output)
		addLibraryItem(library, output, library)

		val memory = LibraryFolder(Translations.getString("library.folder.memory"))
		addLibraryItem(library, BaseLibraryElement(ROM), memory)
		addLibraryItem(library, BaseLibraryElement(RAM), memory)
		addLibraryItem(library, memory, library)

		val arithmetic = LibraryFolder(Translations.getString("library.folder.arithmetic"))
		addLibraryItem(library, BaseLibraryElement(RANDOM), arithmetic)
		addLibraryItem(library, BaseLibraryElement(BIT_EXTENDER), arithmetic)
		addLibraryItem(library, arithmetic, library)
	}

	private fun addLibraryItem(library: Library, item: LibraryItem, directory: LibraryDirectory) {
		item.bindTo(library)
		directory.add(item)
	}
}