package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.net.NetSignalApplierStrategy
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import kotlin.test.*

class WiredOrIntegrationTest : AbstractCircuitTest() {

	private lateinit var builder: GraphViewBuilder<DigitalSignal>
	private lateinit var switchViewA: SwitchView
	private lateinit var switchViewB: SwitchView
	private lateinit var ledView: LEDView
	private lateinit var edgeView: EdgeView<DigitalSignal>

	override fun getCircuitView(): GraphView = builder.graphView

	@BeforeTest
	fun setupCircuit() {
		builder = GraphViewBuilder("test")
		switchViewA = builder.addVerticeView(SwitchView())
		switchViewA.location = Point2D.ZERO
		switchViewB = builder.addVerticeView(SwitchView())
		switchViewB.location = Point2D(0, 100)
		ledView = builder.addVerticeView(LEDView())
		ledView.location = Point2D(100, 0)
		edgeView = builder.connect(switchViewA, ledView)
		builder.split(edgeView, 0, Point2D(50, 0), switchViewB)

		(getCircuitView().graph as DigitalGraph).netSignalApplierStrategy = NetSignalApplierStrategy.WiredOr
	}

	@Test
	fun shouldCalculateWiredOr() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		switchViewA.model.toggle(scheduler)
		proceedUntilQueueIsEmpty()

		assertNull(edgeView.net?.executionError)
		assertTrue(ledView.model.isOn)
	}

	@Test
	fun shouldReturnFromOneTrue() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		switchViewA.model.on(scheduler)
		proceedUntilQueueIsEmpty()

		switchViewA.model.off(scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(false), edgeView.model.signal)
		assertFalse(ledView.model.isOn)
	}

	@Test
	fun shouldReturnFromBothTrue() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		switchViewA.model.on(scheduler)
		proceedUntilQueueIsEmpty()

		switchViewB.model.on(scheduler)
		proceedUntilQueueIsEmpty()

		switchViewB.model.off(scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(true), edgeView.model.signal)
		assertTrue(ledView.model.isOn)
	}
}