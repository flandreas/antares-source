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
import io.antarescircuit.jabbah.graph.model.port.InconsistentNetError
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Runs the wired-OR reproducer circuit with conflict resolution in both the inner and outer graph. */
class BidirectionalConflictPropagationTest : AbstractJvmCircuitTest() {

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

		// Inside the subcircuit: BUS = 1 AND E. BUS is bidirectional, with conflict resolution.
		val inner = TestCircuitBuilder("Enabled bus driver")
		(inner.graph as DigitalGraph).netSignalApplierStrategy = NetSignalApplierStrategy.Conflict
		val high = inner.addVerticeView(ConstantView(LongValueImpl.ONE))
		val gate = inner.addVerticeView(LogicGateView.andGateView())
		val input = inner.addInput("E")
		val inOut = inner.addInOut("BUS")
		inner.connect(high, gate, gate.model.getInput(1))
		inner.connect(input, gate, gate.model.getInput(2))
		inner.connect(gate, inOut)
		val metaGraph = TestLibraryBuilder().addGraphView(inner.graphView, library)

		val outer = TestCircuitBuilder("Bidirectional conflict propagation")
		(outer.graph as DigitalGraph).netSignalApplierStrategy = NetSignalApplierStrategy.Conflict
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
	fun shouldReportConflictingDriversDuringHandoffDeep() = checkHandoff(deep = true)

	@Test
	fun shouldReportConflictingDriversDuringHandoffShallow() = checkHandoff(deep = false)

	@Test
	fun shouldPropagateZeroWhenBothDriversAgreeAfterConflictsDeep() = checkHandoff(deep = true, checkRecovery = true)

	@Test
	fun shouldPropagateZeroWhenBothDriversAgreeAfterConflictsShallow() = checkHandoff(deep = false, checkRecovery = true)

	private fun checkHandoff(deep: Boolean, checkRecovery: Boolean = false) {
		scheduler.isDeepExecution = deep
		scheduler.isSoftBreakpointsEnabled = false
		try {
			startSimulation()
			settle()
			val gate = subcircuit.model.getGraph().elements.single { it is NonUnaryLogicGate } as NonUnaryLogicGate

			assertSignal(false, bus.net!!.signal, "Initially both drivers are 0")
			assertNull(bus.net!!.executionError)
			assertNoIssues()

			// A switch at 0 still actively drives 0; it does not release the bus under Conflict.
			// Internal 1 versus external 0 must therefore report a conflict, rather than resolve to 1.
			enable.model.on(scheduler)
			settle()
			assertSignal(true, gate.getOutput<DigitalSignal>().getOutgoingSignal(), "Internal driver initially enabled")
			assertConflictingNets(gate)

			// Exchange the drivers: internal 0 versus external 1 is also a conflict.
			enable.model.off(scheduler)
			externalDriver.model.on(scheduler)
			settle()
			assertSignal(false, gate.getOutput<DigitalSignal>().getOutgoingSignal(), "Internal driver disabled")
			assertSignal(true, externalDriver.model.getOutput<DigitalSignal>().getOutgoingSignal(), "External driver drives 1")
			assertConflictingNets(gate)

			// Repeat the original handoff. Internal 1 versus external 0 must still report a conflict.
			enable.model.on(scheduler)
			externalDriver.model.off(scheduler)
			settle()
			assertSignal(true, gate.getOutput<DigitalSignal>().getOutgoingSignal(), "Internal driver drives 1")
			assertSignal(false, externalDriver.model.getOutput<DigitalSignal>().getOutgoingSignal(), "External driver drives 0")
			assertConflictingNets(gate)

			if (checkRecovery) {
				// With matching drivers, conflict resolution must recover and propagate their common value.
				externalDriver.model.on(scheduler)
				settle()
				assertNull(gate.getOutput<DigitalSignal>().net!!.executionError, "Inner conflict must clear for 1 and 1")
				assertNull(bus.net!!.executionError, "Outer conflict must clear for 1 and 1")
				assertSignal(true, bus.net!!.signal, "Agreeing drivers must resolve to 1")

				enable.model.off(scheduler)
				externalDriver.model.off(scheduler)
				settle()
				assertSignal(false, gate.getOutput<DigitalSignal>().getOutgoingSignal(), "Internal driver drives 0")
				assertSignal(false, externalDriver.model.getOutput<DigitalSignal>().getOutgoingSignal(), "External driver drives 0")
				assertNull(gate.getOutput<DigitalSignal>().net!!.executionError, "Inner conflict must clear for 0 and 0")
				assertNull(bus.net!!.executionError, "Outer conflict must clear for 0 and 0")
				assertSignal(false, bus.net!!.signal, "Agreeing drivers must resolve to 0")
			}
		} finally {
			stopSimulation()
		}
	}

	private fun assertConflictingNets(gate: NonUnaryLogicGate) {
		// The signal retained during a conflict is not a resolved bus value. Check the errors on both sides.
		assertIs<InconsistentNetError>(gate.getOutput<DigitalSignal>().net!!.executionError, "Inner net must report conflicting drivers")
		assertIs<InconsistentNetError>(bus.net!!.executionError, "Outer net must report conflicting drivers")
	}

	private fun settle() {
		proceedUntilQueueIsEmpty()
		assertTrue(scheduler.isQueueEmpty, "All pending propagation must finish before checking signals")
	}

	private fun assertSignal(expected: Boolean, actual: DigitalSignal?, message: String) {
		assertEquals(DigitalSignalFactory.of(expected), actual, message)
	}
}
