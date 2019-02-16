package ch.scorpion.antares.view.input

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.*

class ClockViewSimulationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView<GraphElementView<*>>
	private lateinit var clockView: ClockView
	private lateinit var ledView: LEDView

	override fun getCircuitView(): GraphView<GraphElementView<*>> {
		return circuitView
	}

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		clockView = builder.addVerticeView(ClockView(styleProvider))
		clockView.period = 100 * 1_000
		ledView = builder.addVerticeView(LEDView(styleProvider))
		builder.connect(clockView, ledView)
		circuitView = builder.build()
	}

	@Test
	fun test() {
		startSimulation()

		assertEquals(Word.of(false), clockView.model!!.getOutput<DigitalSignal>().getOutgoingSignal() as Word)
		assertFalse(ledView.model!!.isOn)

		proceedToMillis(50L)
		assertEquals(Word.of(true), clockView.model!!.getOutput<DigitalSignal>().getOutgoingSignal() as Word)

		proceedToMillis(51L)
		assertTrue(ledView.model!!.isOn)

		proceedToMillis(100L)
		assertEquals(Word.of(false), clockView.model!!.getOutput<DigitalSignal>().getOutgoingSignal() as Word)

		proceedToMillis(101L)
		assertFalse(ledView.model!!.isOn)
	}
}