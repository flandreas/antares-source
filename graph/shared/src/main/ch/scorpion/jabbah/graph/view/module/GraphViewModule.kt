package ch.scorpion.jabbah.graph.view.module

import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.base.help.HelpSource
import ch.scorpion.jabbah.base.help.HelpSourceRegistry
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.BasicStyle
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.drag.DragDestinationHighlighter
import ch.scorpion.jabbah.edit.drag.DragManagerImpl
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.highlight.EditHighlightModule
import ch.scorpion.jabbah.edit.model.text.SimpleTextComponent
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.select.BoundingBoxBelowSelectionModel
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.select.SelectedColorSelectionModel
import ch.scorpion.jabbah.edit.select.SelectionModelFactory
import ch.scorpion.jabbah.edit.style.EditStyleType
import ch.scorpion.jabbah.edit.style.EditTheme
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphAuthorizations
import ch.scorpion.jabbah.graph.container.*
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinitions
import ch.scorpion.jabbah.graph.ui.EmptyGraphNavigationControllerExtension
import ch.scorpion.jabbah.graph.ui.GraphNavigationViewController
import ch.scorpion.jabbah.graph.ui.GraphNavigationViewControllerExtension
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.app.GraphViewAppService
import ch.scorpion.jabbah.graph.view.app.GraphViewAppServiceImpl
import ch.scorpion.jabbah.graph.view.app.ScenarioAppService
import ch.scorpion.jabbah.graph.view.app.UsecaseAppService
import ch.scorpion.jabbah.graph.view.app.oscilloscope.OscilloscopeViewService
import ch.scorpion.jabbah.graph.view.app.oscilloscope.OscilloscopeViewServiceImpl
import ch.scorpion.jabbah.graph.view.connect.*
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointDenialCross
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlight
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlightCircle
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointReconnect
import ch.scorpion.jabbah.graph.view.editor.AutoConnector
import ch.scorpion.jabbah.graph.view.editor.GraphEditor
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.metagraph.MetaGraphService
import ch.scorpion.jabbah.graph.view.net.edge.*
import ch.scorpion.jabbah.graph.view.net.netview.GraphNetViewElementColorProvider
import ch.scorpion.jabbah.graph.view.net.netview.NetViewElementColorProvider
import ch.scorpion.jabbah.graph.view.net.netview.NetViewImpl
import ch.scorpion.jabbah.graph.view.net.node.NodeViewFactory
import ch.scorpion.jabbah.graph.view.net.node.NodeViewFactoryImpl
import ch.scorpion.jabbah.graph.view.net.node.NodeViewImpl
import ch.scorpion.jabbah.graph.view.oscilloscope.*
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.port.PortViewFactory
import ch.scorpion.jabbah.graph.view.port.PortViewStorable
import ch.scorpion.jabbah.graph.view.port.UndefinedPortViewFactory
import ch.scorpion.jabbah.graph.view.scenario.ScenarioDetector
import ch.scorpion.jabbah.graph.view.scenario.ScenarioImpl
import ch.scorpion.jabbah.graph.view.scenario.ScenarioStepImpl
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.graph.view.usecase.UsecaseImpl
import ch.scorpion.jabbah.graph.view.usecase.UsecaseRecorder
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImplSelectionModel
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewStorable
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap


/**
 * Module definitions for the [ch.scorpion.jabbah.graph.view] module.
 */
object GraphViewModule : AbstractModule() {

	var graphViewFactory: GraphViewFactory = object : GraphViewFactory {
		override fun create(name: TranslatableText?): GraphView =
			GraphViewImpl(name ?: TranslatableText(Translations.getString("graph.name.unknown")))

		override fun create(model: Graph): GraphView = GraphViewImpl(model)
	}

	/** Must be specified by higher application layers.*/
	var portViewFactory: PortViewFactory = UndefinedPortViewFactory()

	var graphEditorFactory: (String, DrawingView<Drawing<Component>>) -> GraphEditor = { name, view -> GraphEditor(view, name = name) }

	var containerEditorFactory: (DrawingView<Drawing<Component>>, DrawingView<Drawing<Component>>) -> ContainerEditor = { dv1, dv2 -> ContainerEditor(dv1, dv2) }

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

