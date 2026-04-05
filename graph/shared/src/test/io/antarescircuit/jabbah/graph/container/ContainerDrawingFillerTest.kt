package io.antarescircuit.jabbah.graph.container

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.edit.model.text.LabelComponent
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.TestGraphPortView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ContainerDrawingFillerTest {

    private lateinit var containerDrawing: ContainerDrawing
    private lateinit var graphView: GraphView
    private lateinit var filler: NarrowContainerDrawingFiller

    private fun createGraphView(): GraphView =
        GraphViewModule.graphViewFactory.create(null).apply {
            add(TestGraphPortView.input<Boolean>("I"))
            add(TestGraphPortView.output<Boolean>("O"))
        }

    @BeforeTest
    fun setup() {
        GraphViewTestRule.configure()
        containerDrawing = GraphViewModule.createContainerDrawing()
        graphView = createGraphView()
        filler = NarrowContainerDrawingFiller(graphView, containerDrawing, addLabel = true)
    }

    @Test
    fun shouldFill() {
        filler.fill()

        assertEquals(1, containerDrawing.drawables.filterIsInstance<LabelComponent>().size)
        assertEquals(1, containerDrawing.drawables.filterIsInstance<OriginIndicator>().size)
        assertEquals(2, containerDrawing.drawables.filterIsInstance<PortViewComponent<*>>().size)

        assertPortView("I", 1, Direction.WEST)
        assertPortView("O", 2, Direction.EAST)
    }

    @Test
    fun shouldPreserveAssignments() {
        filler.fill()
        graphView.add(TestGraphPortView.input<Boolean>("I2"))

        filler.fill()

        assertPortView("I", 1, Direction.WEST)
        assertPortView("O", 2, Direction.EAST)
        assertPortView("I2", 3, Direction.WEST)
    }

    private fun assertPortView(name: String, expPortId: Int, expDirection: Direction) {
        containerDrawing.getPortViewComponent(name)!!.portView!!.apply {
            assertEquals(expDirection, direction, "direction")
            assertEquals(expPortId, port.portId, "portId")
        }
    }
}