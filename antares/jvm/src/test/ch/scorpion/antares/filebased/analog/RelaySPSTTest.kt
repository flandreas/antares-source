package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.AnalogRelayView
import ch.scorpion.antares.view.analog.ResistorView
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.math.near
import junit.framework.TestCase.assertTrue
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse

class RelaySPSTTest : AbstractAnalogFileBasedTest() {

    private lateinit var resistorView: ResistorView
    private lateinit var relayView: AnalogRelayView
    private lateinit var switchEdgeView: AnalogEdgeView

    @BeforeTest
    fun openCircuit() {
        openCircuit(UUID("385cedbe-6bc1-4bda-b301-6845069fc777"))
        resistorView = openedCircuitView.getWithId(4) as ResistorView
        relayView = openedCircuitView.getWithId(2) as AnalogRelayView
        switchEdgeView = openedCircuitView.getWithId(11) as AnalogEdgeView
    }

    @Test
    fun shouldBeOff() {
        startSimulation()
        processUntilQueueIsEmpty()

        assertFalse(relayView.model.isOn)
        assertTrue(switchEdgeView.current.near(0.0, 0.1))
        assertTrue(switchEdgeView.model.signal!!.voltage.near(0.0, 0.1))
    }

    @Test
    fun shouldSwitchOn() {
        startSimulation()
        processUntilQueueIsEmpty()

        resistorView.model.setState(100.0, scheduler, openedCircuitView as AnalogGraphView)
        processUntilQueueIsEmpty()

        assertTrue(relayView.model.isOn)
        assertTrue(switchEdgeView.current.near(0.05, 0.1))
        assertTrue(switchEdgeView.model.signal!!.voltage.near(0.0, 0.1))
    }
}