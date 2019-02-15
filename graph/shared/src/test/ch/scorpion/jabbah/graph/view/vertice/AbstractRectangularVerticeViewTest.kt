package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.port.TestPortView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [AbstractRectangularVerticeView].
 */
class AbstractRectangularVerticeViewTest {

    companion object {
	    init {
	    	GraphViewTestRule.configure()
	    }
    }

    @BeforeTest
    fun setup() {
	    TestTranslationsBuilder().withAnyKey()
    }

    /** ---- Without rectangle offset */

    @Test
    fun testNoOffsetWithoutPorts() {
        val rectangle = TestRectangleView()
        rectangle.setBounds(0.0, 0.0, 100.0, 50.0)

        assertEquals(0.0, rectangle.x)
        assertEquals(0.0, rectangle.y)
        assertEquals(100.0, rectangle.width)
        assertEquals(50.0, rectangle.height)
        assertEquals(0.0, rectangle.location.x)
        assertEquals(0.0, rectangle.location.y)
        assertEquals(Rectangle2D(0, 0, 100, 50), rectangle.bounds.boundingBox as Rectangle2D)
        assertEquals(Rectangle2D(0, 0, 100, 50), rectangle.boundingBox)
    }

    @Test
    fun testNoOffsetWithPorts() {
        val rectangle = TestRectangleView()
        rectangle.setBounds(0.0, 0.0, 100.0, 50.0)
        val portView = TestPortView<Boolean>(rectangle.model!!.getInput(), Direction.WEST, PortLabelPosition.INTERNAL, 20)
        portView.setLocation(0.0, 25.0)
        rectangle.addPortView(portView)

        assertEquals(0.0, rectangle.x)
        assertEquals(0.0, rectangle.y)
        assertEquals(100.0, rectangle.width)
        assertEquals(50.0, rectangle.height)
        assertEquals(0.0, rectangle.location.x)
        assertEquals(0.0, rectangle.location.y)
        assertEquals(Rectangle2D(0, 0, 100, 50), rectangle.bounds.boundingBox as Rectangle2D)
        assertEquals(Rectangle2D(-20, 0, 120, 50), rectangle.boundingBox)
    }

    /** ---- With rectangle offset */

    @Test
    fun testOffsetWithoutPorts() {
        val rectangle = TestRectangleView()
        rectangle.setBounds(0.0, 0.0, 100.0, 50.0)
        rectangle.location = Point2D(-20, 25)

        assertEquals(0.0, rectangle.x)
        assertEquals(0.0, rectangle.y)
        assertEquals(100.0, rectangle.width)
        assertEquals(50.0, rectangle.height)
        assertEquals(-20.0, rectangle.location.x)
        assertEquals(25.0, rectangle.location.y)
        assertEquals(Rectangle2D(0, 0, 100, 50), rectangle.bounds.boundingBox as Rectangle2D)
        assertEquals(Rectangle2D(-20, 25, 100, 50), rectangle.boundingBox)
    }

    @Test
    fun testOffsetWithPorts() {
        val rectangle = TestRectangleView()
        rectangle.setBounds(0.0, 0.0, 100.0, 50.0)
        rectangle.location = Point2D(-20, 25)
        val portView = TestPortView<Boolean>(rectangle.model!!.getInput(), Direction.WEST, PortLabelPosition.INTERNAL, 20)
        portView.setLocation(0.0, 25.0)
        rectangle.addPortView(portView)

        assertEquals(0.0, rectangle.x)
        assertEquals(0.0, rectangle.y)
        assertEquals(100.0, rectangle.width)
        assertEquals(50.0, rectangle.height)
        assertEquals(-20.0, rectangle.location.x)
        assertEquals(25.0, rectangle.location.y)
        assertEquals(Rectangle2D(0, 0, 100, 50), rectangle.bounds.boundingBox as Rectangle2D)
        assertEquals(Rectangle2D(-40, 25, 120, 50), rectangle.boundingBox)
    }

    private class TestRectangleView : AbstractRectangularVerticeView<TestVertice>(model = TestVertice()) {
        override val lineWidth: Double
            get() = 0.0
    }
}