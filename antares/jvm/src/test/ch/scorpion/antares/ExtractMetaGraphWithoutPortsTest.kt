package ch.scorpion.antares

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.antares.view.metagraph.AntaresMetaGraphService
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.container.ContainerDrawingLayouter
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.graph.GraphViewCopyPasteService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.mock
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

    private val drawingView = mock<DrawingView<GraphView>>()

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

        every { drawingView.drawing } calls { getCircuitView() }
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
            TranslatableText("Extract"), AntaresGraphTypes.Digital, drawingView, componentIds, library)
        return library.getMetaGraph(uuid)
    }
}