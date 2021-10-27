package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewSpaceTest {

	@Test
	fun shouldReduceAtTop() {
		val viewSpace = ViewSpace(Dimension2D(200, 100))
		viewSpace.reduceTop(20)
		assertEquals(Rectangle2D(0, 20, 200, 80), viewSpace.area)
	}

	@Test
	fun shouldRemoveReductionAtTop() {
		val viewSpace = ViewSpace(Dimension2D(200, 100))
		viewSpace.reduceTop(20)
		viewSpace.removeTopReduction(20)
		assertEquals(Rectangle2D(0, 0, 200, 100), viewSpace.area)
	}

	@Test
	fun shouldFirePropertyEvent() {
		val viewSpace = ViewSpace(Dimension2D(200, 100))
		var area: Rectangle2D? = null
		viewSpace.addPropertyChangeListener {
			area = it.newValue
		}

		viewSpace.reduceTop(20)

		assertEquals(Rectangle2D(0, 20, 200, 80), area)
	}

	@Test
	fun shouldOnlyRemoveOneOfManyEqualTopReductions() {
		val viewSpace = ViewSpace(Dimension2D(200, 100))
		viewSpace.reduceTop(20)
		viewSpace.reduceTop(20)

		viewSpace.removeTopReduction(20)
		assertEquals(Rectangle2D(0, 20, 200, 80), viewSpace.area)

		viewSpace.removeTopReduction(20)
		assertEquals(Rectangle2D(0, 0, 200, 100), viewSpace.area)
	}
}