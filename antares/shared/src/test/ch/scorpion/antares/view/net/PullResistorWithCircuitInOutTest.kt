package ch.scorpion.antares.view.net

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.net.PullDirection
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
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