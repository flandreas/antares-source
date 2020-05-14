package ch.scorpion.jabbah.graph.view.module

import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.BasicStyle
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.highlight.EditHighlightModule
import ch.scorpion.jabbah.edit.model.text.SimpleTextComponent
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.select.BoundingBoxBelowSelectionModel
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.select.SelectedColorSelectionModel
import ch.scorpion.jabbah.edit.select.SelectionModelFactory
import ch.scorpion.jabbah.edit.style.EditStyleType
import ch.scorpion.jabbah.edit.style.EditTheme
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.*
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.ApplicationModeHolder
import ch.scorpion.jabbah.graph.UndefinedApplicationModeHolder
import ch.scorpion.jabbah.graph.container.*
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.script.GraphScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptEngine
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.app.*
import ch.scorpion.jabbah.graph.view.connect.*
import ch.scorpion.jabbah.graph.view.editor.GraphEditor
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.net.edge.*
import ch.scorpion.jabbah.graph.view.net.netview.NetViewImpl
import ch.scorpion.jabbah.graph.view.net.node.NodeViewFactory
import ch.scorpion.jabbah.graph.view.net.node.NodeViewFactoryImpl
import ch.scorpion.jabbah.graph.view.net.node.NodeViewImpl
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeProbeVerticeView
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeView
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeViewFactory
import ch.scorpion.jabbah.graph.view.oscilloscope.UndefinedOscilloscopeViewFactory
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.port.PortViewFactory
import ch.scorpion.jabbah.graph.view.port.PortViewStorable
import ch.scorpion.jabbah.graph.view.port.UndefinedPortViewFactory
import ch.scorpion.jabbah.graph.view.scenario.ScenarioDetector
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImplSelectionModel
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewStorable
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap


/**
 * Module definitions for the [ch.scorpion.jabbah.graph.view] module.
 */
object GraphViewModule : AbstractModule() {

	var graphViewFactory: (name: String?) -> GraphView = {
		GraphViewImpl(it ?: Translations.getString("graph.name.unknown"))
	}

	/** Must be specified by higher application layers.*/
	var portViewFactory: PortViewFactory = UndefinedPortViewFactory()

	var graphEditorFactory: (EventBus) -> GraphEditor = { throw UnsupportedOperationException("GraphEditor factory not configured") }

	var containerEditorFactory: (EventBus) -> ContainerEditor = { throw UnsupportedOperationException("ContainerEditor factory not configured") }

	val dragEdgeViewOriginConnector: DragEdgeViewOriginConnector by lazy { DragEdgeViewOriginConnector(graphViewConnectService) }
	val dragEdgeViewDestinationConnector: DragEdgeViewDestinationConnector by lazy { DragEdgeViewDestinationConnector(graphViewConnectService) }

	val edgeToPortConnector: EdgeToPortConnector by lazy { EdgeToPortConnector(graphViewConnectService, edgeViewFactoryImpl) }

	val outputToInputConnector: OutputToInputConnector by lazy { OutputToInputConnector(graphViewConnectService, edgeViewFactoryImpl) }

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

	val oscilloscopeViewService: OscilloscopeViewService = OscilloscopeViewServiceImpl(EditModule.commandManager, BaseModule.eventBus)

	val scenarioAppService = ScenarioAppService()

	val usecaseAppService = UsecaseAppService()

	var applicationModeHolder: ApplicationModeHolder = UndefinedApplicationModeHolder()

	val currentGraphViewAnimationType: CurrentGraphViewAnimationType by lazy { CurrentGraphViewAnimationType() }

	val manualSchedulerTask = ManualSchedulerTask()

	val timedSchedulerTask = TimedSchedulerTask()

	val switchableSchedulerTask = SwitchableSchedulerTask(listOf(timedSchedulerTask, manualSchedulerTask))

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

		ScriptModule.scriptGatewayProvider = { GraphScriptGateway(ScriptEngine(BaseModule.eventBus)) }
		EditModule.drawingAppService = graphViewAppService

		ExecutionModule.schedulerTaskFactory = {
			if (AppModule.userHolder.user.isDeveloper) {
				switchableSchedulerTask
			} else {
				timedSchedulerTask
			}
		}

