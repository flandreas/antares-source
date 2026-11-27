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

/**
 * Exercises the actual library circuits in deep execution, including transient violations of
 * asynchronous preset/clear priority that disappear before the scheduling queue becomes empty.
 * PRE and CLR are never asserted together; that combination has a separate invalid-state contract.
 */
abstract class AbstractPresetClearFlipFlopTest(
	private val uuid: String,
	private val setPort: String,
	private val resetPort: String? = null,
	private val activeLow: Boolean = false,
	private val fallingEdge: Boolean = false,
) : AbstractStandardLibraryBasedCircuitTest() {

	companion object {
		// Longer than the propagation and edge-detector delays of all variants under test.
		private const val OBSERVATION_NANOS = 1_000L
		// Specific to the falling-edge D circuit used by the modulo-5 regression.
		private const val RELEASE_AFTER_EDGE_NANOS = 20L
	}

	private lateinit var flipFlop: SubGraphVerticeView<*>
	private val presetPort: String get() = if (activeLow) "!(PRE)" else "PRE"
	private val clearPort: String get() = if (activeLow) "!(CLR)" else "CLR"

	override fun createCircuit(): GraphView {
		val builder = TestCircuitBuilder("Preset/clear priority", styleProvider, eventBus)
		flipFlop = builder.add(LibraryModule.libraryHolder
			.getContainerLibraryElement(UUID(uuid))!!
			.getNewInstance()) as SubGraphVerticeView<*>
		return builder.build()
	}

	override fun setup() {
		super.setup()
		scheduler.isDeepExecution = true
		startSimulation()
		write("C", fallingEdge)
		requestValue(false)
		setPreset(false)
		setClear(true)
		proceedUntilQueueIsEmpty()
		setClear(false)
		proceedUntilQueueIsEmpty()
		assertOutput(false)
	}

	@AfterTest
	fun stopCircuit() {
		stopSimulation()
	}

	@Test
	fun shouldClearWithoutClockEdgeWhileClockIsLow() = assertAsynchronousControl(false, false)

	@Test
	fun shouldClearWithoutClockEdgeWhileClockIsHigh() = assertAsynchronousControl(false, true)

	@Test
	fun shouldPresetWithoutClockEdgeWhileClockIsLow() = assertAsynchronousControl(true, false)

	@Test
	fun shouldPresetWithoutClockEdgeWhileClockIsHigh() = assertAsynchronousControl(true, true)

	@Test
	fun shouldKeepClearDominantThroughoutConflictingClockActivity() = assertControlDominatesClock(false)

	@Test
	fun shouldKeepPresetDominantThroughoutConflictingClockActivity() = assertControlDominatesClock(true)

	@Test
	fun shouldResumeClockedOperationAfterClear() = assertClockedOperationResumes(false)

	@Test
	fun shouldResumeClockedOperationAfterPreset() = assertClockedOperationResumes(true)

	private fun assertAsynchronousControl(preset: Boolean, clockHigh: Boolean) {
		storeValue(!preset)
		write("C", clockHigh)
		proceedUntilQueueIsEmpty()
		assertOutput(!preset)

		setControl(preset, true)
		proceedUntilQueueIsEmpty()
		assertOutputFor(preset)
	}

	private fun assertControlDominatesClock(preset: Boolean) {
		setControl(preset, true)
		requestValue(!preset)
		proceedUntilQueueIsEmpty()
		assertOutput(preset)

		// Check both transitions, including master-to-slave transfer in the JK MS variant.
		repeat(2) {
			write("C", !fallingEdge)
			assertOutputFor(preset)
			write("C", fallingEdge)
			assertOutputFor(preset)
		}
	}

	private fun assertClockedOperationResumes(preset: Boolean) {
		setControl(preset, true)
		requestValue(!preset)
		proceedUntilQueueIsEmpty()
		assertOutput(preset)
		setControl(preset, false)
		proceedUntilQueueIsEmpty()
		assertOutputFor(preset)
		storeValue(!preset)
	}

	protected fun assertReleaseDuringClockPulse(preset: Boolean) {
		setControl(preset, true)
		requestValue(!preset)
		proceedUntilQueueIsEmpty()
		assertOutput(preset)

		write("C", !fallingEdge)
		assertOutputFor(preset, RELEASE_AFTER_EDGE_NANOS)
		setControl(preset, false)
		assertOutputFor(preset)
		// The next complete clock cycle must still capture new data normally.
		write("C", fallingEdge)
		proceedUntilQueueIsEmpty()
		storeValue(!preset)
	}

	private fun storeValue(value: Boolean) {
		requestValue(value)
		proceedUntilQueueIsEmpty()
		write("C", !fallingEdge)
		proceedUntilQueueIsEmpty()
		write("C", fallingEdge)
		proceedUntilQueueIsEmpty()
		assertOutput(value)
	}

	private fun requestValue(value: Boolean) {
		write(setPort, value)
		resetPort?.let { write(it, !value) }
	}

	private fun setControl(preset: Boolean, asserted: Boolean) {
		if (preset) setPreset(asserted) else setClear(asserted)
	}

	private fun setPreset(asserted: Boolean) = write(presetPort, asserted != activeLow)
	private fun setClear(asserted: Boolean) = write(clearPort, asserted != activeLow)

	private fun write(port: String, high: Boolean) {
		flipFlop.model.getInput<DigitalSignal>(port).setIncomingSignal(signal(high), scheduler)
	}

	private fun assertOutputFor(value: Boolean, duration: Long = OBSERVATION_NANOS) {
		val start = scheduler.executionTime
		// Checking only the settled output misses the short Q = !Q = 1 conflict in the NAND latch.
		for (elapsed in 1..duration) {
			proceedToNanos(start + elapsed)
			assertOutput(value)
		}
	}

	private fun assertOutput(value: Boolean) {
		for ((port, expected) in listOf("Q" to value, "!Q" to !value)) {
			assertEquals(
				signal(expected),
				flipFlop.model.getOutput<DigitalSignal>(port).getOutgoingSignal(),
				"$port at ${scheduler.executionTime} ns in circuit $uuid",
			)
		}
	}

	private fun signal(high: Boolean): DigitalSignal = DigitalSignalFactory.of(BW_1, if (high) 1UL else 0UL)
}

