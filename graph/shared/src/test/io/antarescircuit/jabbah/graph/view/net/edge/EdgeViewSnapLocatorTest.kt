package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.SnapManager
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.graph.view.EdgeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EdgeViewSnapLocatorTest {

	private val ev: EdgeView<*>

	init {
		GraphViewTestRule.configure()
		ev = GraphViewModule.getEdgeViewFactory().createEdgeView<Boolean>(mock())
	}

	@Test
	fun shouldSnapOnEdgeView() {
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))

		val result = EdgeViewSnapLocator.snap(ev, 50.0, 0.0, outgoing = true)!!

		assertEquals(0, result.segmentIndex)
		assertEquals(50.0, result.location.x)
		assertEquals(0.0, result.location.y)
	}

	@Test
	fun shouldSnapNearEdgeView() {
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))

		val result = EdgeViewSnapLocator.snap(ev, 50.0, 2.0, outgoing = true)!!

		assertEquals(0, result.segmentIndex)
		assertEquals(50.0, result.location.x)
		assertEquals(0.0, result.location.y)
	}

	@Test
	fun shouldNotSnapFarFromEdgeView() {
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))

		val result = EdgeViewSnapLocator.snap(ev, 50.0, 10.0, outgoing = true)

		assertNull(result)
	}

	@Test
	fun shouldSnapToGridOnEdgeView() {
		val snapManager = mock<SnapManager>()
		every { snapManager.snap(any(), any(), any()) } returns Point2D(5, 0)

		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))

		val locatorResult = EdgeViewSnapLocator.snap(ev, 50.0, 1.0, outgoing = true, snapManager)!!

		assertEquals(0, locatorResult.segmentIndex)
		assertEquals(55.0, locatorResult.location.x)
		assertEquals(0.0, locatorResult.location.y)
	}

	@Test
	fun shouldNotSnapNearStartOfOriginSegment() {
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))

		val result = EdgeViewSnapLocator.snap(ev, EdgeViewSnapLocator.FORBIDDEN_END_AREA, 0.0, outgoing = true)

		assertNull(result)
	}

	@Test
	fun shouldNotSnapNearEndOfDestinationSegment() {
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))

		val result = EdgeViewSnapLocator.snap(ev, 100.0 - EdgeViewSnapLocator.FORBIDDEN_END_AREA, 0.0, outgoing = true)

		assertNull(result)
	}

	@Test
	fun shouldYieldFreeCornerDirectionsNW() {
		ev
			.addSegmentPoint(Point2D(0, 100))
			.addSegmentPoint(Point2D(0, 0))
			.addSegmentPoint(Point2D(100, 0))

		val result = EdgeViewSnapLocator.snap(ev, 0.0, 0.0, outgoing = true)!!

		assertTrue(result.directions.contains(Direction.NORTH))
		assertTrue(result.directions.contains(Direction.WEST))
	}

}