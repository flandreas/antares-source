package ch.scorpion.antares.view.gate

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import kotlin.test.*

class TriStateBufferGateViewIntegrationTest : AbstractCircuitTest() {

	private lateinit var builder: GraphViewBuilder<DigitalSignal>
	private lateinit var triStateBGV1: TriStateBufferGateView
	private lateinit var triStateBGV2: TriStateBufferGateView
	private lateinit var net: Net<DigitalSignal>

	override fun getCircuitView(): GraphView = builder.graphView

	@BeforeTest
	fun setupCircuit() {
		builder = GraphViewBuilder("test")
		triStateBGV1 = builder.addVerticeView(TriStateBufferGateView())
		triStateBGV2 = builder.addVerticeView(TriStateBufferGateView())
		net = builder.connect(triStateBGV1, triStateBGV2, triStateBGV2.model.getOutputPort()).model
	}

	@Test
	fun shouldBeUndefinedOnSimulationStart() {
		startSimulation(1000L)

		assertEquals(Word.of(Bit.Undefined), net.signal)
		assertFalse(net.isError)
	}

	@Test
	fun shouldAssertOutputOfSingleGate() {
		startSimulation(1000L)
		triStateBGV1.model.getInputPort().setIncomingSignal(Word.of(Bit.True), scheduler)
		triStateBGV1.model.getEnablePort().setIncomingSignal(Word.of(Bit.True), scheduler)
		proceedToNanos(2000)

		assertEquals(Word.of(Bit.True), net.signal)
		assertFalse(net.isError)
	}

	@Test
	fun shouldAssertEqualOutputs() {
		startSimulation(1000L)
		triStateBGV1.model.getInputPort().setIncomingSignal(Word.of(Bit.True), scheduler)
		triStateBGV1.model.getEnablePort().setIncomingSignal(Word.of(Bit.True), scheduler)
		proceedToNanos(2000)

		triStateBGV2.model.getInputPort().setIncomingSignal(Word.of(Bit.True), scheduler)
		triStateBGV2.model.getEnablePort().setIncomingSignal(Word.of(Bit.True), scheduler)
		proceedToNanos(3000)

		assertEquals(Word.of(Bit.True), net.signal)
		assertFalse(net.isError)
	}

	@Test
	fun shouldDetectDifferingOutputsAsError() {
		startSimulation(1000L)

		produceError()

		assertTrue(net.isError)
	}

	private fun produceError() {
		triStateBGV1.model.getInputPort().setIncomingSignal(Word.of(Bit.True), scheduler)
		triStateBGV1.model.getEnablePort().setIncomingSignal(Word.of(Bit.True), scheduler)
		proceedToNanos(2000)
		triStateBGV2.model.getInputPort().setIncomingSignal(Word.of(Bit.False), scheduler)
		triStateBGV2.model.getEnablePort().setIncomingSignal(Word.of(Bit.True), scheduler)
		proceedToNanos(3000)
	}

	@Test
	fun shouldRecoverFromErrorByUndefinedOutput() {
		startSimulation(1000L)

		produceError()
		assertTrue(net.isError)

		//triStateBGV2.model.getInputPort().setIncomingSignal(Word.of(Bit.False), scheduler)
		triStateBGV2.model.getEnablePort().setIncomingSignal(Word.of(Bit.False), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(Word.of(Bit.True), net.signal)
		assertFalse(net.isError)
	}
}