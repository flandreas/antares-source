package ch.scorpion.antares.view.net

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.model.gate.TriStateBufferGate
import ch.scorpion.antares.model.net.PullDirection.LOW
import ch.scorpion.antares.model.net.PullResistor
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.view.gate.TriStateBufferGateView
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Integration tests of [PullResistorView] operating on bus wires with wider [BitWidth].
 * Pulling up and down must work on each individual [Bit].
 */
class PullResistorViewBusIntegrationTest : AbstractCircuitTest() {

	private lateinit var builder: GraphViewBuilder<DigitalSignal>
	private lateinit var triStateBGV: TriStateBufferGateView
	private lateinit var pullResistorView: PullResistorView
	private lateinit var net: Net<DigitalSignal>

	override fun getCircuitView(): GraphView = builder.graphView

	@BeforeTest
	fun setupCircuit() {
		builder = GraphViewBuilder("test")
		triStateBGV = builder.addVerticeView(TriStateBufferGateView(model = TriStateBufferGate(bitWidth = BitWidth.BW_4)))
		pullResistorView = builder.addVerticeView(PullResistorView(model = PullResistor(bitWidth = BitWidth.BW_4, pullDirection = LOW)))
		net = builder.connect(triStateBGV, pullResistorView, pullResistorView.model.getOutputPort()).model
	}

	@Test
	fun shouldPullPartiallyUndefinedToLow() {
		val signal = DigitalSignalFactory.ofBits(listOf(Bit.False, Bit.Undefined, Bit.True, Bit.Undefined))
		startSimulation()
		proceedUntilQueueIsEmpty()

		triStateBGV.model.getInputPort().setIncomingSignal(signal, scheduler)
		triStateBGV.model.getEnablePort().setIncomingSignal(DigitalSignalFactory.of(Bit.True), scheduler)
		proceedUntilQueueIsEmpty()

		assertFalse(net.isError)
		assertEquals(
			DigitalSignalFactory.ofBits(listOf(Bit.False, Bit.False, Bit.True, Bit.False)),
			net.signal)
	}
}