package io.antarescircuit.antares

import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.metagraph.AntaresMetaGraphService
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.container.ContainerDrawingLayouter
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.graph.GraphViewCopyPasteService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression test for bug #985 'System malfunction' after 'Extract to subcircuit'.
 * Occurred if a single AND gate was extracted to a subcircuit.
 */
class ExtractMetaGraphWithoutPortsTest : AbstractJvmCircuitTest() {

    private lateinit var sourceMetaGraph: MetaGraph
    private lateinit var input1: DigitalCircuitInOutView
    private lateinit var input2: DigitalCircuitInOutView
    private lateinit var output: DigitalCircuitInOutView
    private lateinit var and: LogicGateView

    private val drawingViewBuilder = DrawingViewMockBuilder()
        .withSize(1000, 1000)
        .withDrawingAccessor(::getCircuitView)

    private val library get() = LibraryModule.libraryHolder.library

    override fun getCircuitView(): GraphView = sourceMetaGraph.graph.graphView

    override fun setup() {
        super.setup()
        GraphViewModule.metaGraphService = AntaresMetaGraphService(copyPasteService = GraphViewCopyPasteService())
        BaseModule.properties.set(ContainerDrawingLayouter.PROP_CONTAINER_DRAWING_LAYOUTER, ContainerDrawingLayouter.Narrow.customName)

        setupLibrary()
        val builder = GraphViewBuilder<DigitalSignal>("test")

        input1 = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INPUT)).also { it.location = Point2D(100, 100) })
        input2 = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INPUT)).also { it.location = Point2D(100, 300) })
        output = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.OUTPUT)).also { it.location = Point2D(300, 200) })
        and = builder.addVerticeView(LogicGateView.andGateView().also { it.location = Point2D(200, 200) })
        builder.connect(input1, and, and.model.getInput(1))
        builder.connect(input2, and, and.model.getInput(2))
        builder.connect(and, and.model.getOutput(3), output)

        val libraryBuilder = TestLibraryBuilder()
        sourceMetaGraph = libraryBuilder.addGraphView(builder.graphView, library)
    }

    @Test
    fun shouldExtract() {
        val targetMetaGraph = extract()

        assertEquals(7, getCircuitView().drawables.size)
        assertEquals(1, targetMetaGraph.graph.graphView.drawables.size)
        assertTrue(getCircuitView().getEdgeViews().none { it.hasBrokenPortRef })
    }

    private fun extract(): MetaGraph {
        val componentIds = listOf(and.id)
        val uuid = GraphViewModule.metaGraphService.extractMetaGraph(
            TranslatableText("Extract"),
            AntaresGraphTypes.Digital,
            drawingViewBuilder.build<GraphElementView<*>, GraphView>(),
            componentIds,
            library
        )
        return library.getMetaGraph(uuid)
    }
}