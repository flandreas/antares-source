package io.antarescircuit.antares.view.gate

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.Handedness
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TriStateBufferGateViewTest : AbstractCircuitTest() {

	private lateinit var builder: GraphViewBuilder<DigitalSignal>
	private lateinit var gateView: TriStateBufferGateView
	private lateinit var edgeView: EdgeView<*>

	override fun getCircuitView(): GraphView = builder.graphView

	@BeforeTest
	fun setupCircuit() {
		builder = GraphViewBuilder("test")
		gateView = builder.addVerticeView(TriStateBufferGateView())
		edgeView = builder.connectOutputOpen(gateView, Point2D(100, 0))
	}

	@Test
	fun shouldChangeHandedness() {
		val location = gateView.location.add(gateView.getPortView(gateView.model.getOutputPort())!!.location)
		assertEquals(location, edgeView.polyline.getPointAt(0))

		gateView.handedness = Handedness.LEFT

		assertEquals(location, edgeView.polyline.getPointAt(0))
		assertEquals(Direction.NORTH, gateView.getPortView(gateView.model.getEnablePort())!!.direction)
	}
}