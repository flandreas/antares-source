package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.net.PullDirection
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression test for GitHub issue #150.*/
class PullResistorRaceConditionTest : AbstractCircuitTest() {

	private lateinit var builder: GraphViewBuilder<DigitalSignal>
	private lateinit var pullResistorView: PullResistorView
	private lateinit var inOutView: DigitalCircuitInOutView

	override fun getCircuitView(): GraphView = builder.graphView

	@BeforeTest
	fun setupCircuit() {
		builder = GraphViewBuilder("test")
	}

	@Test
	fun shouldForwardPullResistorValueAtStartUp() {
		pullResistorView = builder.addVerticeView(PullResistorView(PullDirection.LOW))
		inOutView = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INOUT)))
		builder.connect(pullResistorView, inOutView)

		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals((DigitalSignalFactory.of(false)), inOutView.model.getPort<DigitalSignal>().net?.signal)
		assertEquals((DigitalSignalFactory.of(false)), inOutView.model.signal)
	}

	@Test
	fun shouldNotDependOnAddOrder() {
		inOutView = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INOUT)))
		pullResistorView = builder.addVerticeView(PullResistorView(PullDirection.LOW))
		builder.connect(pullResistorView, inOutView)

		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals((DigitalSignalFactory.of(false)), inOutView.model.getPort<DigitalSignal>().net?.signal)
		assertEquals((DigitalSignalFactory.of(false)), inOutView.model.signal)
	}
}