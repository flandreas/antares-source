package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [PointRange].
 */
class PointRangeTest {

    @Before
    fun init() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldSequenceForward() {
        val range = PointRange(Point2D(0, 0), Point2D(0, 3))

        assertThat(range.size, `is`(3.0))
        assertThat(range.getNext(1.0), `is`(Point2D(0, 0)))
        assertThat(range.getNext(1.0), `is`(Point2D(0, 1)))
        assertThat(range.getNext(1.0), `is`(Point2D(0, 2)))
        assertThat(range.getNext(1.0), `is`(Point2D(0, 3)))
        assertThat(range.hasNext(), `is`(false))
    }

    @Test
    fun shouldSequenceBackwards() {
        val range = PointRange(Point2D(0, 0), Point2D(0, -3))

        assertThat(range.size, `is`(3.0))
        assertThat(range.getNext(1.0), `is`(Point2D(0, 0)))
        assertThat(range.getNext(1.0), `is`(Point2D(0, -1)))
        assertThat(range.getNext(1.0), `is`(Point2D(0, -2)))
        assertThat(range.getNext(1.0), `is`(Point2D(0, -3)))
        assertThat(range.hasNext(), `is`(false))
    }

    @Test
    fun shouldHandleZeroSize() {
        val range = PointRange(Point2D(10, 10), Point2D(10, 10))

        assertThat(range.size, `is`(0.0))
        assertThat(range.getCurrent(), `is`(Point2D(10, 10)))
        assertThat(range.hasNext(), `is`(true));
        assertThat(range.getNext(1.09), `is`(Point2D(10, 10)));
        assertThat(range.hasNext(), `is`(false));
    }
}