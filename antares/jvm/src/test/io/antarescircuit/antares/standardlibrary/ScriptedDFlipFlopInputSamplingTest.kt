package io.antarescircuit.antares.standardlibrary

import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_1
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractScriptedDFlipFlopInputSamplingTest(
	private val uuid: String,
	private val activeLow: Boolean,
	private val fallingEdge: Boolean,
) : AbstractStandardLibraryBasedCircuitTest() {

	private lateinit var flipFlop: SubGraphVerticeView<*>
	private val preset: String get() = if (activeLow) "!(PRE)" else "PRE"
	private val clear: String get() = if (activeLow) "!(CLR)" else "CLR"
	private val delay: Long get() = flipFlop.model.propagationDelay.value

	override fun createCircuit(): GraphView {
		val builder = TestCircuitBuilder("Scripted D input sampling", styleProvider, eventBus)
		flipFlop = builder.add(LibraryModule.libraryHolder.getContainerLibraryElement(UUID(uuid))!!
			.getNewInstance()) as SubGraphVerticeView<*>
		return builder.build()
	}

	override fun setup() {
		super.setup()
		scheduler.isDeepExecution = false
		startSimulation()
		write("C", fallingEdge)
		write("D", false)
		control(preset, false)
		control(clear, true)
		proceedUntilQueueIsEmpty()
		control(clear, false)
		proceedUntilQueueIsEmpty()
		assertOutput(false)
	}

	@AfterTest
	fun stopCircuit() {
		stopSimulation()
	}

	@Test
	fun shouldSampleDataAtClockEdgeAndPublishAfterPropagationDelay() {
		write("D", true)
		proceedUntilQueueIsEmpty()
		val edgeTime = scheduler.executionTime
		write("C", !fallingEdge)
		proceedToNanos(edgeTime + 10)
		write("D", false)
		proceedToNanos(edgeTime + delay - 1)
		assertOutput(false)
		proceedToNanos(edgeTime + delay)
		assertOutput(true)
		proceedUntilQueueIsEmpty()
		assertOutput(true)
	}

	@Test
	fun shouldRememberActiveEdgeAfterClockReturnsToInactiveLevel() {
		write("D", true)
		proceedUntilQueueIsEmpty()
		val edgeTime = scheduler.executionTime
		write("C", !fallingEdge)
		proceedToNanos(edgeTime + 10)
		write("C", fallingEdge)
		proceedUntilQueueIsEmpty()
		assertOutput(true)
	}

	@Test
	fun shouldIgnoreClockReceivedDuringClearEvenIfClearIsReleasedBeforeExecution() {
		assertClockIgnoredDuringControl(clear, false)
	}

	@Test
	fun shouldIgnoreClockReceivedDuringPresetEvenIfPresetIsReleasedBeforeExecution() {
		assertClockIgnoredDuringControl(preset, true)
	}

	@Test
	fun shouldKeepSeparateDataSamplesForCloselySpacedClockEdges() {
		write("D", true)
		proceedUntilQueueIsEmpty()
		val firstEdge = scheduler.executionTime
		write("C", !fallingEdge)
		proceedToNanos(firstEdge + 10)
		write("C", fallingEdge)
		proceedToNanos(firstEdge + 20)
		write("D", false)
		proceedToNanos(firstEdge + 30)
		write("C", !fallingEdge)

		proceedToNanos(firstEdge + delay)
		assertOutput(true)
		proceedToNanos(firstEdge + delay + 29)
		assertOutput(true)
		proceedToNanos(firstEdge + delay + 30)
		assertOutput(false)
		proceedUntilQueueIsEmpty()
		assertOutput(false)
	}

	@Test
	fun shouldApplyClearAfterAnEarlierValidClockSample() {
		write("D", true)
		proceedUntilQueueIsEmpty()
		val edgeTime = scheduler.executionTime
		write("C", !fallingEdge)
		proceedToNanos(edgeTime + 10)
		control(clear, true)
		proceedUntilQueueIsEmpty()
		assertOutput(false)
	}

	private fun assertClockIgnoredDuringControl(port: String, value: Boolean) {
		control(port, true)
		write("D", !value)
		proceedUntilQueueIsEmpty()
		assertOutput(value)
		val edgeTime = scheduler.executionTime
		write("C", !fallingEdge)
		proceedToNanos(edgeTime + 20)
		control(port, false)
		proceedUntilQueueIsEmpty()
		assertOutput(value)

		// Suppressing the old event must not suppress the next valid edge after release.
		write("C", fallingEdge)
		proceedUntilQueueIsEmpty()
		write("C", !fallingEdge)
		proceedUntilQueueIsEmpty()
		assertOutput(!value)
	}

	private fun control(port: String, asserted: Boolean) = write(port, asserted != activeLow)

	private fun write(port: String, value: Boolean) {
		flipFlop.model.getInput<DigitalSignal>(port).setIncomingSignal(signal(value), scheduler)
	}

	private fun assertOutput(value: Boolean) {
		assertEquals(signal(value), flipFlop.model.getOutput<DigitalSignal>("Q").getOutgoingSignal(), "Q at ${scheduler.executionTime} ns")
		assertEquals(signal(!value), flipFlop.model.getOutput<DigitalSignal>("!Q").getOutgoingSignal(), "!Q at ${scheduler.executionTime} ns")
	}

	private fun signal(value: Boolean) = DigitalSignalFactory.of(BW_1, if (value) 1UL else 0UL)
}

class ScriptedDFlipFlopActiveLowFallingEdgeInputSamplingTest : AbstractScriptedDFlipFlopInputSamplingTest(
	uuid = "3f910ab2-d6e0-4fe2-9835-f98eea045c9f",
	activeLow = true,
	fallingEdge = true,
)

class ScriptedDFlipFlopActiveLowRisingEdgeInputSamplingTest : AbstractScriptedDFlipFlopInputSamplingTest(
	uuid = "f5ffee61-6d3e-453e-a352-e702a06b11d2",
	activeLow = true,
	fallingEdge = false,
)

class ScriptedDFlipFlopActiveHighRisingEdgeInputSamplingTest : AbstractScriptedDFlipFlopInputSamplingTest(
	uuid = "a1b8c056-8f4b-4ffe-90a9-dbcc321fe733",
	activeLow = false,
	fallingEdge = false,
)
