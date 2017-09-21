package ch.scorpion.antares.view.module

import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.script.AntaresScriptGateway
import ch.scorpion.antares.view.*
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
import ch.scorpion.jabbah.draw.style.*
import ch.scorpion.jabbah.edit.Grid
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.select.Handle
import ch.scorpion.jabbah.edit.select.SelectionModelFactory
import ch.scorpion.jabbah.edit.snap.ComponentSnapper
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.container.OriginIndicator
import ch.scorpion.jabbah.graph.view.editor.AutoConnectorHighlight
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewBelowSelectionModel
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.view.CurrentGraphViewAnimationType
import ch.scorpion.jabbah.graph.view.net.edge.DragEdgePointHighlight
import ch.scorpion.jabbah.graph.view.style.GraphTheme

/**
 * Module definitions for the [ch.scorpion.antares.view]
 */
object AntaresViewModule : AbstractModule() {

    val currentSymbolStyle: CurrentSymbolStyle = CurrentSymbolStyle()
    val currentGraphViewAnimationType: CurrentGraphViewAnimationType by lazy { CurrentGraphViewAnimationType() }

    override fun initialize() {
        Translations.addBundle("antares")

        // Overwritten in order to change the [DrawableDrawer]
        EditModule.drawingViewFactory = {d,c ->
            val drawingView = DrawingViewImpl<Drawing<Component>>(d, c)
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
    }

    private fun customizeProperties(properties: Properties) {
        properties.predefine(Style.PROP_FOREGROUND_COLOR, Themes.get<GraphTheme>().vertice.color.foregroundColor)
        properties.predefine(Style.PROP_BACKGROUND_COLOR, Themes.get<GraphTheme>().vertice.color.backgroundColor)
        properties.predefine(Style.PROP_TEXT_COLOR, Themes.get<GraphTheme>().vertice.color.textColor)
        properties.predefine(Style.PROP_STROKE, Themes.get<GraphTheme>().vertice.stroke)
        properties.predefine(Style.PROP_FONT, Themes.get<GraphTheme>().vertice.font)

        properties.predefine(Grid.PROP_GRID_DEFAULT_DISTANCE, Look.GRID)
        properties.predefine(Grid.PROP_GRID_DEFAULT_PAINT_FACTOR, 2)
        properties.predefine(ComponentSnapper.PROP_SNAP_HIGHLIGHT_COLOR, Themes.get<GraphTheme>().selection.foregroundColor)
        properties.predefine(ComponentSnapper.PROP_SNAP_HIGHLIGHT_STROKE, Stroke(0.5f))

        properties.predefine(OriginIndicator.PROP_SELECTION_COLOR, Themes.get<GraphTheme>().selection.foregroundColor)

        properties.predefine(Handle.PROP_BORDER_COLOR, Themes.get<GraphTheme>().selection.foregroundColor)
        properties.predefine(Handle.PROP_FILL_COLOR, Themes.get<GraphTheme>().selection.backgroundColor)

        properties.predefine(AutoConnectorHighlight.PROP_COLOR, Themes.get<GraphTheme>().selection.foregroundColor)
        properties.predefine(DragEdgePointHighlight.PROP_COLOR, Themes.get<GraphTheme>().selection.foregroundColor)

        properties.predefine(CircuitInOutView.PROP_INPUT_ICON_PATH, "/img/input.png")
        properties.predefine(CircuitInOutView.PROP_OUTPUT_ICON_PATH, "/img/output.png")
        properties.predefine(CircuitInOutView.PROP_INOUT_ICON_PATH, "/img/inout.png")
        properties.predefine(SwitchView.PROP_ICON_PATH, "/img/switch.png")
        properties.predefine(ProbeView.PROP_ICON_PATH, "/img/probe.png")
        properties.predefine(LEDView.PROP_ICON_PATH, "/img/led.png")
        properties.predefine(LEDMatrixView.PROP_ICON_PATH, "/img/led-matrix.png")
        properties.predefine(SevenSegmentDisplayView.PROP_ICON_PATH, "/img/7segment.png")
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
        typeMap.register("clockView", ClockView::class)
        typeMap.register("ledView", LEDView::class)
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
    }

    private fun configureSelectionModels(factory: SelectionModelFactory) {
        factory.register(SelectionDrawingStrategy.REPLACE, DigitalNodeView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.REPLACE, DigitalPortViewComponent::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.REPLACE, DigitalSignalSourceControlView::class.simpleName!!, {SelectedColorSelectionModel(it)})

        factory.register(SelectionDrawingStrategy.REPLACE, LabelComponent::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.REPLACE, DigitalEdgeView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.BELOW, DigitalEdgeView::class.simpleName!!, { EdgeViewBelowSelectionModel(it as EdgeView<*>) })

        factory.register(SelectionDrawingStrategy.REPLACE, SplitterView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.REPLACE, ConcentratorView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.REPLACE, ProbeView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.REPLACE, ConstantView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.REPLACE, TunnelView::class.simpleName!!, {SelectedColorSelectionModel(it)})

        factory.register(SelectionDrawingStrategy.REPLACE, AndGateView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.BELOW, AndGateView::class.simpleName!!, {BoxGateViewBelowSelectionModel(it as BoxGateView<*>)})
        factory.register(SelectionDrawingStrategy.REPLACE, OrGateView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.BELOW, OrGateView::class.simpleName!!, {BoxGateViewBelowSelectionModel(it as BoxGateView<*>)})
        factory.register(SelectionDrawingStrategy.REPLACE, NotGateView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.BELOW, NotGateView::class.simpleName!!, {BoxGateViewBelowSelectionModel(it as BoxGateView<*>)})
        factory.register(SelectionDrawingStrategy.REPLACE, NandGateView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.BELOW, NandGateView::class.simpleName!!, {BoxGateViewBelowSelectionModel(it as BoxGateView<*>)})
        factory.register(SelectionDrawingStrategy.REPLACE, NorGateView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.BELOW, NorGateView::class.simpleName!!, {BoxGateViewBelowSelectionModel(it as BoxGateView<*>)})
        factory.register(SelectionDrawingStrategy.REPLACE, XorGateView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.BELOW, XorGateView::class.simpleName!!, {BoxGateViewBelowSelectionModel(it as BoxGateView<*>)})
        factory.register(SelectionDrawingStrategy.REPLACE, XnorGateView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.BELOW, XnorGateView::class.simpleName!!, {BoxGateViewBelowSelectionModel(it as BoxGateView<*>)})
        factory.register(SelectionDrawingStrategy.REPLACE, BufferGateView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.BELOW, BufferGateView::class.simpleName!!, {BoxGateViewBelowSelectionModel(it as BoxGateView<*>)})
        factory.register(SelectionDrawingStrategy.REPLACE, TriStateBufferGateView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.BELOW, TriStateBufferGateView::class.simpleName!!, {BoxGateViewBelowSelectionModel(it as BoxGateView<*>)})
        factory.register(SelectionDrawingStrategy.REPLACE, DelayGateView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.BELOW, DelayGateView::class.simpleName!!, {BoxGateViewBelowSelectionModel(it as BoxGateView<*>)})

        factory.register(SelectionDrawingStrategy.REPLACE, SwitchView::class.simpleName!!, {SwitchViewSelectionModel(it as SwitchView)})
        factory.register(SelectionDrawingStrategy.REPLACE, ClockView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.REPLACE, CircuitInOutView::class.simpleName!!, {SelectedColorSelectionModel(it)})

        factory.register(SelectionDrawingStrategy.REPLACE, LEDView::class.simpleName!!, { LEDViewSelectionModel(it as LEDView) })
        factory.register(SelectionDrawingStrategy.REPLACE, SevenSegmentDisplayView::class.simpleName!!, { SevenSegmentDisplayViewSelectionModel(it as SevenSegmentDisplayView) })
        factory.register(SelectionDrawingStrategy.REPLACE, LEDMatrixView::class.simpleName!!, {LEDMatrixViewSelectionModel(it as LEDMatrixView)})

        factory.register(SelectionDrawingStrategy.REPLACE, ROMView::class.simpleName!!, {SelectedColorSelectionModel(it)})
        factory.register(SelectionDrawingStrategy.REPLACE, RAMView::class.simpleName!!, {SelectedColorSelectionModel(it)})
    }
}