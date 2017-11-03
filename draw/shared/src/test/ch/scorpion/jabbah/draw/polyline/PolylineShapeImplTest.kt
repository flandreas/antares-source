package ch.scorpion.jabbah.draw.polyline

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawTestRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [PolylineShape].
 */
class PolylineShapeImplTest {

    companion object {
        @ClassRule @JvmField
        val drawTestRule = DrawTestRule()
    }

    @Test
    fun shouldCalculateLength() {
        assertThat(PolylineShapeImpl().addPoint(0, 0).getLength(), `is`(0.0))
        assertThat(PolylineShapeImpl().addPoint(0, 0).addPoint(100, 0).getLength(), `is`(100.0))
        assertThat(PolylineShapeImpl().addPoint(0, 0).addPoint(100, 0).addPoint(100, 200).getLength(), `is`(300.0))
    }

    @Test
    fun shouldFindSegment() {
        val polyline = PolylineShapeImpl().addPoint(0, 0).addPoint(100, 0).addPoint(100, 100)
        assertThat(polyline.findSegment(50.0, 0.0, 5), `is`(0))
        assertThat(polyline.findSegment(100.0, 50.0, 5), `is`(1))
        assertThat(polyline.findSegment(0.0, 100.0, 5), `is`(nullValue()))
    }

    @Test
    fun shouldMovePointsWhenSettingLocation() {
        val polyline = PolylineShapeImpl().addPoint(0, 0).addPoint(100, 0).addPoint(100, 100)
        polyline.setLocation(50.0, 50.0)
        assertThat(polyline.getPointAt(0), `is`(Point2D(50, 50)))
        assertThat(polyline.getPointAt(1), `is`(Point2D(150, 50)))
        assertThat(polyline.getPointAt(2), `is`(Point2D(150, 150)))
    }
}