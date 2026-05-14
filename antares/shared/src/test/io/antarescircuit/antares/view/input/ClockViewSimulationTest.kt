package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.io.StorableCloner
import kotlin.test.*

class ClockViewSimulationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var clockView: ClockView
	private lateinit var ledView: LEDView

	override fun getCircuitView(): GraphView {
		return circuitView
	}

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		clockView = builder.addVerticeView(ClockView(styleProvider))
		clockView.model.periodOrFrequency = MagnitudeValue(100, Magnitude.Milli, SIUnit.Second)
		ledView = builder.addVerticeView(LEDView(styleProvider))
		builder.connect(clockView, ledView)
		circuitView = builder.build()
	}

	@Test
	fun shouldPeriodicallyChangeOutput() {
		startSimulation()

		assertEquals(DigitalSignalFactory.of(false), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())
		assertFalse(ledView.model.isOn)

		proceedToMillis(50L)
		assertEquals(DigitalSignalFactory.of(true), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())

		proceedToMillis(51L)
		assertTrue(ledView.model.isOn)

		proceedToMillis(100L)
		assertEquals(DigitalSignalFactory.of(false), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())

		proceedToMillis(101L)
		assertFalse(ledView.model.isOn)
	}

	@Test
	fun shouldResetPropagationDelayAfterSimulation() {
		startSimulation()
		clockView.model.periodOrFrequency = MagnitudeValue(200, Magnitude.Milli, SIUnit.Second)

		stopSimulation()

		assertEquals(MagnitudeValue(100, Magnitude.Milli, SIUnit.Second), clockView.model.periodOrFrequency)
	}

	@Test
	fun shouldNotWritePropagationDelayChangedDuringSimulation() {
		startSimulation()
		// The knob sets the propagation delay directly
		clockView.model.propagationDelay = LongValueImpl(200_000)
		stopSimulation()

		val clone = StorableCloner.clone(clockView.model)

		assertEquals(100_000_000, clone.propagationDelay.value)
	}

	@Test
	fun inactiveClockShouldNotProduceHighSignal() {
		clockView.isEnabled = false
		startSimulation()

		proceedToMillis(50L)

		assertEquals(DigitalSignalFactory.of(false), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())
	}

	@Test
	fun shouldApplyOffPercentage() {
		clockView.offPercentage = 20.0
		startSimulation()

		proceedToMillis(19L)
		assertEquals(DigitalSignalFactory.of(false), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())

		proceedToMillis(20L)
		assertEquals(DigitalSignalFactory.of(true), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())

		proceedToMillis(100L)
		assertEquals(DigitalSignalFactory.of(false), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())

		proceedToMillis(119L)
		assertEquals(DigitalSignalFactory.of(false), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())

		proceedToMillis(120L)
		assertEquals(DigitalSignalFactory.of(true), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())

		proceedToMillis(199L)
		assertEquals(DigitalSignalFactory.of(true), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())

		proceedToMillis(200L)
		assertEquals(DigitalSignalFactory.of(false), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())
	}

	@Test
	fun shouldStayOffWithHundredOffPercent() {
		clockView.offPercentage = 100.0
		startSimulation()

		proceedToMillis(50L)
		assertEquals(DigitalSignalFactory.of(false), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())

		proceedToMillis(150L)
		assertEquals(DigitalSignalFactory.of(false), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())

		proceedToMillis(250L)
		assertEquals(DigitalSignalFactory.of(false), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())
	}

	@Test
	fun shouldStayOnWithZeroOffPercent() {
		clockView.offPercentage = 0.0
		startSimulation()

		proceedToMillis(50L)
		assertEquals(DigitalSignalFactory.of(true), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())

		proceedToMillis(150L)
		assertEquals(DigitalSignalFactory.of(true), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())

		proceedToMillis(250L)
		assertEquals(DigitalSignalFactory.of(true), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal())
	}
}