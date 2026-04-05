package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProbeViewIntegrationTest : AbstractCircuitTest() {

	private lateinit var builder: GraphViewBuilder<DigitalSignal>
	private lateinit var probeView1: ProbeView
	private lateinit var probeView2: ProbeView
	private lateinit var switchView: SwitchView

	override fun getCircuitView(): GraphView = builder.graphView

	@BeforeTest
	fun setupCircuit() {
		builder = GraphViewBuilder("test")
		probeView1 = builder.addVerticeView(ProbeView())
		probeView2 = builder.addVerticeView(ProbeView())
		switchView = builder.addVerticeView(SwitchView())
		builder.connect(switchView, probeView2)
	}

	@Test
	fun shouldBeUndefinedAtStartup() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_1), probeView1.model.signal)
	}

	@Test
	fun shouldHaveIncomingSignalAtStartup() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(false), probeView2.model.signal)
	}
}