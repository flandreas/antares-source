package ch.scorpion.antares.view.input

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test


class ClockViewSimulationTest : AbstractCircuitTest() {

    private lateinit var circuitView: GraphView<GraphElementView<*>>
    private lateinit var clockView: ClockView
    private lateinit var ledView: LEDView

    override fun getCircuitView(): GraphView<GraphElementView<*>> {
        return circuitView
    }

    @Before
    fun setupCircuit() {
        val builder = TestCircuitBuilder("test", styleProvider, eventBus)
        clockView = builder.addVertice(ClockView(styleProvider))
        clockView.period = 100 * 1_000
        ledView = builder.addVertice(LEDView(styleProvider))
        builder.connect(clockView, ledView)
        circuitView = builder.build()
    }

    @Test
    fun test() {
        startSimulation()

        assertThat(clockView.model!!.getOutput<DigitalSignal>().getOutgoingSignal() as Word, `is`(Word.of(false)))
        assertThat(ledView.model!!.isOn, `is`(false))

        proceedToMillis(50L)
        assertThat(clockView.model!!.getOutput<DigitalSignal>().getOutgoingSignal() as Word, `is`(Word.of(true)))

        proceedToMillis(51L)
        assertThat(ledView.model!!.isOn, `is`(true))

        proceedToMillis(100L)
        assertThat(clockView.model!!.getOutput<DigitalSignal>().getOutgoingSignal() as Word, `is`(Word.of(false)))

        proceedToMillis(101L)
        assertThat(ledView.model!!.isOn, `is`(false))
    }
}