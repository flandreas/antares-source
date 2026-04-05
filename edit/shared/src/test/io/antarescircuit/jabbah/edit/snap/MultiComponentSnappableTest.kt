package io.antarescircuit.jabbah.edit.snap

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.edit.EditTestRule
import io.antarescircuit.jabbah.edit.StyleProviderMockBuilder
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [MultiComponentSnappable].
 */
class MultiComponentSnappableTest {

	@BeforeTest
	fun setup() {
		EditTestRule.configure()
	}

	@Test
	fun shouldSnapX() {
		val snapX = MultiComponentSnappable(listOf(
			RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(0.0, 0.0, 20.0, 10.0)),
			RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(100.0, 200.0, 20.0, 10.0))
		)).snappableX

		assertEquals(6, snapX.size)
		assertEquals(0.0, snapX[0].x)
		assertEquals(10.0, snapX[1].x)
		assertEquals(20.0, snapX[2].x)
		assertEquals(100.0, snapX[3].x)
		assertEquals(110.0, snapX[4].x)
		assertEquals(120.0, snapX[5].x)
	}

	@Test
	fun shouldSnapY() {
		val snapY = MultiComponentSnappable(listOf(
			RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(0.0, 0.0, 20.0, 10.0)),
			RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(100.0, 200.0, 20.0, 10.0))
		)).snappableY

		assertEquals(6, snapY.size)
		assertEquals(0.0, snapY[0].y)
		assertEquals(5.0, snapY[1].y)
		assertEquals(10.0, snapY[2].y)
		assertEquals(200.0, snapY[3].y)
		assertEquals(205.0, snapY[4].y)
		assertEquals(210.0, snapY[5].y)
	}
}