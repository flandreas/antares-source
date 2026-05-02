package io.antarescircuit.jabbah.graph.view.module

import io.antarescircuit.jabbah.app.module.AppModule
import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.help.HelpId
import io.antarescircuit.jabbah.base.help.HelpSource
import io.antarescircuit.jabbah.base.help.HelpSourceRegistry
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.style.BasicStyle
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleRepository
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.drag.DragDestinationHighlighter
import io.antarescircuit.jabbah.edit.drag.DragManagerImpl
import io.antarescircuit.jabbah.edit.editor.EditEditorModule
import io.antarescircuit.jabbah.edit.highlight.EditHighlightModule
import io.antarescircuit.jabbah.edit.model.text.SimpleTextComponent
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.select.BoundingBoxBelowSelectionModel
import io.antarescircuit.jabbah.edit.select.EditSelectModule
import io.antarescircuit.jabbah.edit.select.SelectionModelFactory
import io.antarescircuit.jabbah.edit.select.selectedColorSelectionModelFactory
import io.antarescircuit.jabbah.edit.style.EditStyleType
import io.antarescircuit.jabbah.edit.style.EditTheme
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.speed.SystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphAuthorizations
import io.antarescircuit.jabbah.graph.container.*
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.param.GraphParamDefinitions
import io.antarescircuit.jabbah.graph.ui.EmptyGraphNavigationControllerExtension
import io.antarescircuit.jabbah.graph.ui.GraphNavigationViewController
import io.antarescircuit.jabbah.graph.ui.GraphNavigationViewControllerExtension
import io.antarescircuit.jabbah.graph.view.*
import io.antarescircuit.jabbah.graph.view.app.GraphViewAppService
import io.antarescircuit.jabbah.graph.view.app.GraphViewAppServiceImpl
import io.antarescircuit.jabbah.graph.view.app.ScenarioAppService
import io.antarescircuit.jabbah.graph.view.app.UsecaseAppService
import io.antarescircuit.jabbah.graph.view.app.oscilloscope.OscilloscopeViewService
import io.antarescircuit.jabbah.graph.view.app.oscilloscope.OscilloscopeViewServiceImpl
import io.antarescircuit.jabbah.graph.view.connect.*
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointDenialCross
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlight
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlightCircle
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointReconnect
import io.antarescircuit.jabbah.graph.view.editor.AutoConnector
import io.antarescircuit.jabbah.graph.view.editor.GraphEditor
import io.antarescircuit.jabbah.graph.view.graph.GraphViewImpl
import io.antarescircuit.jabbah.graph.view.metagraph.MetaGraphService
import io.antarescircuit.jabbah.graph.view.net.edge.*
import io.antarescircuit.jabbah.graph.view.net.netview.GraphNetViewElementColorProvider
import io.antarescircuit.jabbah.graph.view.net.netview.NetViewElementColorProvider
import io.antarescircuit.jabbah.graph.view.net.netview.NetViewImpl
import io.antarescircuit.jabbah.graph.view.net.node.NodeViewFactory
import io.antarescircuit.jabbah.graph.view.net.node.NodeViewFactoryImpl
import io.antarescircuit.jabbah.graph.view.net.node.NodeViewImpl
import io.antarescircuit.jabbah.graph.view.oscilloscope.*
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.port.PortViewFactory
import io.antarescircuit.jabbah.graph.view.port.PortViewStorable
import io.antarescircuit.jabbah.graph.view.port.UndefinedPortViewFactory
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioDetector
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioImpl
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioMode
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioStepImpl
import io.antarescircuit.jabbah.graph.view.style.GraphStyleType
import io.antarescircuit.jabbah.graph.view.style.GraphTheme
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseImpl
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseRecorder
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImplSelectionModel
import io.antarescircuit.jabbah.graph.view.vertice.VerticeViewStorable
import io.antarescircuit.jabbah.io.IOModule
import io.antarescircuit.jabbah.io.TypeMap


/**
 * Module definitions for the [io.antarescircuit.jabbah.graph.view] module.
 */
object GraphViewModule : AbstractModule() {

