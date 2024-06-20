package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.SnapManager
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EdgeViewSnapLocatorTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val ev = GraphViewModule.getEdgeViewFactory().createEdgeView<Boolean>(mock())

	@Test
	fun shouldSnapOnEdgeView() {
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))

		val result = EdgeViewSnapLocator.snap(ev, 50.0, 0.0)!!

		assertEquals(0, result.segmentIndex)
		assertEquals(50.0, result.x)
		assertEquals(0.0, result.y)
	}

	@Test
	fun shouldSnapNearEdgeView() {
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))

		val result = EdgeViewSnapLocator.snap(ev, 50.0, 2.0)!!

		assertEquals(0, result.segmentIndex)
		assertEquals(50.0, result.x)
		assertEquals(0.0, result.y)
	}

	@Test
	fun shouldNotSnapFarFromEdgeView() {
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))

		val result = EdgeViewSnapLocator.snap(ev, 50.0, 10.0)

		assertNull(result)
	}

	@Test
	fun shouldSnapToGridOnEdgeView() {
		val snapManager = mock<SnapManager>()
		every { snapManager.snap(any(), any(), any()) } returns Point2D(5, 0)

		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))

		val locatorResult = EdgeViewSnapLocator.snap(ev, 50.0, 1.0, snapManager)!!

		assertEquals(0, locatorResult.segmentIndex)
		assertEquals(55.0, locatorResult.x)
		assertEquals(0.0, locatorResult.y)
	}

	@Test
	fun shouldNotSnapNearStartOfOriginSegment() {
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))

		val result = EdgeViewSnapLocator.snap(ev, EdgeViewSnapLocator.FORBIDDEN_END_AREA, 0.0)

		assertNull(result)
	}

	@Test
	fun shouldNotSnapNearEndOfDestinationSegment() {
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))

		val result = EdgeViewSnapLocator.snap(ev, 100.0 - EdgeViewSnapLocator.FORBIDDEN_END_AREA, 0.0)

		assertNull(result)
	}

}