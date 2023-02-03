package ch.scorpion.antares.view.net

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.net.PullDirection
import ch.scorpion.antares.model.net.PullResistor
import ch.scorpion.antares.model.net.Tunnel
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import kotlin.test.*

class TunnelViewIntegrationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var builder: GraphViewBuilder<DigitalSignal>
	private lateinit var tunnelView1: TunnelView
	private lateinit var tunnelView2: TunnelView
	private lateinit var inOutView: DigitalCircuitInOutView
	private lateinit var pullResistorView: PullResistorView
	private lateinit var edgeView1: EdgeView<DigitalSignal>
	private lateinit var edgeView2: EdgeView<DigitalSignal>

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		builder = GraphViewBuilder("test")
		inOutView = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INOUT)))
		tunnelView1 = builder.addVerticeView(TunnelView(model = Tunnel("A")))
		tunnelView2 = builder.addVerticeView(TunnelView(model = Tunnel("A")))
		pullResistorView = builder.addVerticeView(PullResistorView(model = PullResistor(pullDirection = PullDirection.HIGH)))
		edgeView1 = builder.connect(inOutView, tunnelView1)
		edgeView2 = builder.connect(pullResistorView, tunnelView2)

		circuitView = builder.build()
	}

	@Test
	fun shouldCreateCombinedNets() {
		startSimulation()
		val combinedNets = inOutView.model.getOutput<DigitalSignal>().combinedNets

		assertEquals(1, combinedNets.size)
		assertNotNull(combinedNets.first().accessOf(inOutView.model.getOutput()))
		assertNotNull(combinedNets.first().accessOf(pullResistorView.model.getOutput()))
	}

	@Test
	fun shouldBeHighAtStartup() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(true), inOutView.model.signal)
	}

	@Test
	fun shouldPropagateLow() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		inOutView.model.setIncomingSignal(DigitalSignalFactory.of(false), scheduler)
		proceedUntilQueueIsEmpty()

		assertFalse(edgeView1.model.isError)
		assertEquals(DigitalSignalFactory.of(false), edgeView1.model.signal)
		assertFalse(edgeView2.model.isError)
		assertEquals(DigitalSignalFactory.of(false), edgeView2.model.signal)
	}
}