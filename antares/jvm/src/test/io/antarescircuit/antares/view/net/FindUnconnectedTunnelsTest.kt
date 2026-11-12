package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.AbstractJvmCircuitTest
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.TestLibraryBuilder
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.net.tunnel.TunnelView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.LibraryManagementService
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.connect.unconnected.FindUnconnectedPortsService
import io.antarescircuit.jabbah.graph.view.connect.unconnected.FindUnconnectedPortsType
import io.antarescircuit.jabbah.graph.view.connect.unconnected.UnconnectedElement
import io.antarescircuit.jabbah.graph.view.connect.unconnected.UnconnectedKind
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Tests [FindUnconnectedPortsService] with [TunnelView]s. */
class FindUnconnectedTunnelsTest : AbstractJvmCircuitTest() {

	private val library get() = LibraryModule.libraryHolder.library
	private lateinit var circuitView: GraphView

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		setupLibrary()
		// Required for resolving the library imports when counting global tunnels
		LibraryModule.libraryManagementService = LibraryManagementService()
	}

	@Test
	fun shouldFindLocalTunnelWithUniqueName() {
		lateinit var tunnelB: TunnelView
		val metaGraph = addCircuit("Main") {
			addDrivenTunnel("A")
			addDrivenTunnel("A")
			tunnelB = addDrivenTunnel("B")
		}

		val result = find(metaGraph)

		assertEquals(1, result.size)
		assertEquals(UnconnectedKind.LoneConnector, result.first().kind)
		assertEquals(tunnelB.id, result.first().viewId)
		assertEquals("B", result.first().name)
	}

	@Test
	fun shouldFindUnnamedTunnel() {
		lateinit var tunnel: TunnelView
		val metaGraph = addCircuit("Main") {
			tunnel = addVerticeView(TunnelView())
		}

		val result = find(metaGraph).filter { it.kind != UnconnectedKind.Port }

		assertEquals(listOf(UnconnectedKind.UnnamedConnector to tunnel.id), result.map { it.kind to it.viewId })
	}

	@Test
	fun shouldFindGlobalTunnelWithUniqueNameInLibrary() {
		lateinit var tunnelH: TunnelView
		addCircuit("Sub") {
			addDrivenTunnel("G", isGlobal = true)
		}
		addCircuit("Main") {
			addTunnelWithLED("G", isGlobal = true)
			tunnelH = addDrivenTunnel("H", isGlobal = true)
		}

		val result = FindUnconnectedPortsService.findInLibrary(library, FindUnconnectedPortsType.All)

		assertEquals(listOf(UnconnectedKind.LoneGlobalConnector to tunnelH.id), result.map { it.kind to it.viewId })
	}

	@Test
	fun shouldAcceptGlobalTunnelInSubCircuitUsedTwice() {
		val sub = addCircuit("Sub") {
			addDrivenTunnel("G", isGlobal = true)
		}
		addCircuit("Main") {
			reference(sub.uuid)
			reference(sub.uuid)
		}

		assertTrue(globalTunnelIssues().isEmpty())
	}

	@Test
	fun shouldAcceptGlobalTunnelInSubCircuitWhoseParentIsUsedTwice() {
		val sub = addCircuit("Sub") {
			addDrivenTunnel("G", isGlobal = true)
		}
		val middle = addCircuit("Middle") {
			reference(sub.uuid)
		}
		addCircuit("Main") {
			reference(middle.uuid)
			reference(middle.uuid)
		}

		assertTrue(globalTunnelIssues().isEmpty())
	}

	@Test
	fun shouldFindGlobalTunnelInSubCircuitUsedOnce() {
		val sub = addCircuit("Sub") {
			addDrivenTunnel("G", isGlobal = true)
		}
		addCircuit("Main") {
			reference(sub.uuid)
		}

		assertEquals(1, globalTunnelIssues().size)
	}

	private fun globalTunnelIssues(): List<UnconnectedElement> =
		FindUnconnectedPortsService.findInLibrary(library, FindUnconnectedPortsType.All)
			.filter { it.kind == UnconnectedKind.LoneGlobalConnector }

	@Test
	fun shouldFindNothingInCorrectCircuit() {
		val metaGraph = addCircuit("Main") {
			addDrivenTunnel("A")
			addTunnelWithLED("A")
		}

		assertTrue(find(metaGraph).isEmpty())
	}

	private fun find(metaGraph: MetaGraph): Set<UnconnectedElement> =
		FindUnconnectedPortsService.findInMetaGraph(metaGraph, FindUnconnectedPortsType.All, library)

	private fun addCircuit(name: String, content: TestCircuitBuilder.() -> Unit): MetaGraph {
		val builder = TestCircuitBuilder(name, styleProvider, eventBus)
		builder.content()
		circuitView = builder.build()
		return TestLibraryBuilder().addGraphView(circuitView, library)
	}

	/** Adds a [TunnelView] driven by a [SwitchView]. */
	private fun TestCircuitBuilder.addDrivenTunnel(name: String, isGlobal: Boolean = false): TunnelView {
		val tunnel = addVerticeView(TunnelView(name).also { it.isGlobal = isGlobal })
		connect(addVerticeView(SwitchView()), tunnel)
		return tunnel
	}

	/** Adds a [TunnelView] that drives an [LEDView]. */
	private fun TestCircuitBuilder.addTunnelWithLED(name: String, isGlobal: Boolean = false): TunnelView {
		val tunnel = addVerticeView(TunnelView(name).also { it.isGlobal = isGlobal })
		connect(tunnel, addVerticeView(LEDView()))
		return tunnel
	}
}
