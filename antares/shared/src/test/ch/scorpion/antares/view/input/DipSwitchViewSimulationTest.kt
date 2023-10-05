package ch.scorpion.antares.view.input

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DipSwitchViewSimulationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var dipSwitchView: DipSwitchView

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		dipSwitchView = builder.addVerticeView(DipSwitchView())
		circuitView = builder.build()
	}

	@Test
	fun shouldResetToZeroWithoutInitialValue() {
		startSetAndStop(Bit.True)

		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_4, 0), dipSwitchView.model.getOutput<DigitalSignal>().getOutgoingSignal())
	}

	@Test
	fun shouldResetToInitialValue() {
		dipSwitchView.initialValue = 2L
		startSetAndStop(Bit.True)

		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_4, 2), dipSwitchView.model.getOutput<DigitalSignal>().getOutgoingSignal())
	}

	@Test
	fun shouldRetainValue() {
		dipSwitchView.retainValue = true
		startSetAndStop(Bit.True)

		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_4, 1), dipSwitchView.model.getOutput<DigitalSignal>().getOutgoingSignal())
	}

	private fun startSetAndStop(bit0: Bit) {
		startSimulation()
		proceedUntilQueueIsEmpty()
		dipSwitchView.model.setBit(0, bit0, scheduler, circuitView)
		proceedUntilQueueIsEmpty()
		stopSimulation()
	}
}