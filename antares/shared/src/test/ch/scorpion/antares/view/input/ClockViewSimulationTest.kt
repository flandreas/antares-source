package ch.scorpion.antares.view.input

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.input.PeriodOrFrequency
import ch.scorpion.antares.model.input.PeriodOrFrequencyUnit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.graph.view.GraphView
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

		assertEquals(Word.of(false), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal() as Word)
		assertFalse(ledView.model.isOn)

		proceedToMillis(50L)
		assertEquals(Word.of(true), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal() as Word)

		proceedToMillis(51L)
		assertTrue(ledView.model.isOn)

		proceedToMillis(100L)
		assertEquals(Word.of(false), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal() as Word)

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
	fun inactiveClockShouldNotProduceHighSignal() {
		clockView.isEnabled = false
		startSimulation()

		proceedToMillis(50L)

		assertEquals(Word.of(false), clockView.model.getOutput<DigitalSignal>().getOutgoingSignal() as Word)
	}
}