	var oscilloscopeProbeNameStrategy: OscilloscopeProbeNameStrategy = OscilloscopeProbeNameStrategyImpl()

	val scenarioAppService = ScenarioAppService()

	val usecaseAppService = UsecaseAppService()

	val currentGraphViewAnimationType: CurrentGraphViewAnimationType by lazy { CurrentGraphViewAnimationType() }

	var graphNavigationViewControllerExtension: (GraphNavigationViewController) -> GraphNavigationViewControllerExtension = { EmptyGraphNavigationControllerExtension() }

	var graphViewExecutionAnimationFactory: GraphViewExecutionAnimationFactory = UndefinedGraphViewExecutionAnimationFactory()

	var metaGraphService = MetaGraphService()

	var netViewElementColorProvider: NetViewElementColorProvider<*> = GraphNetViewElementColorProvider

	fun <T: Any> getTypedNetViewElementColorProvider(): NetViewElementColorProvider<T> = netViewElementColorProvider as NetViewElementColorProvider<T>

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
			if (editor is GraphEditor) {
				DragManagerImpl(editor, plugins = setOf(AutoConnector))
			} else if (editor is ContainerEditor) {
				DragManagerImpl(editor, plugins = setOf(DragDestinationHighlighter))
			} else {
				DragManagerImpl(editor)
			}
		}
		EditModule.drawingAppService = graphViewAppService

		GraphAuthorizations.define()
		registerHelpSources()
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
		properties.set(SchedulerImpl.PROP_SCHEDULER_EVENT_SYSTEM_SPEED_LIMIT, SystemSpeedCategory.Observe.customName)
		properties.set(ContainerEditor.PROP_DEFAULT_ZOOM_FACTOR, 2.0f)

		properties.set(OscilloscopeView.PROP_INDIVIDUAL_PROBE_COLORS, true)
		properties.set(UsecaseRecorder.PROP_DEF_DELAY_MS, 2)
		properties.set(UsecaseRecorder.PROP_DEF_TIME_BETWEEN_CLICKS_MS, 100)
	}

	private fun configureSelectionModels(factory: SelectionModelFactory) {
		factory.register(SelectionDrawingStrategy.REPLACE, EdgeViewImpl::class) { EdgeViewReplaceSelectionModel(it as EdgeView<*>) }
		factory.register(SelectionDrawingStrategy.REPLACE, SubGraphVerticeViewImpl::class) { SubGraphVerticeViewImplSelectionModel(it as SubGraphVerticeViewImpl, EditSelectModule.selectionModelProvider) }
		factory.register(SelectionDrawingStrategy.REPLACE, OriginIndicator::class) { OriginIndicatorSelectionModel(it as OriginIndicator) }
		factory.register(SelectionDrawingStrategy.REPLACE, PortViewComponent::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, ControlViewComponent::class) { SelectedColorSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.REPLACE, OscilloscopeProbeVerticeView::class) { SelectedColorSelectionModel(it) }

		factory.register(SelectionDrawingStrategy.BELOW, EdgeViewImpl::class) { EdgeViewBelowSelectionModel(it as EdgeView<*>) }
		factory.register(SelectionDrawingStrategy.BELOW, SubGraphVerticeViewImpl::class) { BoundingBoxBelowSelectionModel(it) }
		factory.register(SelectionDrawingStrategy.BELOW, OscilloscopeView::class) { BoundingBoxBelowSelectionModel(it, outset = 3) }
	}

	private fun configureHighlightModels(factory: SelectionModelFactory) {
		factory.register(SelectionDrawingStrategy.BELOW, EdgeViewImpl::class) { EdgeViewBelowSelectionModel(it as EdgeView<*>, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, SubGraphVerticeViewImpl::class) { BoundingBoxBelowSelectionModel(it, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, OscilloscopeView::class) { BoundingBoxBelowSelectionModel(it, styleType = EditStyleType.HIGHLIGHT, outset = 3) }
	}

	private var edgeViewFactoryImpl: EdgeViewFactory = EdgeViewFactoryImpl(
		DrawStyleModule.styleProvider,
		{ edgeToPortOrEdgeConnector },
		{ dragEdgeViewOriginConnector },
		{ dragEdgeViewDestinationConnector }
	)

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