package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.graph.view.GraphView
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
		dipSwitchView.model.setBit(0, bit0, scheduler)
		proceedUntilQueueIsEmpty()
		stopSimulation()
	}
}