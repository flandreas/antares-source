package ch.scorpion.antares.view.module

import ch.scorpion.antares.AntaresAuthorizations
import ch.scorpion.antares.model.AntaresGraphTypes.Analog
import ch.scorpion.antares.model.AntaresGraphTypes.Digital
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.analog.AnalogCircuitInOut
import ch.scorpion.antares.model.analog.AnalogGraph
import ch.scorpion.antares.model.gate.NonUnaryLogicGateType.*
import ch.scorpion.antares.model.gate.UnaryLogicGateType.Buffer
import ch.scorpion.antares.model.gate.UnaryLogicGateType.Not
import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalNotation
import ch.scorpion.antares.view.*
import ch.scorpion.antares.view.addressable.LookupTableView
import ch.scorpion.antares.view.addressable.RAMView
import ch.scorpion.antares.view.addressable.ROMView
import ch.scorpion.antares.view.analog.*
import ch.scorpion.antares.view.app.AntaresGraphViewService
import ch.scorpion.antares.view.arithmetic.BitExtenderView
import ch.scorpion.antares.view.arithmetic.RandomView
import ch.scorpion.antares.view.container.DigitalPortViewComponent
import ch.scorpion.antares.view.container.DilCase
import ch.scorpion.antares.view.container.DilCaseDragDestinationHighlight
import ch.scorpion.antares.view.figure.*
import ch.scorpion.antares.view.find.DigitalGraphViewSearch
import ch.scorpion.antares.view.gate.*
import ch.scorpion.antares.view.inout.AbstractCircuitInOutView
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.antares.view.input.*
import ch.scorpion.antares.view.metagraph.AntaresMetaGraphService
import ch.scorpion.antares.view.net.*
import ch.scorpion.antares.view.oscilloscope.AntaresOscilloscopeViewFactory
import ch.scorpion.antares.view.oscilloscope.DigitalOscilloscopeProbeNameStrategy
import ch.scorpion.antares.view.output.*
import ch.scorpion.antares.view.port.AbstractAntaresPortView
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
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.base.help.HelpSource
import ch.scorpion.jabbah.base.help.HelpSourceRegistry
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.Style
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.DrawingViewFactory
import ch.scorpion.jabbah.edit.Grid
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.drag.DragDestinationHighlightFactoryRegistry
import ch.scorpion.jabbah.edit.drag.EditDragModule
import ch.scorpion.jabbah.edit.figure.FigureProvider
import ch.scorpion.jabbah.edit.figure.FigureRegistry
import ch.scorpion.jabbah.edit.highlight.EditHighlightModule
import ch.scorpion.jabbah.edit.model.rectangle.AbstractRectangularComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangularBelowSelectionModel
import ch.scorpion.jabbah.edit.model.rectangle.RectangularHandleSelectionModel
import ch.scorpion.jabbah.edit.model.rectangle.RectangularReplaceSelectionModel
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.select.*
import ch.scorpion.jabbah.edit.snap.ComponentSnapper
import ch.scorpion.jabbah.edit.style.EditStyleType
import ch.scorpion.jabbah.edit.style.EditTheme
import ch.scorpion.jabbah.edit.view.AttentionDrawerImpl
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.graph.container.OriginIndicator
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.module.GraphModule
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewFactory
import ch.scorpion.jabbah.graph.view.app.GraphViewAppService
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.DragEdgePointHighlight
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewBelowSelectionModel
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewReplaceSelectionModel
import ch.scorpion.jabbah.graph.view.oscilloscope.AbstractSignalHistoryDrawer
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
	private const val TRANSISTOR_N = "TransistorN"
	private const val TRANSISTOR_P = "TransistorP"
	private const val GROUND = "Ground"
	private const val POWER = "Power"
	private const val WIRE_TAP = "WireTap"
	private const val POWER_ON_RESET = "PowerOnReset"

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
	private const val INPUT_OUTPUT = "InputOutput"
	private const val SWITCH = "Switch"
	private const val DIP_SWITCH = "DipSwitch"
	private const val CLOCK = "Clock"
	private const val KEYBOARD = "Keyboard"
	private const val TERMINAL = "Terminal"
	private const val VIDEO_RAM = "VideoRam"
	private const val JOYSTICK = "Joystick"
	private const val REAL_SWITCH = "RealSwitch"
	private const val DOUBLE_THROW_SWITCH = "DoubleThrowSwitch"

	private const val OUTPUT = "Output"
	private const val OUTPUT_INPUT = "OutputInput"
	private const val LED = "LED"
	private const val RGB_LED = "RgbLED"
	private const val SEVEN_SEGMENT_DISPLAY = "SevenSegmentDisplay"
	private const val SIXTEEN_SEGMENT_DISPLAY = "SixteenSegmentDisplay"
	private const val LED_MATRIX = "LEDMatrix"
	private const val BUZZER = "Buzzer"

	private const val ROM = "ROM"
	private const val RAM = "RAM"
	private const val LUT = "LUT"

	private const val RANDOM = "Random"
	private const val BIT_EXTENDER = "BitExtender"

	// Analog

	private const val LIGHT_BULB = "LightBulb"
	private const val BATTERY = "Battery"
	private const val CURRENT_SOURCE = "CurrentSource"
	private const val RESISTOR = "Resistor"
	private const val ANALOG_SWITCH = "AnalogSwitch"
	private const val ANALOG_GROUND = "AnalogGround"
	private const val ANALOG_TRANSISTOR_N = "AnalogTransistorN"
	private const val ANALOG_TRANSISTOR_P = "AnalogTransistorP"
	private const val ANALOG_INPUT = "AnalogInput"
	private const val ANALOG_OUTPUT = "AnalogOutput"
	private const val ANALOG_POWER = "AnalogPower"

	val currentSymbolStyle: CurrentSymbolStyle by lazy {CurrentSymbolStyle() }

	var analogCircuitCalculator: AnalogCircuitCalculator = KirchhoffAnalogCircuitCalculator

	override fun initialize() {
		Translations.addBundle("antares")

		// Overwritten in order to change the [DrawableDrawer]
		EditModule.drawingViewFactory = DrawingViewFactory { drawing, contextHolder, displayGlobalMessages ->
			val drawingView = DrawingViewImpl(drawing, applicationContextHolder = contextHolder, displayGlobalMessages = displayGlobalMessages)
			drawingView.addDrawableDrawer(OrientableRectangularVerticeViewDrawer())
			drawingView
		}
		EditModule.drawingAppService = AntaresGraphViewService()

		EditModule.attentionDrawerFactory = { signal ->
			if (signal is DigitalSignal) {
				AttentionDrawerImpl(color = Themes.get<GraphTheme>().selection.color.foregroundColor)
			} else {
				AttentionDrawerImpl()
			}
		}

		EditModule.drawingViewSearchFactory = { DigitalGraphViewSearch() }

		GraphViewModule.graphViewFactory = object : GraphViewFactory {

			override fun create(name: TranslatableText?): GraphView =
				DigitalGraphView(name ?: TranslatableText(Translations.getString("graph.name.unknown")))

			override fun create(model: Graph): GraphView =
				when (model.type) {
					Digital -> DigitalGraphView(model as DigitalGraph)
					Analog -> AnalogGraphView(model as AnalogGraph)
					else -> GraphViewImpl(model)
				}
		}

		GraphViewModule.graphViewAppService = EditModule.drawingAppService as GraphViewAppService
		GraphViewModule.portViewFactory = DigitalPortViewFactory(DrawStyleModule.styleProvider)
		GraphViewModule.oscilloscopeViewFactory = AntaresOscilloscopeViewFactory()
		GraphViewModule.oscilloscopeProbeNameStrategy = DigitalOscilloscopeProbeNameStrategy()
		val edgeViewFactory = AntaresEdgeViewFactory(
			DrawStyleModule.styleProvider,
			{ GraphViewModule.edgeToPortConnector },
			{ GraphViewModule.dragEdgeViewOriginConnector },
			{ GraphViewModule.dragEdgeViewDestinationConnector })
		GraphViewModule.setEdgeViewFactory(edgeViewFactory)
		GraphViewModule.setNodeViewFactory(AntaresNodeViewFactory(
			DrawStyleModule.styleProvider))
		GraphViewModule.graphNavigationViewControllerExtension = { AntaresGraphNavigationViewControllerExtension(it) }
		GraphViewModule.graphViewExecutionAnimationFactory = AntaresExecutionAnimationFactory()
		GraphViewModule.metaGraphService = AntaresMetaGraphService()

		GraphModule.require()
		AnimationModule.require()
		AntaresModelModule.require()

		customizeProperties(BaseModule.properties)

		configureTypeMap(IOModule.typeMap)
		configureSelectionModels(EditSelectModule.selectionModelFactory)
		configureHighlightModels(EditHighlightModule.highlightModelFactory)
		configureDragDestinationHighlights(EditDragModule.dragDestinationHighlightFactoryRegistry)

		registerBaseLibraryElements(LibraryModule.baseLibraryElementRepository)

		registerFigures()
		registerHelpSources()

		AntaresAuthorizations.define()
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

		properties.set(EdgeView.PROP_MIN_EDGE_VIEW_LENGTH, AbstractAntaresPortView.LENGTH + 5)
		properties.set(DigitalEdgeView.PROP_WIDE_BUS_STROKE, true)

		properties.set(AbstractCircuitInOutView.PROP_INPUT_ICON_PATH, "/img/input.png")
		properties.set(AbstractCircuitInOutView.PROP_OUTPUT_ICON_PATH, "/img/output.png")
		properties.set(AbstractCircuitInOutView.PROP_INOUT_ICON_PATH, "/img/inout.png")
		properties.set(SwitchView.PROP_ICON_PATH, "/img/switch.png")
		properties.set(DipSwitchView.PROP_ICON_PATH, "/img/dip-switch.png")
		properties.set(ProbeView.PROP_ICON_PATH, "/img/probe.png")
		properties.set(LEDView.PROP_ICON_PATH, "/img/led.png")
		properties.set(RgbLEDView.PROP_ICON_PATH, "/img/rgb-led.png")
		properties.set(LEDMatrixView.PROP_ICON_PATH, "/img/led-matrix.png")
		properties.set(SevenSegmentDisplayView.PROP_ICON_PATH, "/img/7segment.png")
		properties.set(SixteenSegmentDisplayView.PROP_ICON_PATH, "/img/16segment.png")
		properties.set(TerminalView.PROP_ICON_PATH, "/img/terminal.png")
		properties.set(KeyboardView.PROP_ICON_PATH, "/img/keyboard.png")
		properties.set(ClockView.PROP_ICON_PATH, "/img/clock.png")
		properties.set(JoystickView.PROP_ICON_PATH, "/img/joystick.png")
		properties.set(VideoRamView.PROP_ICON_PATH, "/img/videoram.png")

		properties.set(LogicGateView.PROP_DATA_FLOW_ENABLED, true)
		properties.set(AbstractTransistorView.PROP_TRANSISTOR_CIRCLE, true)

		properties.set(LightColor.PROP_DEFAULT_LIGHT_COLOR, LightColor.RED.customName)
		properties.set(DigitalSignalNotation.PROP_DIGITAL_SIGNAL_NOTATION, DigitalSignalNotation.BASE_SUBSCRIPT.customName)
		properties.set(TunnelViewFace.PROP_TUNNEL_FACE, TunnelViewFace.ARROW.customName)

		properties.set(AbstractSignalHistoryDrawer.PROP_FILL_SIGNAL, true)
		properties.set(SymbolStyle.PROP_SYMBOL_STYLE, SymbolStyle.AMERICAN.customName)
		properties.set(TransistorViewSymbol.PROP_TRANSISTOR_SYMBOL, TransistorViewSymbol.Bulk.customName)
	}

	private fun configureTypeMap(typeMap: TypeMap) {
		typeMap.register("circuitInOutView", DigitalCircuitInOutView::class)
		typeMap.register("digitalEdgeView", DigitalEdgeView::class)
		typeMap.register("digitalNodeView", DigitalNodeView::class)
		typeMap.register("digitalPortView", DigitalPortView::class)
		typeMap.register("digitalPortViewComp", DigitalPortViewComponent::class)
		typeMap.register("digitalSignalSourceCV", DigitalSignalSourceControlView::class)

		typeMap.register("andGateView", { it is LogicGateView && it.model.logicGateType == And }) { LogicGateView.andGateView() }
		typeMap.register("nandGateView", { it is LogicGateView && it.model.logicGateType == Nand }) { LogicGateView.nandGateView() }
		typeMap.register("orGateView", { it is LogicGateView && it.model.logicGateType == Or }) { LogicGateView.orGateView() }
		typeMap.register("norGateView", { it is LogicGateView && it.model.logicGateType == Nor }) { LogicGateView.norGateView() }
		typeMap.register("xorGateView", { it is LogicGateView && it.model.logicGateType == Xor }) { LogicGateView.xorGateView() }
		typeMap.register("xnorGateView", { it is LogicGateView && it.model.logicGateType == Xnor }) { LogicGateView.xnorGateView() }
		typeMap.register("notGateView", { it is LogicGateView && it.model.logicGateType == Not }) { LogicGateView.notGateView() }
		typeMap.register("bufferGateView", { it is LogicGateView && it.model.logicGateType == Buffer }) { LogicGateView.bufferGateView() }

		typeMap.register("triStateBufferGateView", TriStateBufferGateView::class)

		typeMap.register("switchView", SwitchView::class)
		typeMap.register("dipSwitchView", DipSwitchView::class)
		typeMap.register("clockView", ClockView::class)
		typeMap.register("clockControlView", ClockControlView::class)
		typeMap.register("ledView", LEDView::class)
		typeMap.register("RgbLedView", RgbLEDView::class)
		typeMap.register("sevenSegmentDisplayView", SevenSegmentDisplayView::class)
		typeMap.register("sixteenSegmentDisplayView", SixteenSegmentDisplayView::class)
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
		typeMap.register("buzzerView", BuzzerView::class)
		typeMap.register("videoRamView", VideoRamView::class)
		typeMap.register("doubleThrowSwitchView", DoubleThrowSwitchView::class)
		typeMap.register("lookupTableView", LookupTableView::class)
		typeMap.register("wireTapView", WireTapView::class)
		typeMap.register("powerOnResetView", PowerOnResetView::class)

		typeMap.register("graphView", DigitalGraphView::class)
		typeMap.register("dilCase", DilCase::class)

		typeMap.register("andGateShape", AndGateFigure::class)
		typeMap.register("orGateShape", OrGateFigure::class)
		typeMap.register("notGateShape", NotGateFigure::class)

		// Analog

		typeMap.register("analogGraphView", AnalogGraphView::class)
		typeMap.register("analogEdgeView", AnalogEdgeView::class)
		typeMap.register("lightBulbView", LightBulbView::class)
		typeMap.register("batteryView", BatteryView::class)
		typeMap.register("currentSourceView", CurrentSourceView::class)
		typeMap.register("resistorView", ResistorView::class)
		typeMap.register("analogSwitchView", AnalogSwitchView::class)
		typeMap.register("analogNodeView", AnalogNodeView::class)
		typeMap.register("analogGroundView", AnalogGroundView::class)
		typeMap.register("analogTransistorView", AnalogTransistorView::class)
		typeMap.register("analogCircuitInOutView", AnalogCircuitInOutView::class)
		typeMap.register("analogPowerView", AnalogPowerView::class)
	}

	private fun configureSelectionModels(factory: SelectionModelFactory) {
		factory.register(SelectionDrawingStrategy.REPLACE, DigitalNodeView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, DigitalPortViewComponent::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, DigitalSignalSourceControlView::class) { SelectedColorSelectionModel(it) }

		factory.register(SelectionDrawingStrategy.REPLACE, LabelComponent::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, DigitalEdgeView::class) { EdgeViewReplaceSelectionModel(it as EdgeView<*>) }
		factory.register(SelectionDrawingStrategy.REPLACE, AnalogEdgeView::class) { EdgeViewReplaceSelectionModel(it as EdgeView<*>) }

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
		factory.register(SelectionDrawingStrategy.REPLACE, WireTapView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, PowerOnResetView::class) { SelectedColorSelectionModel(it) }

		factory.register(SelectionDrawingStrategy.REPLACE, LogicGateView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, TriStateBufferGateView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, DelayGateView::class) { SelectedColorSelectionModel(it) }

		factory.register(SelectionDrawingStrategy.REPLACE, SwitchView::class) { SwitchViewSelectionModel(it as SwitchView) }
		factory.register(SelectionDrawingStrategy.REPLACE, DipSwitchView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, ClockView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, ClockControlView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, DigitalCircuitInOutView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, KeyboardView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, TerminalView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, JoystickView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, RealSwitchView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, VideoRamView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, DoubleThrowSwitchView::class) { SelectedColorSelectionModel(it) }

		factory.register(SelectionDrawingStrategy.REPLACE, LEDView::class) { LEDViewSelectionModel(it as LEDView) }
		factory.register(SelectionDrawingStrategy.REPLACE, RgbLEDView::class) { LEDViewSelectionModel(it as RgbLEDView) }
		factory.register(SelectionDrawingStrategy.REPLACE, SevenSegmentDisplayView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, SixteenSegmentDisplayView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, LEDMatrixView::class) { LEDMatrixViewSelectionModel(it as LEDMatrixView) }
		factory.register(SelectionDrawingStrategy.REPLACE, BuzzerView::class) { SelectedColorSelectionModel(it) }

		factory.register(SelectionDrawingStrategy.REPLACE, ROMView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, RAMView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, LookupTableView::class) { SelectedColorSelectionModel(it) }

		factory.register(SelectionDrawingStrategy.REPLACE, RandomView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, BitExtenderView::class) { SelectedColorSelectionModel(it) }

		factory.register(SelectionDrawingStrategy.BELOW, DilCase::class) { RectangularBelowSelectionModel(it as AbstractRectangularComponent) }
		factory.register(SelectionDrawingStrategy.ABOVE, DilCase::class) { RectangularHandleSelectionModel(it as AbstractRectangularComponent) }
		factory.register(SelectionDrawingStrategy.REPLACE, DilCase::class) { RectangularReplaceSelectionModel(it as AbstractRectangularComponent) }

		factory.register(SelectionDrawingStrategy.REPLACE, AndGateFigure::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, OrGateFigure::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, NotGateFigure::class) { SelectedColorSelectionModel(it) }

		// Analog
		factory.register(SelectionDrawingStrategy.REPLACE, AnalogNodeView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, LightBulbView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, BatteryView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, CurrentSourceView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, ResistorView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, AnalogSwitchView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, AnalogGroundView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, AnalogTransistorView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, AnalogCircuitInOutView::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, AnalogPowerView::class) { SelectedColorSelectionModel(it) }
	}

	private fun configureHighlightModels(factory: SelectionModelFactory) {
		factory.register(SelectionDrawingStrategy.BELOW, DigitalEdgeView::class) { EdgeViewBelowSelectionModel(component = it as EdgeView<*>, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, LogicGateView::class) { BoxGateViewBelowSelectionModel(component = it as BoxGateView<*>, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, TriStateBufferGateView::class) { BoxGateViewBelowSelectionModel(component = it as BoxGateView<*>, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, DelayGateView::class) { BoxGateViewBelowSelectionModel(component = it as BoxGateView<*>, styleType = EditStyleType.HIGHLIGHT) }

		factory.register(SelectionDrawingStrategy.BELOW, RAMView::class) { BoundingBoxBelowSelectionModel(it, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, ROMView::class) { BoundingBoxBelowSelectionModel(it, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, LookupTableView::class) { BoundingBoxBelowSelectionModel(it, styleType = EditStyleType.HIGHLIGHT) }

		factory.register(SelectionDrawingStrategy.BELOW, PullResistorView::class) { BoundingBoxBelowSelectionModel(it, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, TransistorView::class) { TransistorViewBelowSelectionModel(it as TransistorView, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, GroundView::class) { BoundingBoxBelowSelectionModel(it, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, PowerView::class) { BoundingBoxBelowSelectionModel(it, styleType = EditStyleType.HIGHLIGHT) }
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
		// Backward compatibility
		repository.register(TRANSISTOR, "library.element.Transistor", { "/img/transistor.png" }, TransistorView::class)
		repository.register(TRANSISTOR_N, "library.element.Transistor.nType", { "/img/transistor.png" }, HelpId(TransistorView::class.simpleName!!)) {
			TransistorView(TransistorType.N)
		}
		repository.register(TRANSISTOR_P, "library.element.Transistor.pType", { "/img/transistor.png" }, null) {
			TransistorView(TransistorType.P)
		}
		repository.register(GROUND, "library.element.Ground", { "/img/ground.png" }, GroundView::class)
		repository.register(POWER, "library.element.Power", { "/img/power.png" }, PowerView::class)
		repository.register(BIDIRECTIONAL_SPLITTER, "library.element.BidirectionalSplitter", { "/img/splitter.png" }, BidirectionalSplitterView::class)
		repository.register(WIRE_TAP, "library.element.WireTap", { "/img/wire-tap.png" }, WireTapView::class)
		repository.register(POWER_ON_RESET, "library.element.PowerOnReset", { "/img/power-on-reset.png" }, PowerOnResetView::class)

		repository.register(AND,
			"library.element.AndGate",
			CurrentSymbolStyleToString(mapOf(
				SymbolStyle.AMERICAN to "/img/and.png",
				SymbolStyle.EUROPEAN to "/img/and-iec.png",
				SymbolStyle.VERBOSE to "/img/and-iec.png"
			))::evaluate,
			And.helpId) { LogicGateView.andGateView() }
		repository.register(OR,
			"library.element.OrGate",
			CurrentSymbolStyleToString(mapOf(
				SymbolStyle.AMERICAN to "/img/or.png",
				SymbolStyle.EUROPEAN to "/img/or-iec.png",
				SymbolStyle.VERBOSE to "/img/or-iec.png"
			))::evaluate,
			Or.helpId) { LogicGateView.orGateView() }
		repository.register(NOT,
			"library.element.NotGate",
			CurrentSymbolStyleToString(mapOf(
				SymbolStyle.AMERICAN to "/img/not.png",
				SymbolStyle.EUROPEAN to "/img/not-iec.png",
				SymbolStyle.VERBOSE to "/img/not-iec.png"
			))::evaluate,
			Not.helpId) { LogicGateView.notGateView() }
		repository.register(NAND,
			"library.element.NandGate",
			CurrentSymbolStyleToString(mapOf(
				SymbolStyle.AMERICAN to "/img/nand.png",
				SymbolStyle.EUROPEAN to "/img/nand-iec.png",
				SymbolStyle.VERBOSE to "/img/nand-iec.png"
			))::evaluate,
			Nand.helpId) { LogicGateView.nandGateView() }
		repository.register(NOR,
			"library.element.NorGate",
			CurrentSymbolStyleToString(mapOf(
				SymbolStyle.AMERICAN to "/img/nor.png",
				SymbolStyle.EUROPEAN to "/img/nor-iec.png",
				SymbolStyle.VERBOSE to "/img/nor-iec.png"
			))::evaluate,
			Nor.helpId) { LogicGateView.norGateView() }
		repository.register(XOR,
			"library.element.XorGate",
			CurrentSymbolStyleToString(mapOf(
				SymbolStyle.AMERICAN to "/img/xor.png",
				SymbolStyle.EUROPEAN to "/img/xor-iec.png",
				SymbolStyle.VERBOSE to "/img/xor-iec.png"
			))::evaluate,
			Xor.helpId) { LogicGateView.xorGateView() }
		repository.register(XNOR,
			"library.element.XnorGate",
			CurrentSymbolStyleToString(mapOf(
				SymbolStyle.AMERICAN to "/img/xnor.png",
				SymbolStyle.EUROPEAN to "/img/xnor-iec.png",
				SymbolStyle.VERBOSE to "/img/xnor-iec.png"
			))::evaluate,
			Xnor.helpId) { LogicGateView.xnorGateView() }
		repository.register(BUFFER,
			"library.element.Buffer",
			CurrentSymbolStyleToString(mapOf(
				SymbolStyle.AMERICAN to "/img/buffer.png",
				SymbolStyle.EUROPEAN to "/img/buffer-iec.png",
				SymbolStyle.VERBOSE to "/img/buffer-iec.png"
			))::evaluate,
			Buffer.helpId) { LogicGateView.bufferGateView() }
		repository.register(TRISTATE_BUFFER, "library.element.TriStateBuffer", { "/img/tristate-buffer.png" }, TriStateBufferGateView::class)
		repository.register(DELAY, "library.element.Delay", { "/img/delay.png" }, DelayGateView::class)
		repository.register(INPUT, "library.element.GraphInput", { "/img/input.png" }, HelpId(DigitalCircuitInOutView::class.simpleName!!)) {
			DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INPUT))
		}
		repository.register(INPUT_OUTPUT, "library.element.GraphInOut", { "/img/inout.png" }, HelpId(DigitalCircuitInOutView::class.simpleName!!)) {
			val inoutView = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INOUT))
			inoutView.orientation = Direction.WEST
			inoutView
		}

		repository.register(SWITCH, "library.element.Toggle", { "/img/switch.png" }, SwitchView::class)
		repository.register(DIP_SWITCH, "library.element.DipSwitch", { "/img/dip-switch.png" }, DipSwitchView::class)
		repository.register(CLOCK, "library.element.Clock", { "/img/clock.png" }, ClockView::class)
		repository.register(KEYBOARD, "library.element.Keyboard", { "/img/keyboard.png" }, KeyboardView::class)
		repository.register(TERMINAL, "library.element.Terminal", { "/img/terminal.png" }, TerminalView::class)
		repository.register(VIDEO_RAM, "library.element.VideoRam", { "/img/videoram.png" }, VideoRamView::class)
		repository.register(OUTPUT, "library.element.GraphOutput", { "/img/output.png" }, HelpId(DigitalCircuitInOutView::class.simpleName!!)) {
			DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.OUTPUT))
		}
		repository.register(OUTPUT_INPUT, "library.element.GraphInOut", { "/img/inout.png" }, HelpId(DigitalCircuitInOutView::class.simpleName!!)) {
			val inoutView = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INOUT))
			inoutView.orientation = Direction.EAST
			inoutView
		}
		repository.register(JOYSTICK, "library.element.Joystick", { "/img/joystick.png" }, JoystickView::class)
		repository.register(REAL_SWITCH, "library.element.RealSwitch", { "/img/real-switch.png" }, RealSwitchView::class)
		repository.register(DOUBLE_THROW_SWITCH, "library.element.DoubleThrowSwitch", { "/img/double-throw-switch.png" }, DoubleThrowSwitchView::class)

		repository.register(LED, "library.element.LED", { "/img/led.png" }, LEDView::class)
		repository.register(RGB_LED, "library.element.RgbLED", { "/img/rgb-led.png" }, RgbLEDView::class)
		repository.register(SEVEN_SEGMENT_DISPLAY, "library.element.SevenSegmentDisplay", { "/img/7segment.png" }, SevenSegmentDisplayView::class)
		repository.register(SIXTEEN_SEGMENT_DISPLAY, "library.element.SixteenSegmentDisplay", { "/img/16segment.png" }, SixteenSegmentDisplayView::class)
		repository.register(LED_MATRIX, "library.element.LEDMatrix", { "/img/led-matrix.png" }, LEDMatrixView::class)
		repository.register(BUZZER, "library.element.Buzzer", { "/img/buzzer.png" }, BuzzerView::class)

		repository.register(ROM, "library.element.ROM", { "/img/rom.png" }, ROMView::class)
		repository.register(RAM, "library.element.RAM", { "/img/ram.png" }, RAMView::class)
		repository.register(LUT, "library.element.LookupTable", { "/img/lut.png" }, LookupTableView::class)

		repository.register(RANDOM, "library.element.Random", { "/img/random.png" }, RandomView::class)
		repository.register(BIT_EXTENDER, "library.element.BitExtender", { "/img/bitextender.png" }, BitExtenderView::class)

		// Analog

		repository.register(LIGHT_BULB, "library.element.LightBulb", { "/img/led.png" }, LightBulbView::class)
		repository.register(BATTERY, "library.element.Battery", { "/img/led.png" }, BatteryView::class)
		repository.register(CURRENT_SOURCE, "library.element.CurrentSource", { "/img/led.png" }, CurrentSourceView::class)
		repository.register(RESISTOR, "library.element.Resistor", { "/img/led.png" }, ResistorView::class)
		repository.register(ANALOG_SWITCH, "library.element.AnalogSwitch", { "/img/led.png" }, AnalogSwitchView::class)
		repository.register(ANALOG_GROUND, "library.element.AnalogGround", { "/img/led.png" }, AnalogGroundView::class)
		repository.register(ANALOG_TRANSISTOR_N, "library.element.AnalogTransistor.nType", { "/img/transistor.png" }, HelpId(AbstractTransistorView::class.simpleName!!)) {
			AnalogTransistorView(TransistorType.N)
		}
		repository.register(ANALOG_TRANSISTOR_P, "library.element.AnalogTransistor.pType", { "/img/transistor.png" }, HelpId(AbstractTransistorView::class.simpleName!!)) {
			AnalogTransistorView(TransistorType.P)
		}
		repository.register(ANALOG_INPUT, "library.element.GraphInput", { "/img/input.png" }, HelpId(AnalogCircuitInOutView::class.simpleName!!)) {
			AnalogCircuitInOutView(model = AnalogCircuitInOut(portType = PortType.INPUT))
		}
		repository.register(ANALOG_OUTPUT, "library.element.GraphOutput", { "/img/output.png" }, HelpId(AnalogCircuitInOutView::class.simpleName!!)) {
			AnalogCircuitInOutView(model = AnalogCircuitInOut(portType = PortType.OUTPUT))
		}
		repository.register(ANALOG_POWER, "library.element.AnalogPower", { "/img/power.png" }, AnalogPowerView::class)
	}

	fun fillBaseElementLibrary(library: Library) {
		val fu = LibraryFolder(Translations.getString("library.folder.frequentlyUsed"))
		addLibraryItem(library, BaseLibraryElement(Digital, SWITCH), fu)
		addLibraryItem(library, BaseLibraryElement(Digital, LED), fu)
		addLibraryItem(library, BaseLibraryElement(Digital, INPUT), fu)
		addLibraryItem(library, BaseLibraryElement(Digital, OUTPUT), fu)
		addLibraryItem(library, BaseLibraryElement(Digital, AND), fu)
		addLibraryItem(library, BaseLibraryElement(Digital, OR), fu)
		addLibraryItem(library, BaseLibraryElement(Digital, NOT), fu)
		addLibraryItem(library, fu, library)

		val net = LibraryFolder(Translations.getString("library.folder.net"))
		addLibraryItem(library, BaseLibraryElement(Digital, CONSTANT), net)
		addLibraryItem(library, BaseLibraryElement(Digital, SPLITTER), net)
		addLibraryItem(library, BaseLibraryElement(Digital, CONCENTRATOR), net)
		addLibraryItem(library, BaseLibraryElement(Digital, BIDIRECTIONAL_SPLITTER), net)
		addLibraryItem(library, BaseLibraryElement(Digital, PROBE), net)
		addLibraryItem(library, BaseLibraryElement(Digital, TUNNEL), net)
		addLibraryItem(library, BaseLibraryElement(Digital, BREAK), net)
		addLibraryItem(library, BaseLibraryElement(Digital, PULL_RESISTOR), net)
		addLibraryItem(library, BaseLibraryElement(Digital, TRANSISTOR_P), net)
		addLibraryItem(library, BaseLibraryElement(Digital, TRANSISTOR_N), net)
		addLibraryItem(library, BaseLibraryElement(Digital, GROUND), net)
		addLibraryItem(library, BaseLibraryElement(Digital, POWER), net)
		addLibraryItem(library, BaseLibraryElement(Digital, REAL_SWITCH), net)
		addLibraryItem(library, BaseLibraryElement(Digital, DOUBLE_THROW_SWITCH), net)
		addLibraryItem(library, BaseLibraryElement(Digital, WIRE_TAP), net)
		addLibraryItem(library, net, library)

		val base = LibraryFolder(Translations.getString("library.folder.baseElements"))
		addLibraryItem(library, BaseLibraryElement(Digital, AND), base)
		addLibraryItem(library, BaseLibraryElement(Digital, OR), base)
		addLibraryItem(library, BaseLibraryElement(Digital, NOT), base)
		addLibraryItem(library, BaseLibraryElement(Digital, NAND), base)
		addLibraryItem(library, BaseLibraryElement(Digital, NOR), base)
		addLibraryItem(library, BaseLibraryElement(Digital, XOR), base)
		addLibraryItem(library, BaseLibraryElement(Digital, XNOR), base)
		addLibraryItem(library, BaseLibraryElement(Digital, BUFFER), base)
		addLibraryItem(library, BaseLibraryElement(Digital, TRISTATE_BUFFER), base)
		addLibraryItem(library, BaseLibraryElement(Digital, DELAY), base)
		addLibraryItem(library, base, library)

		val input = LibraryFolder(Translations.getString("library.folder.input"))
		addLibraryItem(library, BaseLibraryElement(Digital, INPUT), input)
		addLibraryItem(library, BaseLibraryElement(Digital, INPUT_OUTPUT), input)
		addLibraryItem(library, BaseLibraryElement(Digital, SWITCH), input)
		addLibraryItem(library, BaseLibraryElement(Digital, DIP_SWITCH), input)
		addLibraryItem(library, BaseLibraryElement(Digital, CLOCK), input)
		addLibraryItem(library, BaseLibraryElement(Digital, KEYBOARD), input)
		addLibraryItem(library, BaseLibraryElement(Digital, JOYSTICK), input)
		addLibraryItem(library, input, library)

		val output = LibraryFolder(Translations.getString("library.folder.output"))
		addLibraryItem(library, BaseLibraryElement(Digital, OUTPUT), output)
		addLibraryItem(library, BaseLibraryElement(Digital, OUTPUT_INPUT), output)
		addLibraryItem(library, BaseLibraryElement(Digital, LED), output)
		addLibraryItem(library, BaseLibraryElement(Digital, RGB_LED), output)
		addLibraryItem(library, BaseLibraryElement(Digital, SEVEN_SEGMENT_DISPLAY), output)
		addLibraryItem(library, BaseLibraryElement(Digital, LED_MATRIX), output)
		addLibraryItem(library, BaseLibraryElement(Digital, TERMINAL), output)
		addLibraryItem(library, BaseLibraryElement(Digital, BUZZER), output)
		addLibraryItem(library, BaseLibraryElement(Digital, VIDEO_RAM), output)
		addLibraryItem(library, output, library)

		val memory = LibraryFolder(Translations.getString("library.folder.memory"))
		addLibraryItem(library, BaseLibraryElement(Digital, ROM), memory)
		addLibraryItem(library, BaseLibraryElement(Digital, RAM), memory)
		addLibraryItem(library, BaseLibraryElement(Digital, LUT), memory)
		addLibraryItem(library, memory, library)

		val arithmetic = LibraryFolder(Translations.getString("library.folder.arithmetic"))
		addLibraryItem(library, BaseLibraryElement(Digital, RANDOM), arithmetic)
		addLibraryItem(library, BaseLibraryElement(Digital, BIT_EXTENDER), arithmetic)
		addLibraryItem(library, arithmetic, library)

		val analog = LibraryFolder(Translations.getString("library.folder.analog"))
		addLibraryItem(library, BaseLibraryElement(Analog, LIGHT_BULB), analog)
		addLibraryItem(library, BaseLibraryElement(Analog, BATTERY), analog)
		addLibraryItem(library, BaseLibraryElement(Analog, RESISTOR), analog)
		addLibraryItem(library, BaseLibraryElement(Analog, ANALOG_SWITCH), analog)
		addLibraryItem(library, BaseLibraryElement(Analog, ANALOG_GROUND), analog)
		addLibraryItem(library, BaseLibraryElement(Analog, ANALOG_TRANSISTOR_N), analog)
		addLibraryItem(library, BaseLibraryElement(Analog, ANALOG_TRANSISTOR_P), analog)
		addLibraryItem(library, BaseLibraryElement(Analog, ANALOG_INPUT), analog)
		addLibraryItem(library, BaseLibraryElement(Analog, ANALOG_OUTPUT), analog)
		addLibraryItem(library, BaseLibraryElement(Analog, ANALOG_POWER), analog)
	}

	private fun addLibraryItem(library: Library, item: LibraryItem, directory: LibraryDirectory) {
		item.bindTo(library)
		directory.add(item)
	}

	private fun registerFigures() {
		FigureRegistry.registerDefaultGeometricalFigures()

		FigureRegistry.registerGroup(Translations.getString("antares.figureGroup.circuitSymbols")).apply {
			register(FigureProvider(AndGateFigure.TYPE) { AndGateFigure() })
			register(FigureProvider(OrGateFigure.TYPE) { OrGateFigure() })
			register(FigureProvider(NotGateFigure.TYPE) { NotGateFigure() })
			register(FigureProvider(Translations.getString("antares.figure.multiplexer")) { createMultiplexerFigure() })
			register(FigureProvider(Translations.getString("antares.figure.demultiplexer")) { createDemultiplexerFigure() })
			register(FigureProvider(Translations.getString("antares.figure.alu")) { createAluFigure() })
			register(FigureProvider(DilCase.TYPE) { DilCase() })
		}
	}

	private fun registerHelpSources() {
		val base = "/base-library"
		with (HelpSourceRegistry) {
			register(HelpId(ConstantView::class.simpleName!!), HelpSource("$base/constant"))
			register(HelpId(SplitterView::class.simpleName!!), HelpSource("$base/splitter"))
			register(HelpId(ConcentratorView::class.simpleName!!), HelpSource("$base/combiner"))
			register(HelpId(ProbeView::class.simpleName!!), HelpSource("$base/probe"))
			register(HelpId(TunnelView::class.simpleName!!), HelpSource("$base/tunnel"))
			register(HelpId(BreakView::class.simpleName!!), HelpSource("$base/breakpoint"))
			register(HelpId(PullResistorView::class.simpleName!!), HelpSource("$base/pull-resistor"))
			register(HelpId(TransistorView::class.simpleName!!), HelpSource("$base/transistor"))

			register(HelpId(GroundView::class.simpleName!!), HelpSource("$base/ground"))
			register(HelpId(PowerView::class.simpleName!!), HelpSource("$base/power"))
			register(HelpId(BidirectionalSplitterView::class.simpleName!!), HelpSource("$base/bidi-splitter"))
			register(HelpId(WireTapView::class.simpleName!!), HelpSource("$base/wire-tap"))
			register(HelpId(PowerOnResetView::class.simpleName!!), HelpSource("$base/powerOn-reset"))

			register(And.helpId, HelpSource("$base/and"))
			register(Nor.helpId, HelpSource("$base/or"))
			register(Not.helpId, HelpSource("$base/not"))
			register(Nand.helpId, HelpSource("$base/nand"))
			register(Nor.helpId, HelpSource("$base/nor"))
			register(Xor.helpId, HelpSource("$base/xor"))
			register(Xnor.helpId, HelpSource("$base/xnor"))
			register(Buffer.helpId, HelpSource("$base/buffer"))

			register(HelpId(TriStateBufferGateView::class.simpleName!!), HelpSource("$base/tristate-buffer"))
			register(HelpId(DelayGateView::class.simpleName!!), HelpSource("$base/delay"))
			register(HelpId(DigitalCircuitInOutView::class.simpleName!!), HelpSource("$base/port"))

			register(HelpId(SwitchView::class.simpleName!!), HelpSource("$base/switch"))
			register(HelpId(DipSwitchView::class.simpleName!!), HelpSource("$base/dip-switch"))
			register(HelpId(ClockView::class.simpleName!!), HelpSource("$base/clock"))
			register(HelpId(KeyboardView::class.simpleName!!), HelpSource("$base/keyboard"))
			register(HelpId(TerminalView::class.simpleName!!), HelpSource("$base/terminal"))
			register(HelpId(VideoRamView::class.simpleName!!), HelpSource("$base/video-ram"))
			register(HelpId(JoystickView::class.simpleName!!), HelpSource("$base/joystick"))
			register(HelpId(RealSwitchView::class.simpleName!!), HelpSource("$base/real-switch"))
			register(HelpId(DoubleThrowSwitchView::class.simpleName!!), HelpSource("$base/double-throw-switch"))

			register(HelpId(LEDView::class.simpleName!!), HelpSource("$base/led"))
			register(HelpId(RgbLEDView::class.simpleName!!), HelpSource("$base/rgb-led"))
			register(HelpId(SevenSegmentDisplayView::class.simpleName!!), HelpSource("$base/7segment"))
			register(HelpId(SixteenSegmentDisplayView::class.simpleName!!), HelpSource("$base/16segment"))
			register(HelpId(LEDMatrixView::class.simpleName!!), HelpSource("$base/led-matrix"))
			register(HelpId(BuzzerView::class.simpleName!!), HelpSource("$base/buzzer"))

			register(HelpId(ROMView::class.simpleName!!), HelpSource("$base/rom"))
			register(HelpId(RAMView::class.simpleName!!), HelpSource("$base/ram"))
			register(HelpId(LookupTableView::class.simpleName!!), HelpSource("$base/lut"))

			register(HelpId(RandomView::class.simpleName!!), HelpSource("$base/random"))
			register(HelpId(BitExtenderView::class.simpleName!!), HelpSource("$base/bit-extender"))
		}
	}
}