	var graphViewFactory: GraphViewFactory = object : GraphViewFactory {
		override fun create(name: TranslatableText?): GraphView =
			GraphViewImpl(name ?: TranslatableText(Translations.getString("graph.name.unknown")))

		override fun create(model: Graph): GraphView = GraphViewImpl(model)
	}

	/** Must be specified by higher application layers.*/
	var portViewFactory: PortViewFactory = UndefinedPortViewFactory()

	var graphEditorFactory: (String, DrawingView<GraphElementView<*>, GraphView>) -> GraphEditor = { name, view -> GraphEditor(view, name = name) }

	var containerEditorFactory: (DrawingView<Component, Drawing<Component>>, DrawingView<GraphElementView<*>, GraphView>) -> ContainerEditor = { dv1, dv2 -> ContainerEditor(dv1, dv2) }

	val dragEdgeViewOriginConnector: DragEdgeViewOriginConnector by lazy { DragEdgeViewOriginConnector(graphViewConnectService) }
	val dragEdgeViewDestinationConnector: DragEdgeViewDestinationConnector by lazy { DragEdgeViewDestinationConnector(graphViewConnectService) }

	val edgeToPortOrEdgeConnector: EdgeToPortOrEdgeConnector by lazy { EdgeToPortOrEdgeConnector(graphViewConnectService, edgeViewFactoryImpl) }

	val outputToInputOrEdgeConnector: OutputToInputOrEdgeConnector by lazy { OutputToInputOrEdgeConnector(graphViewConnectService, edgeViewFactoryImpl) }

	val inputToOutputOrEdgeConnector: InputToOutputOrEdgeConnector by lazy { InputToOutputOrEdgeConnector(graphViewConnectService, edgeViewFactoryImpl) }

	val reconnectOriginConnector: ReconnectOriginConnector by lazy { ReconnectOriginConnector(graphViewConnectService) }

	val reconnectDestinationConnector: ReconnectDestinationConnector by lazy { ReconnectDestinationConnector(graphViewConnectService) }

	val graphViewConnectService: GraphViewConnectService by lazy {
		GraphViewConnectServiceImpl(
			{ edgeViewFactoryImpl },
			{ nodeViewFactory })
	}

	var graphViewAppService: GraphViewAppService = GraphViewAppServiceImpl(EditModule.copyPasteService, EditModule.commandManager)

	/** Must be specified by higher application layers.*/
	var oscilloscopeViewFactory: OscilloscopeViewFactory = UndefinedOscilloscopeViewFactory()

	val oscilloscopeViewService: OscilloscopeViewService = OscilloscopeViewServiceImpl()

	var verticeViewNameStrategy: VerticeViewNameStrategy = VerticeViewNameStrategyImpl()

	val scenarioAppService = ScenarioAppService()

	val usecaseAppService = UsecaseAppService()

	val currentGraphViewAnimationType: CurrentGraphViewAnimationType by lazy { CurrentGraphViewAnimationType() }

	var graphNavigationViewControllerExtension: (GraphNavigationViewController) -> GraphNavigationViewControllerExtension = { EmptyGraphNavigationControllerExtension() }

	var graphViewExecutionAnimationFactory: GraphViewExecutionAnimationFactory = UndefinedGraphViewExecutionAnimationFactory()

	var metaGraphService = MetaGraphService()

	var netViewElementColorProvider: NetViewElementColorProvider<*> = GraphNetViewElementColorProvider

	var connectionEstablishedHandler: ConnectionEstablishedHandler? = null

	@Suppress("UNCHECKED_CAST")
	fun <T: Any> getTypedNetViewElementColorProvider(): NetViewElementColorProvider<T> =
		netViewElementColorProvider as NetViewElementColorProvider<T>

	override fun initialize() {
		EditModule.require()
		AppModule.require()
		GraphModelModule.require()

		Themes.register(GraphTheme())

		configureTypeMap(IOModule.typeMap)
		fillProperties(BaseModule.properties)
		configureStyleRepository(StyleRepository.INSTANCE)
		configureSelectionModels(EditSelectModule.selectionModelFactory)
		configureHighlightModels(EditHighlightModule.highlightModelFactory)

		EditEditorModule.dragManagerFactory = { editor ->
            when (editor) {
                is GraphEditor -> DragManagerImpl(editor, plugins = setOf(AutoConnector))
                is ContainerEditor -> DragManagerImpl(editor, plugins = setOf(DragDestinationHighlighter))
                else -> DragManagerImpl(editor)
            }
		}
		EditModule.drawingService = GraphViewServiceImpl()
		EditModule.drawingAppService = graphViewAppService

		GraphAuthorizations.define()
		registerHelpSources()
	}

