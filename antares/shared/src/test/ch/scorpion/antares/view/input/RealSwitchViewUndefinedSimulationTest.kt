package ch.scorpion.antares.view.input

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the simulation behaviour of [RealSwitchView] with a [DigitalCircuitInOutView] that
 * can go into state "undefined".
 */
class RealSwitchViewUndefinedSimulationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var switchView: SwitchView
	private lateinit var realSwitchView: RealSwitchView
	private lateinit var inOutView: DigitalCircuitInOutView

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		switchView = builder.addVerticeView(SwitchView())
		realSwitchView = builder.addVerticeView(RealSwitchView())
		inOutView = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INOUT)))

		builder.connect(switchView, realSwitchView, toPort = realSwitchView.model.getInput(1))
		builder.connect(realSwitchView, fromPort = realSwitchView.model.getOutput(2), inOutView)

		circuitView = builder.build()
	}

	@Test
	fun shouldResetInOutToUndefined() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		switchView.model.toggle(scheduler, circuitView)
		proceedUntilQueueIsEmpty()

		realSwitchView.model.toggle(scheduler, circuitView)
		proceedUntilQueueIsEmpty()
		assertEquals(DigitalSignalFactory.of(Bit.True), inOutView.model.signal)

		realSwitchView.model.toggle(scheduler, circuitView)
		proceedUntilQueueIsEmpty()
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), inOutView.model.signal)
	}
}