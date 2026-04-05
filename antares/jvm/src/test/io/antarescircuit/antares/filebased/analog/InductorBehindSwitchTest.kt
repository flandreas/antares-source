package io.antarescircuit.antares.filebased.analog

import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.view.analog.AnalogCircuitInOutView
import io.antarescircuit.antares.view.analog.AnalogEdgeView
import io.antarescircuit.antares.view.analog.AnalogSwitchView
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.math.near
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class InductorBehindSwitchTest : AbstractAnalogFileBasedTest() {

    private lateinit var inputView: AnalogCircuitInOutView
    private lateinit var switchView: AnalogSwitchView
    private lateinit var edgeView: AnalogEdgeView

    @BeforeTest
    fun openCircuit() {
        openCircuit(UUID("6c51e498-0216-4587-b90f-cfffbffc6cb4"))
        inputView = openedCircuitView.getWithId(1) as AnalogCircuitInOutView
        switchView = openedCircuitView.getWithId(2) as AnalogSwitchView
        edgeView = openedCircuitView.getWithId(7) as AnalogEdgeView
    }

    @Test
    fun shouldNotOscillateAfterSwitchingOff() {
        startSimulation()
        processUntilQueueIsEmpty()

        // On
        inputView.model.toggle(scheduler)
        switchView.model.toggle(scheduler)
        processUntilQueueIsEmpty()
        assertTrue((edgeView.net!!.signal as AnalogSignal).voltage.near(5.0, 0.1))
        assertTrue(edgeView.current.near(0.05, 0.1))

        // Off
        switchView.model.toggle(scheduler)
        proceedToNanos(70)
        assertTrue((edgeView.net!!.signal as AnalogSignal).voltage.near(0.0, 0.1))
        assertTrue(edgeView.current.near(0.0, 0.1))

        assertTrue(scheduler.isQueueEmpty)
    }
}