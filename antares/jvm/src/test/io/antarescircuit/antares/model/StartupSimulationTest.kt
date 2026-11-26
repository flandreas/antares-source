package io.antarescircuit.antares.model

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.arithmetic.Random
import io.antarescircuit.antares.model.input.Clock
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.standardlibrary.AbstractStandardLibraryBasedCircuitTest
import io.antarescircuit.antares.view.arithmetic.RandomView
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.input.ClockView
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StartupSimulationTest {

	@BeforeTest
	fun setup() {
		AntaresTestRule.configure()
		AbstractStandardLibraryBasedCircuitTest.setupLibrary()
	}

	@Test
	fun shouldIgnoreDerivedClockTransitionsDuringSettlingAndCountAfterward() {
		val builder = TestCircuitBuilder("startup")
		val input = builder.addInput("C", inputStartValue = DigitalSignalFactory.of(true))
		val first = builder.add(LogicGateView.notGateView())
		val second = builder.add(LogicGateView.notGateView())
		first.model.propagationDelay = LongValueImpl(1_000)
		second.model.propagationDelay = LongValueImpl(1_000)
		val flipFlop = builder.add(LibraryModule.libraryHolder.getContainerLibraryElement(
			UUID("ce3245e3-3b2c-405f-9d1b-9650823acfe7"))!!.getNewInstance()) as SubGraphVerticeView<*>
		val output = builder.addOutput("Q")
		builder.connect(input, first)
		builder.connect(first, second)
		builder.connect(second, flipFlop, flipFlop.model.getInput("T"))
		builder.connect(flipFlop, flipFlop.model.getOutput("Q"), output)

		withInitializedCircuit(builder) { _, runner ->
			assertEquals(DigitalSignalFactory.of(false), output.model.signal)
			input.model.setIncomingSignal(DigitalSignalFactory.of(false), runner.scheduler)
			runner.proceedUntilQueueEmpty()
			assertEquals(DigitalSignalFactory.of(true), output.model.signal)
			input.model.setIncomingSignal(DigitalSignalFactory.of(true), runner.scheduler)
			runner.proceedUntilQueueEmpty()
			input.model.setIncomingSignal(DigitalSignalFactory.of(false), runner.scheduler)
			runner.proceedUntilQueueEmpty()
			assertEquals(DigitalSignalFactory.of(false), output.model.signal)
		}
	}

	@Test
	fun shouldHonorAsynchronousPresetDuringInitialization() {
		assertPresetClearStartup(false, true, true)
	}

	@Test
	fun shouldHonorAsynchronousClearDuringInitialization() {
		assertPresetClearStartup(true, false, false)
	}

	@Test
	fun shouldPreserveInitialStateWhenPresetAndClearAreInactive() {
		assertPresetClearStartup(true, true, false)
	}

	private fun assertPresetClearStartup(preset: Boolean, clear: Boolean, expectedQ: Boolean) {
		val builder = TestCircuitBuilder("startup preset")
		val flipFlop = builder.add(LibraryModule.libraryHolder.getContainerLibraryElement(
			UUID("f5ffee61-6d3e-453e-a352-e702a06b11d2"))!!.getNewInstance()) as SubGraphVerticeView<*>
		for ((name, value) in listOf("C" to false, "D" to false, "!(PRE)" to preset, "!(CLR)" to clear)) {
			val input = builder.addInput(name, inputStartValue = DigitalSignalFactory.of(value))
			builder.connect(input, flipFlop, flipFlop.model.getInput(name))
		}
		val output = builder.addOutput("Q")
		val inverted = builder.addOutput("!Q")
		builder.connect(flipFlop, flipFlop.model.getOutput("Q"), output)
		builder.connect(flipFlop, flipFlop.model.getOutput("!Q"), inverted)

		withInitializedCircuit(builder) { _, _ ->
			assertEquals(DigitalSignalFactory.of(expectedQ), output.model.signal)
			assertEquals(DigitalSignalFactory.of(!expectedQ), inverted.model.signal)
		}
	}

	@Test
	fun shouldDelayAutomaticClockUntilSlowInitialPropagationSettles() {
		val builder = TestCircuitBuilder("startup clock")
		val clock = Clock().apply { periodOrFrequency = MagnitudeValue(1_000, Magnitude.Nano, SIUnit.Second) }
		val clockView = builder.add(ClockView(model = clock))
		val first = builder.add(LogicGateView.notGateView())
		val second = builder.add(LogicGateView.notGateView())
		first.model.propagationDelay = LongValueImpl(10_000)
		second.model.propagationDelay = LongValueImpl(10_000)
		val output = builder.addOutput("O")
		builder.connect(clockView, first)
		builder.connect(first, second)
		builder.connect(second, output)

		withInitializedCircuit(builder) { _, runner ->
			val scheduler = runner.scheduler
			assertFalse(clock.isOn)
			assertEquals(0L, clock.cycleCount)
			assertEquals(DigitalSignalFactory.of(false), output.model.signal)
			val settledAt = scheduler.executionTime
			assertTrue(settledAt >= 10_000)
			scheduler.execute()
			assertEquals(settledAt + 500, scheduler.executionTime)
			assertTrue(clock.isOn)
			assertEquals(1L, clock.cycleCount)
		}
	}

	@Test
	fun shouldNotClockNativeComponentsFromAnInitialHighLevel() {
		val builder = TestCircuitBuilder("startup native clock")
		var samples = 0
		val random = Random { samples++; 1UL }
		val randomView = builder.add(RandomView(model = random))
		val input = builder.addInput("C", inputStartValue = DigitalSignalFactory.of(true))
		builder.connect(input, randomView)
		builder.connect(randomView, builder.addOutput("O", random.bitWidth))

		withInitializedCircuit(builder) { _, runner ->
			assertEquals(0, samples)
			input.model.setIncomingSignal(DigitalSignalFactory.of(false), runner.scheduler)
			runner.proceedUntilQueueEmpty()
			input.model.setIncomingSignal(DigitalSignalFactory.of(true), runner.scheduler)
			runner.proceedUntilQueueEmpty()
			assertEquals(1, samples)
		}
	}

	private fun withInitializedCircuit(builder: TestCircuitBuilder, check: (DigitalGraph, ControlledCircuitRunner) -> Unit) {
		val graph = builder.build().graph as DigitalGraph
		val runner = ControlledCircuitRunner()
		val scheduler = runner.scheduler
		scheduler.isDeepExecution = false
		scheduler.isSingleStepMode = true
		scheduler.isSoftBreakpointsEnabled = false
		try {
			graph.bind(false, LibraryModule.libraryHolder)
			scheduler.isActive = true
			graph.formNet(scheduler)
			graph.executionInitialize(scheduler)
			graph.executionStart(scheduler, null)
			var waves = 0
			while (scheduler.isInitializing) {
				assertTrue(++waves < 1_000, "Initialization did not settle")
				scheduler.execute()
			}
			check(graph, runner)
		} finally {
			scheduler.isActive = false
			graph.executionStopped(scheduler)
			runner.dispose()
		}
	}
}
