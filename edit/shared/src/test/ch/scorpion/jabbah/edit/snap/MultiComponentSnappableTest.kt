package ch.scorpion.jabbah.edit.snap

import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.style.StyleProviderMockBuilder
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [MultiComponentSnappable].
 */
class MultiComponentSnappableTest {

    companion object {
        @ClassRule @JvmField
        val drawTestRule = DrawTestRule()
    }

    @Test
    fun shouldSnapX() {
        val snapX = MultiComponentSnappable(listOf(
                RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(0.0, 0.0, 20.0, 10.0)),
                RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(100.0, 200.0, 20.0, 10.0))
        )).snappableX

        // Didn't succeed in using hamcrest's containsInAnyOrder() method due to type inference problems

        assertThat(snapX.size, `is`(6))
        assertThat(snapX[0], `is`(0.0))
        assertThat(snapX[1], `is`(10.0))
        assertThat(snapX[2], `is`(20.0))
        assertThat(snapX[3], `is`(100.0))
        assertThat(snapX[4], `is`(110.0))
        assertThat(snapX[5], `is`(120.0))
    }

    @Test
    fun shouldSnapY() {
        val snapY = MultiComponentSnappable(listOf(
                RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(0.0, 0.0, 20.0, 10.0)),
                RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(100.0, 200.0, 20.0, 10.0))
        )).snappableY

        // Didn't succeed in using hamcrest's containsInAnyOrder() method due to type inference problems

        assertThat(snapY.size, `is`(6))
        assertThat(snapY[0], `is`(0.0))
        assertThat(snapY[1], `is`(5.0))
        assertThat(snapY[2], `is`(10.0))
        assertThat(snapY[3], `is`(200.0))
        assertThat(snapY[4], `is`(205.0))
        assertThat(snapY[5], `is`(210.0))
    }
}