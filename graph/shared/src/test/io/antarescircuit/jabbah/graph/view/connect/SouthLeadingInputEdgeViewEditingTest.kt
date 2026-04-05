package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.view.AbstractGraphViewEditingTest
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

class SouthLeadingInputEdgeViewEditingTest : AbstractGraphViewEditingTest() {

	private lateinit var vv1: TestVerticeView
	private lateinit var vv2: TestVerticeView
	private lateinit var edgeView: EdgeView<Boolean>

	override fun setupCircuit() {
		vv1 = builder.addVerticeView(TestVerticeView.createSouthInputVerticeView("vv1", 0, 0))
		vv2 = builder.addVerticeView(TestVerticeView.createSouthInputVerticeView("vv2", 0, 100))
		edgeView = builder.connectInputOpen(vv1, Point2D(-100, 300))
	}

	private fun adjustEdgeView() {
		driver.mouseMoveTo(0, 200)
		driver.pressMouseAt(0, 200)
		driver.dragMouseTo(-100, 200)
		driver.releaseMouseAt(-100, 200)
	}

	@Test
	fun edgeViewShouldLeadToSouth() {
		adjustEdgeView()
		assertEquals(Point2D(-100, 300), edgeView.polyline.getPointAt(0))
		assertEquals(Point2D(-100, 10), edgeView.polyline.getPointAt(1))
		assertEquals(Point2D(0, 10), edgeView.polyline.getPointAt(2))
		assertEquals(Point2D(0, 0), edgeView.polyline.getPointAt(3))
	}

	@Test
	fun shouldSplitEdgeView() {
		adjustEdgeView()

		val result = builder.split(edgeView, 0, Point2D(-100, 200), null as VerticeView<*>?)
		
		assertEquals(Point2D(-100, 200), result.tailEdgeView.polyline.getPointAt(0))
		assertEquals(Point2D(-100, 10), result.tailEdgeView.polyline.getPointAt(1))
		assertEquals(Point2D(0, 10), result.tailEdgeView.polyline.getPointAt(2))
		assertEquals(Point2D(0, 0), result.tailEdgeView.polyline.getPointAt(3))
	}
}