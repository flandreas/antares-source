package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

class EdgeViewLayoutImplTest {

	private val builder: GraphViewBuilder<Boolean>

	init {
		GraphViewTestRule.configure()
		builder = GraphViewBuilder()
	}

	/** Regression test for GitHub #143. */
	@Test
	fun shouldLayoutDestinationAdjustedNone() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 200, Direction.WEST))
		val ev = builder.connect(v1, v2)

		ev.moveSegment(2, 50.0)
		assertEquals(6, ev.polyline.pointsCount)

		ev.layout.isAdjusted = true
		ev.layout.type = LayoutType.NONE

		v2.moveBy(44.0, 33.0)

		assertEquals(6, ev.polyline.pointsCount)

		assertEquals(Point2D(100, 100), ev.polyline.getPointAt(0))
		assertEquals(Point2D(150, 100), ev.polyline.getPointAt(1))
		assertEquals(Point2D(150, 250), ev.polyline.getPointAt(2))
		assertEquals(Point2D(180, 250), ev.polyline.getPointAt(3))
		assertEquals(Point2D(180, 200), ev.polyline.getPointAt(4))
		assertEquals(Point2D(244, 233), ev.polyline.getPointAt(5))
	}

	private fun createVerticeView(x: Int, y: Int, dir: Direction): TestVerticeView =
		TestVerticeView(loc = Point2D(x, y), inputDirection = dir, portViewLength = 20)

}