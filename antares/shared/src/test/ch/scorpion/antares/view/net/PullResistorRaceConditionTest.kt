package ch.scorpion.antares.view.net

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.net.PullDirection
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression test for GitHub issue #150.*/
class PullResistorRaceConditionTest : AbstractCircuitTest() {

	private lateinit var builder: GraphViewBuilder<DigitalSignal>
	private lateinit var pullResistorView: PullResistorView
	private lateinit var inOutView: CircuitInOutView

	override fun getCircuitView(): GraphView = builder.graphView

	@BeforeTest
	fun setupCircuit() {
		builder = GraphViewBuilder("test")
	}

	@Test
	fun shouldForwardPullResistorValueAtStartUp() {
		pullResistorView = builder.addVerticeView(PullResistorView(PullDirection.LOW))
		inOutView = builder.addVerticeView(CircuitInOutView(model = CircuitInOutImpl(portType = PortType.INOUT)))
		builder.connect(pullResistorView, inOutView)

		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals((Word.of(false)), inOutView.model.getPort<DigitalSignal>().net?.signal)
		assertEquals((Word.of(false)), inOutView.model.signal)
	}

	@Test
	fun shouldNotDependOnAddOrder() {
		inOutView = builder.addVerticeView(CircuitInOutView(model = CircuitInOutImpl(portType = PortType.INOUT)))
		pullResistorView = builder.addVerticeView(PullResistorView(PullDirection.LOW))
		builder.connect(pullResistorView, inOutView)

		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals((Word.of(false)), inOutView.model.getPort<DigitalSignal>().net?.signal)
		assertEquals((Word.of(false)), inOutView.model.signal)
	}
}