class DFlipFlopActiveLowPresetClearFallingEdgeTest : AbstractPresetClearFlipFlopTest(
	uuid = "3f910ab2-d6e0-4fe2-9835-f98eea045c9f",
	setPort = "D",
	activeLow = true,
	fallingEdge = true,
) {
	// Other variants have different detector delays; this overlap reproduces the counter's timing.
	@Test
	fun shouldNotCaptureResetInducedClockPulseWhenClearIsReleased() = assertReleaseDuringClockPulse(false)

	@Test
	fun shouldNotCaptureClockPulseStartedDuringPresetWhenPresetIsReleased() = assertReleaseDuringClockPulse(true)
}

/** D-Flipflop !(PRE)/!(CRL) */
class DFlipFlopActiveLowPresetClearRisingEdgeTest : AbstractPresetClearFlipFlopTest(
	uuid = "f5ffee61-6d3e-453e-a352-e702a06b11d2",
	setPort = "D",
	activeLow = true,
)

/** D-Flipflop PRE/CRL */
class DFlipFlopActiveHighPresetClearRisingEdgeTest : AbstractPresetClearFlipFlopTest(
	uuid = "a1b8c056-8f4b-4ffe-90a9-dbcc321fe733",
	setPort = "D",
)

/** SR Flip-Flop !(PRE)/!(CLR) (Neg) */
class SRFlipFlopActiveLowPresetClearFallingEdgeTest : AbstractPresetClearFlipFlopTest(
	uuid = "9dc3d0a1-b49e-41d1-b298-c043a2f01228",
	setPort = "S",
	resetPort = "R",
	activeLow = true,
	fallingEdge = true,
)

/** SR Flip-Flop PRE/CLR */
class SRFlipFlopActiveHighPresetClearRisingEdgeTest : AbstractPresetClearFlipFlopTest(
	uuid = "d34d3483-be1d-432b-a949-7f68e0ee756b",
	setPort = "S",
	resetPort = "R",
)

/** JK MS-Flip-Flop PRE/CLR */
class JKMasterSlaveFlipFlopActiveHighPresetClearTest : AbstractPresetClearFlipFlopTest(
	uuid = "048daa5b-de3e-4ae9-a343-e8c355a033dc",
	setPort = "J",
	resetPort = "K",
)
