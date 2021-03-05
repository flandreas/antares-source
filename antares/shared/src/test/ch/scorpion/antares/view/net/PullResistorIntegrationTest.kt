package ch.scorpion.antares.view.net

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.model.net.PullDirection.LOW
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.gate.TriStateBufferGateView
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PullResistorIntegrationTest : AbstractCircuitTest() {

	private lateinit var builder: GraphViewBuilder<DigitalSignal>
	private lateinit var triStateBGV: TriStateBufferGateView
	private lateinit var pullResistorView: PullResistorView
	private lateinit var net: Net<DigitalSignal>

	override fun getCircuitView(): GraphView = builder.graphView

	@BeforeTest
	fun setupCircuit() {
		builder = GraphViewBuilder("test")
		triStateBGV = builder.addVerticeView(TriStateBufferGateView())
		pullResistorView = builder.addVerticeView(PullResistorView(LOW))
		net = builder.connect(triStateBGV, pullResistorView, pullResistorView.model.getOutputPort()).model
	}

	@Test
	fun shouldBeLowOnSimulationStart() {
		startSimulation(1000L)

		assertEquals(Word.of(Bit.False), net.signal)
		assertFalse(net.isError)
	}

	@Test
	fun shouldNotDisturbDefinedSignal() {
		startSimulation(1000L)

		triStateBGV.model.getInputPort().setIncomingSignal(Word.of(Bit.True), scheduler)
		triStateBGV.model.getEnablePort().setIncomingSignal(Word.of(Bit.True), scheduler)
		proceedToNanos(2000)

		assertFalse(net.isError)
		assertEquals(Word.of(Bit.True), net.signal)
	}

	@Test
	fun shouldPullUndefinedToLow() {
		startSimulation(1000L)
		triStateBGV.model.getInputPort().setIncomingSignal(Word.of(Bit.True), scheduler)
		triStateBGV.model.getEnablePort().setIncomingSignal(Word.of(Bit.True), scheduler)
		proceedToNanos(2000)

		triStateBGV.model.getEnablePort().setIncomingSignal(Word.of(Bit.False), scheduler)
		proceedToNanos(3000)

		assertEquals(Word.of(Bit.False), net.signal)
		assertFalse(net.isError)
	}
}