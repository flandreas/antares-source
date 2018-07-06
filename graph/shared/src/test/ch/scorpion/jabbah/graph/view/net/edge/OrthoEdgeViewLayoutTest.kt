package ch.scorpion.jabbah.graph.view.net.edge

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [OrthoEdgeViewLayout].
 */
class OrthoEdgeViewLayoutTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphViewTestRule()
    }

    private lateinit var graphView: GraphView<*>
    private lateinit var layout: OrthoEdgeViewLayout

    @Before
    fun setup() {
        layout = OrthoEdgeViewLayout()
        graphView = mock()
        whenever(graphView.snapper).thenReturn(null)
    }

    @Test
    fun layoutEastEast() {
        val points = layout.layout(
                null,
                graphView,
                LayoutBoundary(
                        point = Point2D(0, 0),
                        directions = setOf(Direction.EAST),
                        isPort = true),
                LayoutBoundary(
                        point = Point2D(100, 100),
                        directions = setOf(Direction.EAST),
                        isPort = true))

        assertThat(points.size, `is`(4))
        assertThat(points[0], `is`(Point2D(0, 0)))
        assertThat(points[1], `is`(Point2D(50, 0)))
        assertThat(points[2], `is`(Point2D(50, 100)))
        assertThat(points[3], `is`(Point2D(100, 100)))
    }

    @Test
    fun layoutEastEastDegenerated() {
        val points = layout.layout(
                null,
                graphView,
                LayoutBoundary(
                        point = Point2D(100, 0),
                        directions = setOf(Direction.EAST),
                        isPort = true),
                LayoutBoundary(
                        point = Point2D(0, 0),
                        directions = setOf(Direction.EAST),
                        isPort = true))

		assertThat(points.size, `is`(2))
		assertThat(points[0], `is`(Point2D(100, 0)))
		assertThat(points[1], `is`(Point2D(0, 0)))
    }

    @Test
    fun shouldNotFailWithEmptyPolyline() {
        val points = layout.layout(
                null,
                graphView,
                LayoutBoundary(
                        point = Point2D(0, 0),
                        directions = setOf(Direction.EAST),
                        isPort = true),
                LayoutBoundary(
                        point = Point2D(0, 0),
                        directions = setOf(Direction.EAST),
                        isPort = true))

        assertThat(points.size, `is`(2))
    }

    @Test
    fun layoutSouthOpen() {
        val points = layout.layout(
                null,
                graphView,
                LayoutBoundary(
                        point = Point2D(0, 0),
                        directions = setOf(Direction.EAST),
                        isPort = true),
                LayoutBoundary(
                        point = Point2D(100, 100),
                        directions = setOf(Direction.WEST),
                        isPort = false))

        assertThat(points.size, `is`(3))
    }
}
