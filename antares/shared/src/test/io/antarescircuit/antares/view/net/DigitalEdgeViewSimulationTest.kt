package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.graph.view.GraphView
import kotlin.test.Test
import kotlin.test.assertEquals

class DigitalEdgeViewSimulationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView

	override fun getCircuitView(): GraphView = circuitView

	@Test
	fun unconnectedEdgeViewShouldBeUndefinedAtStartup() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		val edgeView = builder.add(DigitalEdgeView())
		circuitView = builder.graphView

		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_1), edgeView.model.signal)
	}
}