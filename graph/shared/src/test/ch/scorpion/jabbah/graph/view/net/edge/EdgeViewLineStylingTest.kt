package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import kotlin.test.Test
import kotlin.test.assertEquals

class EdgeViewLineStylingTest {

    companion object {
        init {
            GraphViewTestRule.configure()
        }
    }

    @Test
    fun shouldUpdateBoundingBoxIfUnconnected() {
        val ev = EdgeViewImpl<Boolean>()
            .addSegmentPoint(Point2D(-252, -28))
            .addSegmentPoint(Point2D(-420, -28))
            .addSegmentPoint(Point2D(-420, -182))
            .addSegmentPoint(Point2D(-336, -182))

        assertEquals(-420.0 - ev.stroke.width - 1, ev.boundingBox.x)
        assertEquals(-182.0 - EdgeEndpointView.SIZE_HALF - ev.stroke.width - 1, ev.boundingBox.y)
        assertEquals(420.0 - 252.0 + 2 + EdgeEndpointView.SIZE_HALF + ev.stroke.width + ev.originEndpointView.stroke.width, ev.boundingBox.width)
        assertEquals(182.0 - 28.0 + 2 + 2 * EdgeEndpointView.SIZE_HALF + 2 * ev.originEndpointView.stroke.width, ev.boundingBox.height)
    }
}