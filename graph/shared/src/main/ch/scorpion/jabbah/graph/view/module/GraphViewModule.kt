package ch.scorpion.jabbah.graph.view.module

import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.BasicStyle
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.select.SelectionModelFactory
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.script.GraphScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.view.connect.*
import ch.scorpion.jabbah.graph.view.container.*
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.net.edge.*
import ch.scorpion.jabbah.graph.view.net.netview.NetViewImpl
import ch.scorpion.jabbah.graph.view.net.node.NodeViewFactory
import ch.scorpion.jabbah.graph.view.net.node.NodeViewFactoryImpl
import ch.scorpion.jabbah.graph.view.net.node.NodeViewImpl
import ch.scorpion.jabbah.graph.view.port.PortFactory
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.port.UndefinedPortFactory
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImplSelectionModel
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.rectangle.RectangularHandleSelectionModel
import ch.scorpion.jabbah.edit.select.SelectedColorSelectionModel
import ch.scorpion.jabbah.edit.style.EditTheme
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.style.GraphTheme


/**
 * Module definitions for the [ch.scorpion.jabbah.graph.view] module.
 */
object GraphViewModule : AbstractModule() {

    var graphViewFactory: () -> GraphView<*> = {GraphViewImpl<GraphElementView<GraphElement>>()}

    /** Must be specified by higher application layers.*/
    var portFactory: PortFactory = UndefinedPortFactory()

    var containerEditorFactory: (EventBus) -> ContainerEditor = { throw UnsupportedOperationException("ContainerEditorFactor not configured") }

    val dragEdgeViewOriginConnector: DragEdgeViewOriginConnector by lazy {DragEdgeViewOriginConnector({ graphViewConnectService})}
    val dragEdgeViewDestinationConnector: DragEdgeViewDestinationConnector by lazy { DragEdgeViewDestinationConnector({graphViewConnectService})}

    val edgeToPortConnector: EdgeToPortConnector by lazy { EdgeToPortConnector(
            {graphViewConnectService},
            {edgeViewFactoryImpl})
    }

    val outputToInputConnector: OutputToInputConnector by lazy { OutputToInputConnector(
            { graphViewConnectService},
            {edgeViewFactoryImpl})
    }

    val inputToOutputOrEdgeConnector: InputToOutputOrEdgeConnector by lazy { InputToOutputOrEdgeConnector(
            { graphViewConnectService},
            { edgeViewFactoryImpl})
    }

    override fun initialize() {
        EditModule.require()
        AppModule.require()
        GraphModelModule.require()
        configureTypeMap(IOModule.typeMap)
        fillProperties(BaseModule.properties)
        configureStyleRepository(StyleRepository.INSTANCE)
        configureSelectionModels(EditSelectModule.selectionModelFactory)
        ScriptModule.scriptGatewayProvider = { GraphScriptGateway(ScriptModule.scriptEngineProvider.invoke()) }

        Themes.register(GraphTheme())
    }

    private fun configureTypeMap(typeMap: TypeMap) {
        typeMap.register("graphElement", GraphElementViewWrapper::class)
        typeMap.register("graphView", GraphViewImpl::class)
        typeMap.register("netView", NetViewImpl::class)
        typeMap.register("edgeView", EdgeViewImpl::class)
        typeMap.register("nodeView", NodeViewImpl::class)
        typeMap.register("containerDrawing", ContainerDrawing::class)
        typeMap.register("originIndicator", OriginIndicator::class)
        typeMap.register("subGraphVerticeViewRef", SubGraphVerticeViewImpl::class)
        typeMap.register("containerBox", RectangularComponent::class)
        typeMap.register("controlViewComponent", ControlViewComponent::class)
    }

    private fun configureStyleRepository(repository: StyleRepository) {
        repository.registerStyle(GraphStyleType.VERTICE, BasicStyle())
        repository.registerStyle(GraphStyleType.ANNOTATION, BasicStyle())
        repository.registerStyle(GraphStyleType.EDGE, BasicStyle())
        repository.registerStyle(GraphStyleType.EXPLANATION, BasicStyle())
        repository.registerStyle(GraphStyleType.SUBSYSTEM, BasicStyle())
    }

