package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.AbstractJvmCircuitTest
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.TestLibraryBuilder
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.gate.NonUnaryLogicGate
import io.antarescircuit.antares.model.net.NetSignalApplierStrategy
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Reproduces a lost output transition across a bidirectional subcircuit port on a wired-OR bus. */
class BidirectionalWiredOrPropagationTest : AbstractJvmCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var subcircuit: SubGraphVerticeView<SubGraphVerticeRef>
	private lateinit var externalDriver: SwitchView
	private lateinit var enable: SwitchView
	private lateinit var bus: EdgeView<DigitalSignal>

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		setupLibrary()
		val library = LibraryModule.libraryHolder.library

		// Inside the subcircuit: BUS = 1 AND E. BUS is bidirectional, with wired-OR resolution.
		val inner = TestCircuitBuilder("Enabled bus driver")
		(inner.graph as DigitalGraph).netSignalApplierStrategy = NetSignalApplierStrategy.WiredOr
		val high = inner.addVerticeView(ConstantView(LongValueImpl.ONE))
		val gate = inner.addVerticeView(LogicGateView.andGateView())
		val input = inner.addInput("E")
		val inOut = inner.addInOut("BUS")
		inner.connect(high, gate, gate.model.getInput(1))
		inner.connect(input, gate, gate.model.getInput(2))
		inner.connect(gate, inOut)
		val metaGraph = TestLibraryBuilder().addGraphView(inner.graphView, library)

		val outer = TestCircuitBuilder("Bidirectional wired-OR propagation")
		(outer.graph as DigitalGraph).netSignalApplierStrategy = NetSignalApplierStrategy.WiredOr
		subcircuit = library.getContainerLibraryElement(metaGraph.uuid)!!
			.getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<SubGraphVerticeRef>
		outer.addVerticeView(subcircuit)
		subcircuit.location = Point2D(200, 0)
		externalDriver = outer.addVerticeView(SwitchView())
		enable = outer.addVerticeView(SwitchView().also { it.location = Point2D(0, 100) })
		bus = outer.connect(externalDriver, subcircuit, subcircuit.model.getInput("BUS"))
		outer.connect(enable, subcircuit, subcircuit.model.getInput("E"))
		circuitView = outer.graphView
	}

	@Test
	fun shouldForwardInternalSignalAfterExternalDriverReleasesSameValueDeep() = checkHandoff(deep = true)

	@Test
	fun shouldForwardInternalSignalAfterExternalDriverReleasesSameValueShallow() = checkHandoff(deep = false)

	private fun checkHandoff(deep: Boolean) {
		scheduler.isDeepExecution = deep
		scheduler.isSoftBreakpointsEnabled = false
		try {
			startSimulation()
			settle()
			val gate = subcircuit.model.getGraph().elements.single { it is NonUnaryLogicGate } as NonUnaryLogicGate

			// First establish 1 from the internal driver, with the external driver at 0.
			enable.model.on(scheduler)
			settle()
			assertSignal(true, gate.getOutput<DigitalSignal>().getOutgoingSignal(), "Internal driver initially enabled")
			assertSignal(true, bus.net!!.signal, "Initial internal drive must reach the bus")

			// Let the external driver hold 1 while disabling the internal driver. The bus stays at 1.
			enable.model.off(scheduler)
			externalDriver.model.on(scheduler)
			settle()
			assertSignal(false, gate.getOutput<DigitalSignal>().getOutgoingSignal(), "Internal driver disabled")
			assertSignal(true, bus.net!!.signal, "External driver holds the bus at 1")

			// Hand the same value back to the internal driver. The settled bus must remain at 1.
			enable.model.on(scheduler)
			externalDriver.model.off(scheduler)
			settle()
			assertSignal(true, gate.getOutput<DigitalSignal>().getOutgoingSignal(), "Internal driver drives 1")
			assertSignal(false, externalDriver.model.getOutput<DigitalSignal>().getOutgoingSignal(), "External driver drives 0")
			assertNoIssues()
			assertSignal(true, bus.net!!.signal, "Settled wired-OR bus must be 1 when its drivers are 1 and 0")

			// The handoff must not leave a latched 1 after both drivers return to 0.
			enable.model.off(scheduler)
			settle()
			assertSignal(false, gate.getOutput<DigitalSignal>().getOutgoingSignal(), "Internal driver returns to 0")
			assertSignal(false, bus.net!!.signal, "Settled wired-OR bus must return to 0 when both drivers are 0")
			assertNoIssues()
		} finally {
			stopSimulation()
		}
	}

	private fun settle() {
		proceedUntilQueueIsEmpty()
		assertTrue(scheduler.isQueueEmpty, "All pending propagation must finish before checking signals")
	}

	private fun assertSignal(expected: Boolean, actual: DigitalSignal?, message: String) {
		assertEquals(DigitalSignalFactory.of(expected), actual, message)
	}
}
