package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.net.PullDirection
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/** Regression test for GitHub issue #150.*/
class PullResistorWithCircuitInOutTest : AbstractCircuitTest() {

	private lateinit var builder: GraphViewBuilder<DigitalSignal>
	private lateinit var pullResistorView: PullResistorView
	private lateinit var inOutView: DigitalCircuitInOutView
	private lateinit var ledView: LEDView

	override fun getCircuitView(): GraphView = builder.graphView

	@BeforeTest
	fun setupCircuit() {
		builder = GraphViewBuilder("test")

		inOutView = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INOUT)))
		inOutView.orientation = Direction.SOUTH
		inOutView.location = Point2D(0, 100)

		pullResistorView = builder.addVerticeView(PullResistorView(PullDirection.HIGH))
		pullResistorView.orientation = Direction.SOUTH
		pullResistorView.location = Point2D.ZERO

		ledView = builder.addVerticeView(LEDView())
		ledView.orientation = Direction.EAST
		ledView.location = Point2D(100, 50)

		val ev = builder.connect(pullResistorView, inOutView)
		builder.split(ev, 0, Point2D(0, 50), ledView)
	}

	@Test
	fun shouldForwardPullResistorValueAtStartUp() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertTrue(ledView.model.isOn)
	}
}