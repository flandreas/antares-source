package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.port.TestPortView
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [AbstractRectangularVerticeView].
 */
class AbstractRectangularVerticeViewTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphViewTestRule()
    }

    @Before
    fun setup() {
        TestTranslationsBuilder()
            .withResource("test.name")
                .withResource("test.desc")
    }

    /** ---- Without rectangle offset */

    @Test
    fun testNoOffsetWithoutPorts() {
        val rectangle = TestRectangleView()
        rectangle.setBounds(0.0, 0.0, 100.0, 50.0)

        assertThat(rectangle.x, `is`(0.0))
        assertThat(rectangle.y, `is`(0.0))
        assertThat(rectangle.width, `is`(100.0))
        assertThat(rectangle.height, `is`(50.0))
        assertThat(rectangle.location.x, `is`(0.0))
        assertThat(rectangle.location.y, `is`(0.0))
        assertThat(rectangle.bounds.boundingBox as Rectangle2D, `is`(Rectangle2D(0, 0, 100, 50)))
        assertThat(rectangle.boundingBox, `is`(Rectangle2D(0, 0, 100, 50)))
    }

    @Test
    fun testNoOffsetWithPorts() {
        val rectangle = TestRectangleView()
        rectangle.setBounds(0.0, 0.0, 100.0, 50.0)
        val portView = TestPortView<Boolean>(rectangle.model!!.getInput(), Direction.WEST, PortLabelPosition.INTERNAL, 20)
        portView.setLocation(0.0, 25.0)
        rectangle.addPortView(portView)

        assertThat(rectangle.x, `is`(0.0))
        assertThat(rectangle.y, `is`(0.0))
        assertThat(rectangle.width, `is`(100.0))
        assertThat(rectangle.height, `is`(50.0))
        assertThat(rectangle.location.x, `is`(0.0))
        assertThat(rectangle.location.y, `is`(0.0))
        assertThat(rectangle.bounds.boundingBox as Rectangle2D, `is`(Rectangle2D(0, 0, 100, 50)))
        assertThat(rectangle.boundingBox, `is`(Rectangle2D(-20, 0, 120, 50)))
    }

    /** ---- With rectangle offset */

    @Test
    fun testOffsetWithoutPorts() {
        val rectangle = TestRectangleView()
        rectangle.setBounds(0.0, 0.0, 100.0, 50.0)
        rectangle.location = Point2D(-20, 25)

        assertThat(rectangle.x, `is`(0.0))
        assertThat(rectangle.y, `is`(0.0))
        assertThat(rectangle.width, `is`(100.0))
        assertThat(rectangle.height, `is`(50.0))
        assertThat(rectangle.location.x, `is`(-20.0))
        assertThat(rectangle.location.y, `is`(25.0))
        assertThat(rectangle.bounds.boundingBox as Rectangle2D, `is`(Rectangle2D(0, 0, 100, 50)))
        assertThat(rectangle.boundingBox, `is`(Rectangle2D(-20, 25, 100, 50)))
    }

    @Test
    fun testOffsetWithPorts() {
        val rectangle = TestRectangleView()
        rectangle.setBounds(0.0, 0.0, 100.0, 50.0)
        rectangle.location = Point2D(-20, 25)
        val portView = TestPortView<Boolean>(rectangle.model!!.getInput(), Direction.WEST, PortLabelPosition.INTERNAL, 20)
        portView.setLocation(0.0, 25.0)
        rectangle.addPortView(portView)

        assertThat(rectangle.x, `is`(0.0))
        assertThat(rectangle.y, `is`(0.0))
        assertThat(rectangle.width, `is`(100.0))
        assertThat(rectangle.height, `is`(50.0))
        assertThat(rectangle.location.x, `is`(-20.0))
        assertThat(rectangle.location.y, `is`(25.0))
        assertThat(rectangle.bounds.boundingBox as Rectangle2D, `is`(Rectangle2D(0, 0, 100, 50)))
        assertThat(rectangle.boundingBox, `is`(Rectangle2D(-40, 25, 120, 50)))
    }

    private class TestRectangleView : AbstractRectangularVerticeView<TestVertice>("test", TestVertice()) {
        override val lineWidth: Double
            get() = 0.0
    }
}