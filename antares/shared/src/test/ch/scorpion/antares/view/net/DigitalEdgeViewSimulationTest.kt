package ch.scorpion.antares.view.net

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.graph.view.GraphView
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