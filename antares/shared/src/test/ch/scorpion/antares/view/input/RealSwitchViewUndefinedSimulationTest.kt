package ch.scorpion.antares.view.input

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the simulation behaviour of [RealSwitchView] with a [CircuitInOutView] that
 * can go into state "undefined".
 */
class RealSwitchViewUndefinedSimulationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var switchView: SwitchView
	private lateinit var realSwitchView: RealSwitchView
	private lateinit var inOutView: CircuitInOutView

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		switchView = builder.addVerticeView(SwitchView())
		realSwitchView = builder.addVerticeView(RealSwitchView())
		inOutView = builder.addVerticeView(CircuitInOutView(model = CircuitInOutImpl(portType = PortType.INOUT)))

		builder.connect(switchView, realSwitchView, toPort = realSwitchView.model.getInput(1))
		builder.connect(realSwitchView, fromPort = realSwitchView.model.getOutput(2), inOutView)

		circuitView = builder.build()
	}

	@Test
	fun shouldResetInOutToUndefined() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		switchView.model.toggle(scheduler)
		proceedUntilQueueIsEmpty()

		realSwitchView.model.toggle(scheduler)
		proceedUntilQueueIsEmpty()
		assertEquals(DigitalSignalFactory.of(Bit.True), inOutView.model.signal)

		realSwitchView.model.toggle(scheduler)
		proceedUntilQueueIsEmpty()
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), inOutView.model.signal)
	}
}