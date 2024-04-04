package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphPortView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import kotlin.test.Test
import kotlin.test.assertEquals

class ContainerDrawingFillerTest {

    companion object {
        init {
            GraphViewTestRule.configure()
        }
    }

    private val containerDrawing = GraphViewModule.createContainerDrawing()
    private val graphView = createGraphView()
    private val filler = NarrowContainerDrawingFiller(graphView, containerDrawing, addLabel = true)

    private fun createGraphView(): GraphView =
        GraphViewModule.graphViewFactory.create(null).apply {
            add(TestGraphPortView.input<Boolean>("I"))
            add(TestGraphPortView.output<Boolean>("O"))
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