package io.antarescircuit.antares.filebased.analog

import io.antarescircuit.antares.view.analog.AnalogEdgeView
import io.antarescircuit.antares.view.analog.AnalogGraphView
import io.antarescircuit.antares.view.analog.AnalogRelayView
import io.antarescircuit.antares.view.analog.ResistorView
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.math.near
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
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

        resistorView.model.setState(MagnitudeValue(100.0, Magnitude.One, SIUnit.Ohm), scheduler, openedCircuitView as AnalogGraphView)
        processUntilQueueIsEmpty()

        assertTrue(relayView.model.isOn)
        assertTrue(switchEdgeView.current.near(0.05, 0.1))
        assertTrue(switchEdgeView.model.signal!!.voltage.near(0.0, 0.1))
    }
}