    private fun fillProperties(properties: Properties) {
        properties.predefine(PortView.PROP_SENSITIVE_AREA, 10)
        properties.predefine(PortView.PROP_HIGHLIGHT, ConnectionPointHighlightCircle())
        properties.predefine(DragEdgePointHighlight.PROP_COLOR, Color.BLACK)
        properties.predefine(DragEdgePointHighlight.PROP_HALF_SIZE, 6)
        properties.predefine(ConnectionPointHighlightCircle.PROP_COLOR, Themes.get<EditTheme>().selection.foregroundColor)
        properties.predefine(OriginIndicator.PROP_COLOR, Color.BLUE)
        properties.predefine(OriginIndicator.PROP_SELECTION_COLOR, Color.RED)
    }

    private fun configureSelectionModels(factory: SelectionModelFactory) {
        factory.register(SelectionDrawingStrategy.REPLACE, SubGraphVerticeViewImpl::class.simpleName!!,
            { SubGraphVerticeViewImplSelectionModel(it as SubGraphVerticeViewImpl, EditSelectModule.selectionModelProvider) })
        factory.register(SelectionDrawingStrategy.REPLACE, OriginIndicator::class.simpleName!!, { OriginIndicatorSelectionModel(it as OriginIndicator)})
        factory.register(SelectionDrawingStrategy.REPLACE, PortViewComponent::class.simpleName!!, { SelectedColorSelectionModel(it) })
        factory.register(SelectionDrawingStrategy.REPLACE, ControlViewComponent::class.simpleName!!, { SelectedColorSelectionModel(it) })
        factory.register(SelectionDrawingStrategy.BELOW, EdgeViewImpl::class.simpleName!!, { EdgeViewBelowSelectionModel(it as EdgeView<*>) })
    }

    val graphViewConnectService: GraphViewConnectService by lazy {
        GraphViewConnectServiceImpl(
                {edgeViewFactoryImpl},
                { nodeViewFactory})}

    private var edgeViewFactoryImpl: EdgeViewFactory<Any> = EdgeViewFactoryImpl(
                DrawStyleModule.styleProvider, { edgeToPortConnector}, {dragEdgeViewOriginConnector}, {dragEdgeViewDestinationConnector})

    fun <T: Any> getEdgeViewFactory(): EdgeViewFactory<T> {
        return edgeViewFactoryImpl as EdgeViewFactory<T>
    }

    fun <T: Any> setEdgeViewFactory(factory: EdgeViewFactory<T>) {
        edgeViewFactoryImpl = factory as EdgeViewFactory<Any>
    }

    private var nodeViewFactory: NodeViewFactory<Any> = NodeViewFactoryImpl(DrawStyleModule.styleProvider)

    fun <T: Any> getNodeViewFactory(): NodeViewFactory<T> {
        return nodeViewFactory as NodeViewFactory<T>
    }

    fun <T: Any> setNodeViewFactory(factory: NodeViewFactory<T>) {
        nodeViewFactory = factory as NodeViewFactory<Any>
    }

    fun <T: GraphElementView<*>> createGraphView(graph: Graph = GraphModelModule.graphFactory.invoke()): GraphView<T> {
        return GraphViewImpl(graph, IOModule.storableClonerProvider.invoke(), outputToInputConnector, inputToOutputOrEdgeConnector, graphViewConnectService, BaseModule.eventBus)
    }

    fun createContainerDrawing(): ContainerDrawing {
        return ContainerDrawing(
            IOModule.storableCreator,
            IOModule.storableClonerProvider.invoke(),
            BaseModule.eventBus,
            ScriptModule.scriptGateway,
            LibraryModule.libraryHolder,
            DrawStyleModule.styleProvider)
    }
}