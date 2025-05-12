package ch.scorpion.antares.view.input

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.input.PeriodOrFrequency
import ch.scorpion.antares.model.input.PeriodOrFrequencyUnit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.StorableCloner
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
		clockView.model.periodOrFrequency = PeriodOrFrequency(100, PeriodOrFrequencyUnit.Millisecond)
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
		clockView.model.periodOrFrequency = PeriodOrFrequency(200, PeriodOrFrequencyUnit.Millisecond)

		stopSimulation()

		assertEquals(PeriodOrFrequency(100, PeriodOrFrequencyUnit.Millisecond), clockView.model.periodOrFrequency)
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