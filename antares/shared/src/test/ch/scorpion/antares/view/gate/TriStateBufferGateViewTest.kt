package ch.scorpion.antares.view.gate

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.Handedness
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
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