	override fun resetDependencies() {
		EditModule.reset()
		AppModule.reset()
		GraphModelModule.reset()
	}

	private fun configureTypeMap(typeMap: TypeMap) {
		typeMap.register("graphElement", GraphElementViewWrapper::class)
		typeMap.register("graphElementWrapper", GraphElementWrapper::class)
		typeMap.register("graphView", GraphViewImpl::class)
		typeMap.register("netView", NetViewImpl::class)
		typeMap.register("edgeView", EdgeViewImpl::class)
		typeMap.register("nodeView", NodeViewImpl::class)
		typeMap.register("containerDrawing", ContainerDrawing::class)
		typeMap.register("originIndicator", OriginIndicator::class)
		typeMap.register("subGraphVerticeViewRef", SubGraphVerticeViewImpl::class)
		typeMap.register("controlViewComponent", ControlViewComponent::class)
		typeMap.register("graphTextComponent", SimpleTextComponent::class)
		typeMap.register("oscilloscopeView", OscilloscopeView::class)
		typeMap.register("oscilloscopeProbeView", OscilloscopeProbeVerticeView::class)
		typeMap.register("verticeViewStorable", VerticeViewStorable::class)
		typeMap.register("portViewStorable", PortViewStorable::class)
	}

	private fun configureStyleRepository(repository: StyleRepository) {
		repository.registerStyle(GraphStyleType.VERTICE, BasicStyle())
		repository.registerStyle(GraphStyleType.EDGE, BasicStyle())
		repository.registerStyle(GraphStyleType.EXPLANATION, BasicStyle())
		repository.registerStyle(GraphStyleType.SUBSYSTEM, BasicStyle())
	}

	private fun fillProperties(properties: Properties) {
		properties.set(EdgeView.PROP_MIN_EDGE_VIEW_LENGTH, 5)
		properties.set(PortView.PROP_SENSITIVE_AREA, 10)
		properties.set(PortView.PROP_HIGHLIGHT, ConnectionPointHighlightCircle())
		properties.set(PortView.PROP_HIGHLIGHT_RECONNECT, ConnectionPointReconnect())
		properties.set(PortView.PROP_CONNECT_DENY, ConnectionPointDenialCross())
		properties.set(DragEdgePointHighlight.PROP_COLOR, Color.BLACK)
		properties.set(DragEdgePointHighlight.PROP_HALF_SIZE, 6)
		properties.set(ConnectionPointHighlight.PROP_COLOR, Themes.get<EditTheme>().snap.color.foregroundColor)
		properties.set(OriginIndicator.PROP_COLOR, Color.BLUE)
		properties.set(OriginIndicator.PROP_SELECTION_COLOR, Color.RED)
		properties.set(GraphViewAnimationType.PROP_GRAPH_VIEW_ANIMATION_TYPE, GraphViewAnimationType.Animation.customName)
		properties.set(GraphNavigationViewController.PROP_DIVE_ANIMATION, true)
		properties.set(OrthoEdgeViewLayouter.PROP_ADVANCED_LAYOUT, true)

		properties.set(ScenarioDetector.PROP_LIMIT_SYSTEM_SPEED_CATEGORY, SystemSpeedCategory.Observe.customName)
		properties.set(Scheduler.PROP_SCHEDULER_EVENT_SYSTEM_SPEED_LIMIT, SystemSpeedCategory.Observe.customName)
		properties.set(ContainerEditor.PROP_DEFAULT_ZOOM_FACTOR, 2.0f)
		properties.set(ScenarioMode.PROP_SCENARIO_MODE, ScenarioMode.SpeedLimitOrSlower.customName)

		properties.set(OscilloscopeView.PROP_INDIVIDUAL_PROBE_COLORS, true)
		properties.set(UsecaseRecorder.PROP_DEF_DELAY_MS, 2)
		properties.set(UsecaseRecorder.PROP_DEF_TIME_BETWEEN_CLICKS_MS, 100)

		properties.set(ConnectMethod.PROP_CONNECT_METHOD, ConnectMethod.AutoLayout.customName)
	}

