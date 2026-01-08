package ch.scorpion.antares.model

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.antares.model.net.Tunnel
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Net
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DigitalGraphTest {

	private val signalHandler = mock<SignalHandler>()
	private lateinit var tunnel1: Tunnel
	private lateinit var tunnel2: Tunnel

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldCreateTunnelNetsDuringNetFormation() {
		val graph = createGraphWithTunnels()

		graph.formNet(signalHandler)

		val net = graph.elements.filterIsInstance<Net<*>>().first() as DigitalNet
		assertNotNull(net)
		assertSame(net, tunnel1.getPort<DigitalSignal>(2).net)
		assertSame(net, tunnel2.getPort<DigitalSignal>(2).net)
	}

	@Test
	fun shouldDestroyTunnelNetsWhenExecutionIsStopped() {
		val graph = createGraphWithTunnels()

		graph.executionStopped(signalHandler)

		val net = graph.elements.filterIsInstance<Net<*>>().firstOrNull()
		assertNull(net)
		assertNull(tunnel1.getPort<DigitalSignal>(2).net)
		assertNull(tunnel2.getPort<DigitalSignal>(2).net)
	}

	@Test
	fun shouldNotContainDifferentInOutInstanceWithSameName() {
		val graph = DigitalGraph()
		val inst1 = DigitalCircuitInOutImpl(name = "addr")
		val inst2 = DigitalCircuitInOutImpl(name = "addr")

		graph.add(inst1)

		assertTrue(graph.contains(inst1))
		assertFalse(graph.contains(inst2))
	}

	private fun createGraphWithTunnels(): DigitalGraph {
		val graph = DigitalGraph()
		tunnel1 = Tunnel("Test")
		graph.add(tunnel1)
		tunnel2 = Tunnel("Test")
		graph.add(tunnel2)
		return graph
	}
}