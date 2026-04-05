package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.model.net.PullDirection.LOW
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.signal.Word
import io.antarescircuit.antares.view.gate.TriStateBufferGateView
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PullResistorViewIntegrationTest : AbstractCircuitTest() {

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

		assertEquals(DigitalSignalFactory.of(Bit.False), net.signal)
		assertFalse(net.isError)
	}

	@Test
	fun shouldNotDisturbDefinedSignal() {
		startSimulation(1000L)

		triStateBGV.model.getInputPort().setIncomingSignal(DigitalSignalFactory.of(Bit.True), scheduler)
		triStateBGV.model.getEnablePort().setIncomingSignal(DigitalSignalFactory.of(Bit.True), scheduler)
		proceedToNanos(2000)

		assertFalse(net.isError)
		assertEquals(DigitalSignalFactory.of(Bit.True), net.signal)
	}

	@Test
	fun shouldPullUndefinedToLow() {
		startSimulation(1000L)
		triStateBGV.model.getInputPort().setIncomingSignal(DigitalSignalFactory.of(Bit.True), scheduler)
		triStateBGV.model.getEnablePort().setIncomingSignal(DigitalSignalFactory.of(Bit.True), scheduler)
		proceedToNanos(2000)

		triStateBGV.model.getEnablePort().setIncomingSignal(DigitalSignalFactory.of(Bit.False), scheduler)
		proceedToNanos(3000)

		assertEquals(DigitalSignalFactory.of(Bit.False), net.signal)
		assertFalse(net.isError)
	}
}