	private fun configureSelectionModels(factory: SelectionModelFactory) {
		factory.register(SelectionDrawingStrategy.REPLACE, EdgeViewImpl::class) { EdgeViewReplaceSelectionModel(it as EdgeView<*>) }
		factory.register(SelectionDrawingStrategy.REPLACE, SubGraphVerticeViewImpl::class) { SubGraphVerticeViewImplSelectionModel(it as SubGraphVerticeViewImpl, EditSelectModule.selectionModelProvider) }
		factory.register(SelectionDrawingStrategy.REPLACE, OriginIndicator::class) { OriginIndicatorSelectionModel(it as OriginIndicator) }
		factory.register(SelectionDrawingStrategy.REPLACE, PortViewComponent::class, selectedColorSelectionModelFactory)
		factory.register(SelectionDrawingStrategy.REPLACE, ControlViewComponent::class, selectedColorSelectionModelFactory)
		factory.register(SelectionDrawingStrategy.REPLACE, OscilloscopeProbeVerticeView::class, selectedColorSelectionModelFactory)

		factory.register(SelectionDrawingStrategy.BELOW, EdgeViewImpl::class) { EdgeViewBelowSelectionModel(it as EdgeView<*>) }
		factory.register(SelectionDrawingStrategy.BELOW, SubGraphVerticeViewImpl::class) { BoundingBoxBelowSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.BELOW, OscilloscopeView::class) { BoundingBoxBelowSelectionModel(it, outset = 3) }
	}

	private fun configureHighlightModels(factory: SelectionModelFactory) {
		factory.register(SelectionDrawingStrategy.BELOW, EdgeViewImpl::class) { EdgeViewBelowSelectionModel(it as EdgeView<*>, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, SubGraphVerticeViewImpl::class) { BoundingBoxBelowSelectionModel(it, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, OscilloscopeView::class) { BoundingBoxBelowSelectionModel(it, styleType = EditStyleType.HIGHLIGHT, outset = 3) }
	}

	private var edgeViewFactoryImpl: EdgeViewFactory = EdgeViewFactoryImpl(DrawStyleModule.styleProvider)

	private fun registerHelpSources() {
		HelpSourceRegistry.register(HelpId(OscilloscopeView::class.simpleName!!), HelpSource("/oscilloscope/oscilloscope"))
		HelpSourceRegistry.register(UsecaseRecorder.HELP_ID, HelpSource("/usecases/usecases#recording"))
		HelpSourceRegistry.register(GraphViewImpl.SCRIPT_HELP_ID, HelpSource("/circuits/circuit-scripting"))
		HelpSourceRegistry.register(ContainerDrawing.SCRIPT_HELP_ID, HelpSource("/subcircuits/subcircuits#representation"))
		HelpSourceRegistry.register(ScenarioImpl.CONDITION_HELP_ID, HelpSource("/scenarios/scenarios#scenarioCondition"))
		HelpSourceRegistry.register(ScenarioStepImpl.SCRIPTING_HELP_ID, HelpSource("/scenarios/scenarios#scenarioStepScripting"))
		HelpSourceRegistry.register(UsecaseImpl.SCRIPTING_HELP_ID, HelpSource("/usecases/usecase-scripting"))
		HelpSourceRegistry.register(GraphParamDefinitions.HELP_ID, HelpSource("circuit-parameters#graphParamDefinitions"))
	}

	fun getEdgeViewFactory(): EdgeViewFactory = edgeViewFactoryImpl

	fun setEdgeViewFactory(factory: EdgeViewFactory) {
		edgeViewFactoryImpl = factory
	}

	private var nodeViewFactory: NodeViewFactory = NodeViewFactoryImpl(
		DrawStyleModule.styleProvider
	)

	fun setNodeViewFactory(factory: NodeViewFactory) {
		nodeViewFactory = factory
	}

	fun createContainerDrawing(name: String = Translations.getString("graph.name.unknown")): ContainerDrawing =
		ContainerDrawing(
			name,
			BaseModule.eventBus,
			LibraryModule.libraryHolder,
			DrawStyleModule.styleProvider)
}