		BaseModule.eventBus.register(SchedulerActivationStateEvent::class) { EditModule.commandManager.active = !it.scheduler.isActive }
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
		repository.registerStyle(GraphStyleType.ANNOTATION, BasicStyle())
		repository.registerStyle(GraphStyleType.EDGE, BasicStyle())
		repository.registerStyle(GraphStyleType.EXPLANATION, BasicStyle())
		repository.registerStyle(GraphStyleType.SUBSYSTEM, BasicStyle())
	}

	private fun fillProperties(properties: Properties) {
		properties.set(PortView.PROP_SENSITIVE_AREA, 10)
		properties.set(PortView.PROP_HIGHLIGHT, ConnectionPointHighlightCircle())
		properties.set(DragEdgePointHighlight.PROP_COLOR, Color.BLACK)
		properties.set(DragEdgePointHighlight.PROP_HALF_SIZE, 6)
		properties.set(ConnectionPointHighlightCircle.PROP_COLOR, Themes.get<EditTheme>().selection.color.foregroundColor)
		properties.set(OriginIndicator.PROP_COLOR, Color.BLUE)
		properties.set(OriginIndicator.PROP_SELECTION_COLOR, Color.RED)
		properties.set(GraphViewAnimationType.PROP_GRAPH_VIEW_ANIMATION_TYPE, GraphViewAnimationType.Animation.customName)

		properties.set(ScenarioDetector.PROP_LIMIT_SYSTEM_SPEED_CATEGORY, SystemSpeedCategory.Observe.customName)
		properties.set(SchedulerImpl.PROP_SCHEDULER_EVENT_SYSTEM_SPEED_LIMIT, SystemSpeedCategory.Observe.customName)
		properties.set(ContainerEditor.PROP_DEFAULT_ZOOM_FACTOR, 2.0f)
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
		factory.register(SelectionDrawingStrategy.BELOW, OscilloscopeView::class) { BoundingBoxBelowSelectionModel(it) }
	}

	private fun configureHighlightModels(factory: SelectionModelFactory) {
		factory.register(SelectionDrawingStrategy.BELOW, EdgeViewImpl::class) { EdgeViewBelowSelectionModel(it as EdgeView<*>, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, SubGraphVerticeViewImpl::class) { BoundingBoxBelowSelectionModel(it, styleType = EditStyleType.HIGHLIGHT) }
		factory.register(SelectionDrawingStrategy.BELOW, OscilloscopeView::class) { BoundingBoxBelowSelectionModel(it, styleType = EditStyleType.HIGHLIGHT) }
	}

	private var edgeViewFactoryImpl: EdgeViewFactory<Any> = EdgeViewFactoryImpl(
		DrawStyleModule.styleProvider,
		{ edgeToPortConnector },
		{ dragEdgeViewOriginConnector },
		{ dragEdgeViewDestinationConnector },
		ExecutionModule.currentSystemSpeedCategory
	)

	fun <T : Any> getEdgeViewFactory(): EdgeViewFactory<T> {
		return edgeViewFactoryImpl as EdgeViewFactory<T>
	}

	fun <T : Any> setEdgeViewFactory(factory: EdgeViewFactory<T>) {
		edgeViewFactoryImpl = factory as EdgeViewFactory<Any>
	}

	private var nodeViewFactory: NodeViewFactory<Any> = NodeViewFactoryImpl(
		DrawStyleModule.styleProvider,
		ExecutionModule.currentSystemSpeedCategory
	)

	fun <T : Any> getNodeViewFactory(): NodeViewFactory<T> {
		return nodeViewFactory as NodeViewFactory<T>
	}

	fun <T : Any> setNodeViewFactory(factory: NodeViewFactory<T>) {
		nodeViewFactory = factory as NodeViewFactory<Any>
	}

	fun createGraphView(name: String = Translations.getString("graph.name.unknown")): GraphView {
		return createGraphView(GraphModelModule.graphFactory.invoke(name))
	}

	fun createGraphView(graph: Graph): GraphView {
		return GraphViewImpl(graph, BaseModule.eventBus)
	}

	fun createContainerDrawing(name: String = Translations.getString("graph.name.unknown")): ContainerDrawing {
		return ContainerDrawing(
			name,
			IOModule.storableCreator,
			BaseModule.eventBus,
			ScriptModule.scriptGateway,
			GraphModelModule.metaGraphRepository,
			DrawStyleModule.styleProvider)
	}
}