package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.analog.AnalogGraph
import io.antarescircuit.antares.model.analog.AnalogNet
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.DrawableAdapter
import io.antarescircuit.jabbah.draw.DrawableEvent
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CurrentFlowAnimationTickTest {

	private val systemSpeed = SystemSpeed()
	private val speedCategory = CurrentSystemSpeedCategory(systemSpeed)
	private lateinit var graph: AnalogGraphView
	private lateinit var firstEdge: AnalogEdgeView
	private lateinit var secondEdge: AnalogEdgeView
	private var invalidations = 0
	private var redraws = 0

	@BeforeTest
	fun setup() {
		AntaresTestRule.configure()
		graph = AnalogGraphView(AnalogGraph())
		firstEdge = createEdge()
		secondEdge = createEdge()
		graph.add(firstEdge)
		graph.add(secondEdge)
		(firstEdge.net as AnalogNet).setNode(firstEdge.id, 0, 0)
		(secondEdge.net as AnalogNet).setNode(secondEdge.id, 0, 0)
		graph.addDrawableListener(object : DrawableAdapter() {
			override fun drawableInvalidated(event: DrawableEvent) {
				invalidations++
			}

			override fun drawableRequestRedraw(event: DrawableEvent) {
				redraws++
			}
		})
	}

	@AfterTest
	fun teardown() {
		graph.dispose()
	}

	@Test
	fun shouldAdvanceEveryMovingEdgeAndRequestOneRedraw() {
		firstEdge.current = 1.0
		secondEdge.current = 2.0

		graph.currentFlowAnimationTick(speedCategory)

		assertTrue(firstEdge.animationOffset > 0.0)
		assertTrue(secondEdge.animationOffset > 0.0)
		assertEquals(1, invalidations)
		assertEquals(1, redraws)
	}

	@Test
	fun shouldKeepRefreshingStationaryEdgesForVoltageChanges() {
		graph.currentFlowAnimationTick(speedCategory)

		assertEquals(0.0, firstEdge.animationOffset)
		assertEquals(0.0, secondEdge.animationOffset)
		assertEquals(1, invalidations)
		assertEquals(1, redraws)
	}

	@Test
	fun shouldKeepOffsetsWhilePaused() {
		secondEdge.current = 1.0
		graph.currentFlowAnimationTick(speedCategory)
		val offset = secondEdge.animationOffset
		invalidations = 0
		redraws = 0
		systemSpeed.pause()

		graph.currentFlowAnimationTick(speedCategory)

		assertEquals(offset, secondEdge.animationOffset)
		assertEquals(1, invalidations)
		assertEquals(1, redraws)
	}

	private fun createEdge(): AnalogEdgeView = AnalogEdgeView().apply {
		addSegmentPoint(Point2D.ZERO)
		addSegmentPoint(Point2D(100, 0))
	}
}
